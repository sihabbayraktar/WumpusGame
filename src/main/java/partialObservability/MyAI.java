package partialObservability;

import common.Direction;
import common.Entity;
import common.Position;
import fullObservability.SearchAI;
import org.tweetyproject.logics.pl.reasoner.SatReasoner;
import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.syntax.*;
import wumpus.Agent;

import java.util.LinkedList;

public class MyAI extends Agent {

    private final PlBeliefSet KB;
    private final SatReasoner satReasoner;
    private final LinkedList<Action> safeplan;

    private final int[] cp;
    private final int[] world;
    private final int[] maxDist;
    Action lastAction = null;

    Proposition wumpusExist, arrowExist, rightDir, leftDir, downDir, upDir;

    public MyAI() {
        this.KB = new PlBeliefSet();
        this.satReasoner = new SatReasoner();
        this.safeplan = new LinkedList<>();
        this.cp = new int[]{0, 0};
        this.world = new int[]{10, 10};
        this.maxDist = new int[]{0, 0};

        this.wumpusExist = new Proposition("WumpusExist");
        this.arrowExist = new Proposition("ArrowExist");
        this.rightDir = new Proposition("right");
        this.upDir = new Proposition("up");
        this.downDir = new Proposition("down");
        this.leftDir = new Proposition("left");

        SatSolver.setDefaultSolver(new Sat4jSolver());

        initRule();
    }

     Proposition prop(String entity, Position p) {
        String explanation = entity + "_[" + p.x + " , " + p.y + " ]";
        return new Proposition(explanation);
    }

    public Agent.Action getAction(boolean stench, boolean breeze, boolean glitter, boolean bump, boolean scream) {

        if (scream) {
            KB.remove(wumpusExist);
            if (!breeze) {
                safeplan.add(Action.FORWARD);
            }

            for (int i = 0; i < maxDist[0] + 1; i++) {
                for (int j = 0; j < maxDist[1] + 1; j++) {
                    Proposition stenchProp = prop(Entity.STENCH.idstr(), new Position(i, j));
                    if (satReasoner.query(KB, stenchProp)
                            && satReasoner.query(KB, prop(Entity.VISITED.idstr(), new Position(i, j)))) {
                        KB.remove(stenchProp);
                        if (!satReasoner.query(KB, prop(Entity.BREEZE.idstr(), new Position(i, j)))) {
                            safeEnviromentAdd(new int[]{i, j});
                        }
                    }
                }
            }
        }

        if (bump) {
            switch (direction()) {
                case Right:
                    world[0] = cp[0];
                    for (int i = 0; i < world[1]; i++) {
                        KB.remove(prop(Entity.SAFE.idstr(), new Position(world[0] + 1, i)));
                    }
                    break;
                case Up:
                    world[1] = cp[1];
                    for (int i = 0; i < world[0]; i++) {
                        KB.remove(prop(Entity.SAFE.idstr(), new Position(i, world[1] + 1)));
                    }
                    break;
                default:
                    break;
            }
            safeplan.clear();
        } else {
            positionUpdate();
        }

        if (stench && satReasoner.query(KB, wumpusExist)) {
            KB.add(prop(Entity.STENCH.idstr(), new Position(cp[0], cp[1])));
        } else {
            stench = false;
            KB.add(new Negation(prop(Entity.STENCH.idstr(), new Position(cp[0], cp[1]))));
        }
        if (breeze) {
            KB.add(prop(Entity.BREEZE.idstr(), new Position(cp[0], cp[1])));
        } else {
            KB.add(new Negation(prop(Entity.BREEZE.idstr(), new Position(cp[0], cp[1]))));
        }

        KB.add(prop(Entity.SAFE.idstr(), new Position(cp[0], cp[1])),
                prop(Entity.VISITED.idstr(), new Position(cp[0], cp[1])),
                new Negation(prop(Entity.PIT.idstr(), new Position(cp[0], cp[1]))));
        if (satReasoner.query(KB, wumpusExist)) {
            KB.add(new Negation(prop(Entity.WUMPUS.idstr(),
                    new Position(cp[0], cp[1]))));
        }

        if (!breeze && !stench) {
            safeEnviromentAdd(cp);
        }

        if (glitter) {
            safeplan.clear();
            KB.add(prop(Entity.GOLD.idstr(), new Position(cp[0], cp[1])));
            safeplan.add(Action.GRAB);
            LinkedList<Action> plan = SearchAI.search(cp, new int[]{0, 0}, createTile(cp), direction());
            safeplan.addAll(plan);
            safeplan.add(Action.CLIMB);
        }

        if (safeplan.size() == 0) {
            int[] tiles = findUnvisitedSafeTile();
            if (tiles != null) {
                LinkedList<Action> plan = SearchAI.search(cp, tiles, createTile(new int[]{0, 0}), direction());
                safeplan.addAll(plan);
            }
        }

        if (safeplan.size() == 0 && satReasoner.query(KB, arrowExist)) {
            int[] findPossibleWumpus = findWumpus();

            if (findPossibleWumpus != null) {
                LinkedList<Action> plan = SearchAI.search(cp, findPossibleWumpus, createTile(findPossibleWumpus), direction());
                safeplan.addAll(plan);
                safeplan.removeLast();
                safeplan.add(Action.SHOOT);
            }
        }

        if (safeplan.size() == 0) {
            int[] tiles = findUnsafeUnvisitedTile();
            if (tiles != null) {
                LinkedList<Action> plan = SearchAI.search(cp, tiles, createTile(tiles), direction());
                safeplan.addAll(plan);
            }
        }
        if (safeplan.size() == 0) {
            LinkedList<Action> plan = SearchAI.search(cp, new int[]{0, 0}, createTile(new int[]{0, 0}), direction());
            safeplan.addAll(plan);
            safeplan.add(Action.CLIMB);
        }

        lastAction = safeplan.pop();
        if (lastAction == Action.SHOOT) {
            KB.remove(arrowExist);
        }
        return lastAction;
    }

    void initRule() {
        int colNum = 10;
        int rowNum = 10;
        Disjunction atLeastOneWumpus = new Disjunction();

        for (int col = 0; col < colNum; col++) {
            for (int row = 0; row < rowNum; row++) {

                Disjunction wumpDist = new Disjunction();
                Disjunction pitDist = new Disjunction();


                //Left
                if (col != 0) {
                    pitDist.add(prop(Entity.PIT.idstr(), new Position(col - 1, row)));
                    wumpDist.add(prop(Entity.WUMPUS.idstr(), new Position(col - 1, row)));
                }

                //Right
                if (col != colNum - 1) {
                    pitDist.add(prop(Entity.PIT.idstr(), new Position(col + 1, row)));
                    wumpDist.add(prop(Entity.WUMPUS.idstr(), new Position(col + 1, row)));
                }

                //Down
                if (row != 0) {
                    pitDist.add(prop(Entity.PIT.idstr(), new Position(col, row - 1)));
                    wumpDist.add(prop(Entity.WUMPUS.idstr(), new Position(col, row - 1)));
                }

                //Up
                if (row != rowNum - 1) {
                    pitDist.add(prop(Entity.PIT.idstr(), new Position(col, row + 1)));
                    wumpDist.add(prop(Entity.WUMPUS.idstr(), new Position(col, row + 1)));
                }

                KB.add(new Equivalence(prop(Entity.BREEZE.idstr(), new Position(col, row)), pitDist),
                        new Equivalence(prop(Entity.STENCH.idstr(), new Position(col, row)), wumpDist));
                atLeastOneWumpus.add(prop(Entity.WUMPUS.idstr(), new Position(col, row)));

                Negation noWumpusCurrentTile = new Negation(prop(Entity.WUMPUS.idstr(), new Position(col, row)));
                for (int col1 = 0; col1 < colNum; col1++) {
                    for (int row1 = 0; row1 < rowNum; row1++) {
                        if (col1 != col && row1 != row) {
                            KB.add(new Disjunction(noWumpusCurrentTile,
                                    new Negation(prop(Entity.WUMPUS.idstr(), new Position(col1, row1)))));
                        }
                    }
                }
            }
        }
        KB.add(atLeastOneWumpus, rightDir, arrowExist, wumpusExist);
        KB.add(prop(Entity.SAFE.idstr(), new Position(0, 0)));
        KB.add(new Negation(prop(Entity.WUMPUS.idstr(), new Position(0, 0))));
        KB.add(new Negation(prop(Entity.PIT.idstr(), new Position(0, 0))));
    }


    Direction direction() {

        if (satReasoner.query(KB, rightDir)) {
            return Direction.Right;
        }

        if (satReasoner.query(KB, downDir)) {
            return Direction.Down;
        }

        if (satReasoner.query(KB, leftDir)) {
            return Direction.Left;
        }

        if (satReasoner.query(KB, upDir)) {
            return Direction.Up;
        }

        return Direction.NoDirection;
    }


    void positionUpdate() {

        switch (direction()) {

            case Right:
                if (lastAction == Action.FORWARD) {
                    cp[0] = cp[0] + 1;
                    if (cp[0] > maxDist[0]) {
                        maxDist[0] = cp[0];
                    }
                } else if (lastAction == Action.TURN_RIGHT) {
                    KB.remove(rightDir);
                    KB.add(downDir);
                } else if (lastAction == Action.TURN_LEFT) {
                    KB.remove(rightDir);
                    KB.add(upDir);
                }
                break;

            case Down:
                if (lastAction == Action.FORWARD) {
                    cp[1] = cp[1] - 1;
                } else if (lastAction == Action.TURN_RIGHT) {
                    KB.remove(downDir);
                    KB.add(leftDir);
                } else if (lastAction == Action.TURN_LEFT) {
                    KB.remove(downDir);
                    KB.add(rightDir);
                }
                break;

            case Left:
                if (lastAction == Action.FORWARD) {
                    cp[0] = cp[0] - 1;
                } else if (lastAction == Action.TURN_RIGHT) {
                    KB.remove(leftDir);
                    KB.add(upDir);
                } else if (lastAction == Action.TURN_LEFT) {
                    KB.remove(leftDir);
                    KB.add(downDir);
                }
                break;

            case Up:
                if (lastAction == Action.FORWARD) {
                    cp[1] = cp[1] + 1;
                    //Further distance we have travelled up
                    if (cp[1] > maxDist[1]) {
                        maxDist[1] = cp[1];
                    }
                } else if (lastAction == Action.TURN_RIGHT) {
                    KB.remove(upDir);
                    KB.add(rightDir);
                } else if (lastAction == Action.TURN_LEFT) {
                    KB.remove(upDir);
                    KB.add(leftDir);
                }
                break;
            default:
                break;
        }
    }

    int[][] createTile(int[] optUnvisitedTile) {

        int xcorSize = Math.max(maxDist[0] + 1, optUnvisitedTile[0]);
        int ycorSize = Math.max(maxDist[1] + 1, optUnvisitedTile[1]);

        int[][] tiles =
                new int[xcorSize + 1][ycorSize + 1];

        for (int i = 0; i < xcorSize + 1; i++) {
            for (int j = 0; j < ycorSize + 1; j++) {

                if (satReasoner.query(KB, prop(Entity.SAFE.idstr(), new Position(i, j))) ||
                        (optUnvisitedTile[0] == i && optUnvisitedTile[1] == j)) {
                    tiles[i][j] = 0;
                } else {
                    tiles[i][j] = 1;
                }
            }
        }
        return tiles;
    }

    void safeEnviromentAdd(int[] position) {

        //Left
        if (position[0] != 0) {
            Position pos = new Position(position[0] - 1, position[1]);
            addSafeEnvironmentRule(pos);
        }

        //Right
        if (position[0] != world[0]) {
            Position pos = new Position(position[0] + 1, position[1]);
            addSafeEnvironmentRule(pos);
        }
        //Down
        if (position[1] != 0) {
            Position pos = new Position(position[0], position[1] - 1);
            addSafeEnvironmentRule(pos);
        }
        //Up
        if (position[1] != world[1]) {
            Position pos = new Position(position[0], position[1] + 1);
            addSafeEnvironmentRule(pos);
        }
    }

    PlBeliefSet addSafeEnvironmentRule(Position p) {
        KB.add(prop(Entity.SAFE.idstr(), p),
                new Negation(prop(Entity.WUMPUS.idstr(), p)),
                new Negation(prop(Entity.PIT.idstr(), p)));
        return KB;
    }

    int[] findUnvisitedSafeTile() {

        //Up
        Position up = new Position(cp[0], cp[1] + 1);
        boolean upCond = unvisitedSafeTileCheck(cp[1] + 1 <= maxDist[1] + 1 && cp[1] + 1 < world[1], up);
        if (upCond) {
            return new int[]{up.x, up.y};
        }

        //Down
        Position dp = new Position(cp[0], cp[1] - 1);
        boolean downCond = unvisitedSafeTileCheck(cp[1] - 1 >= 0, dp);
        if (downCond) {
            return new int[]{dp.x, dp.y};
        }

        //Right
        Position rp = new Position(cp[0] + 1, cp[1]);
        boolean rightCond = unvisitedSafeTileCheck(cp[0] + 1 <= maxDist[0] + 1 && cp[0] + 1 < world[0], rp);
        if (rightCond) {
            return new int[]{rp.x, rp.y};
        }

        //Left
        Position lp = new Position(cp[0] - 1, cp[1]);
        boolean leftCond = unvisitedSafeTileCheck(cp[0] - 1 >= 0, lp);
        if (leftCond) {
            return new int[]{lp.x, lp.y};
        }

        for (int i = 0; i <= maxDist[0] + 1 && i < world[0]; i++) {
            for (int j = 0; j <= maxDist[0] + 1 && j < world[1]; j++) {
                Position pos = new Position(i, j);
                boolean cond = unvisitedSafeTileCheck((cp[0] != i || cp[1] != j), pos);
                if (cond) {
                    return new int[]{pos.x, pos.y};
                }
            }
        }
        return null;
    }

    boolean unvisitedSafeTileCheck(boolean posCheck, Position p) {
        return posCheck &&
                satReasoner.query(KB, prop(Entity.SAFE.idstr(), p)) &&
                !satReasoner.query(KB, prop(Entity.VISITED.idstr(), p));
    }

    int[] findWumpus() {
        for (int i = 0; i < maxDist[0] + 2 && i < world[0]; i++) {
            for (int j = 0; j < maxDist[1] + 2 && j < world[1]; j++) {
                if (satReasoner.query(KB, prop(Entity.STENCH.idstr(), new Position(i, j)))) {

                    Position up = new Position(i, j + 1);
                    boolean upCond = possibleWumpusCheck(j + 1 <= maxDist[1] + 1, up);
                    if (upCond) {
                        return new int[]{up.x, up.y};
                    }

                    Position dp = new Position(i, j - 1);
                    boolean downCond = possibleWumpusCheck(j - 1 >= 0, dp);
                    if (downCond) {
                        return new int[]{dp.x, dp.y};
                    }

                    Position rp = new Position(i + 1, j);
                    boolean rightCond = possibleWumpusCheck(i + 1 <= maxDist[0] + 1, rp);
                    if (rightCond) {
                        return new int[]{rp.x, rp.y};
                    }

                    Position lp = new Position(i - 1, j);
                    boolean leftCond = possibleWumpusCheck(i - 1 >= 0, lp);
                    if (leftCond) {
                        return new int[]{lp.x, lp.y};
                    }

                }
            }
        }
        return null;
    }

    boolean possibleWumpusCheck(boolean posCheck, Position p) {
        return posCheck &&
                (satReasoner.query(KB, prop(Entity.WUMPUS.idstr(), p)) ||
                        (!satReasoner.query(KB, new Negation(prop(Entity.WUMPUS.idstr(), p)))
                                && !satReasoner.query(KB, prop(Entity.PIT.idstr(), p))
                                && !satReasoner.query(KB, prop(Entity.VISITED.idstr(), p))));
    }

    int[] findUnsafeUnvisitedTile() {

        Position up = new Position(cp[0], cp[1] + 1);
        boolean upCond = unsafeUnvisitedTileCheck(cp[1] + 1 <= maxDist[1] + 1 && cp[1] + 1 < world[1], up);

        if (upCond) {
            return new int[]{up.x, up.y};
        }

        Position dp = new Position(cp[0], cp[1] - 1);
        boolean downCond = unsafeUnvisitedTileCheck(cp[1] - 1 >= 0, dp);

        if (downCond) {
            return new int[]{dp.x, dp.y};
        }

        Position rp = new Position(cp[0] + 1, cp[1]);
        boolean rightCond = unsafeUnvisitedTileCheck(cp[0] + 1 <= maxDist[0] + 1 && cp[0] + 1 < world[0], rp);

        if (rightCond) {
            return new int[]{rp.x, rp.y};
        }

        Position lp = new Position(cp[0] - 1, cp[1]);
        boolean leftCond = unsafeUnvisitedTileCheck(cp[0] - 1 >= 0, lp);

        if (leftCond) {
            return new int[]{lp.x, lp.y};
        }

        for (int i = 0; i < maxDist[0] + 1 && i < world[0]; i++) {
            for (int j = 0; j < maxDist[1] + 1 && j < world[1]; j++) {
                if ((cp[0] != i || cp[1] != j) && satReasoner
                        .query(KB, prop(Entity.VISITED.idstr(), new Position(i, j)))) {

                    Position up1 = new Position(i, j + 1);
                    boolean upCond1 = unsafeUnvisitedTileCheck(j + 1 <= maxDist[1] + 1, up1);
                    if (upCond1) {
                        return new int[]{up1.x, up1.y};
                    }

                    Position dp1 = new Position(i, j - 1);
                    boolean downCond1 = unsafeUnvisitedTileCheck(j - 1 >= 0, dp1);
                    if (downCond1) {
                        return new int[]{dp1.x, dp1.y};
                    }

                    Position rp1 = new Position(i + 1, j);
                    boolean rightCond1 = unsafeUnvisitedTileCheck(i + 1 <= maxDist[0] + 1, rp1);
                    if (rightCond1) {
                        return new int[]{rp1.x, rp1.y};
                    }

                    Position lp1 = new Position(i - 1, j);
                    boolean leftCond1 = unsafeUnvisitedTileCheck(i - 1 >= 0, lp1);
                    if (leftCond1) {
                        return new int[]{lp1.x, lp1.y};
                    }
                }
            }
        }
        return null;
    }

    boolean unsafeUnvisitedTileCheck(boolean posCheck, Position p) {

        return posCheck &&
                !satReasoner.query(KB, prop(Entity.VISITED.idstr(), p)) &&
                !satReasoner.query(KB, prop(Entity.PIT.idstr(), p)) &&
                !satReasoner.query(KB, prop(Entity.WUMPUS.idstr(), p));
    }
}
