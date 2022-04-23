package fullObservability;


import common.Direction;
import wumpus.Agent;
import wumpus.World;

import java.util.*;

import static common.Direction.*;


public class SearchAI extends Agent {

    private final ListIterator<Action> planIterator;
    private final Queue<int[]> queue;
    private final List<int[]> visited;
    private final Queue<int[]> qw;
    private final List<int[]> vw;
    public Direction dir;
    LinkedList<Action> plan = new LinkedList<>();
    private int[] currentPosition = new int[]{-1, -1};
    private int[] goldPosition;
    private boolean wumpusDead = false;

    public SearchAI(World.Tile[][] board) {

        this.dir = Right;
        this.queue = new LinkedList<>();
        this.vw = new ArrayList<>();
        this.qw = new LinkedList<>();
        this.visited = new ArrayList<>();


        int pointsWumpusKilled = pathControl(board, true);
        int pointsWumpusNotKilled = pathControl(board, false);

        if (goldPosition != null) {

            List<int[]> path = findPath(pointsWumpusKilled < pointsWumpusNotKilled ? vw : visited);
            for (int i = path.size() - 1; i > 0; i--) {
                proceed(path.get(i), path.get(i - 1), board);
            }
            plan.add(Action.GRAB);
            for (int i = 0; i < path.size() - 1; i++) {
                proceed(path.get(i), path.get(i + 1), board);
            }
        }
        plan.add(Action.CLIMB);

        planIterator = plan.listIterator();
    }

    public static LinkedList<Action> search(int[] safeStart, int[] safeGoal, int[][] map, Direction dir) {

        if (safeGoal[0] == safeStart[0] && safeGoal[1] == safeStart[1]) {
            return new LinkedList<>();
        } else {
            Queue<int[]> queueSafe = new LinkedList<>();
            ArrayList<int[]> visitedSafe = new ArrayList<>();

            int[] currentPosition;
            queueSafe.add(safeStart);

            while (!queueSafe.isEmpty()) {

                currentPosition = queueSafe.remove();
                visitedSafe.add(currentPosition);
                if (currentPosition[0] == safeGoal[0] && currentPosition[1] == safeGoal[1]) {
                    break;
                } else {
                    findSafeNeigbours(map, currentPosition, queueSafe, visitedSafe);
                }
            }

            return moveSafe(visitedSafe, safeStart, safeGoal, dir);
        }
    }

    private static void findSafeNeigbours(int[][] map, int[] currentPosition, Queue<int[]> queue1, ArrayList<int[]> visited1) {

        //Left
        int[] nl = {currentPosition[0] - 1, currentPosition[1]};
        if (nl[0] >= 0 && map[nl[0]][nl[1]] == 0 && !isVisitedOrInQueueSafe(nl, queue1, visited1)) {
            queue1.add(new int[]{nl[0], nl[1], currentPosition[0], currentPosition[1]});
        }
        //Right
        int[] nr = {currentPosition[0] + 1, currentPosition[1]};
        if (nr[0] < (map.length) && map[nr[0]][nr[1]] == 0 && !isVisitedOrInQueueSafe(nr, queue1, visited1)) {
            queue1.add(new int[]{nr[0], nr[1], currentPosition[0], currentPosition[1]});
        }
        //Up
        int[] nu = {currentPosition[0], currentPosition[1] + 1};
        if (nu[1] < map[0].length && map[nu[0]][nu[1]] == 0 && !isVisitedOrInQueueSafe(nu, queue1, visited1)) {
            queue1.add(new int[]{nu[0], nu[1], currentPosition[0], currentPosition[1]});
        }
        //Down
        int[] nd = {currentPosition[0], currentPosition[1] - 1};
        if (nd[1] >= 0 && map[nd[0]][nd[1]] == 0 && !isVisitedOrInQueueSafe(nd, queue1, visited1)) {
            queue1.add(new int[]{nd[0], nd[1], currentPosition[0], currentPosition[1]});
        }
    }

    private static boolean isVisitedOrInQueueSafe(int[] position, Queue<int[]> queue1, ArrayList<int[]> visited1) {
        return isVisitedOrInQueue(position, queue1, visited1);
    }

    private static LinkedList<Action> moveSafe(ArrayList<int[]> visitedPath, int[] safeStart, int[] safeGoal, Direction dir) {
        LinkedList<Action> planSafe = new LinkedList<>();
        boolean isFinished = false;

        int[] current = safeStart;
        Direction initdir = dir;

        int[] from = new int[]{0, 0};
        int[] to = new int[]{0, 0};

        while (!isFinished) {
            boolean isFound = false;

            for (int[] ints : visitedPath) {

                if (ints.length > 2 && current[0] == ints[2] && current[1] == ints[3]) {
                    current = ints;

                    from[0] = current[2];
                    from[1] = current[3];

                    to[0] = current[0];
                    to[1] = current[1];

                    // RIGHT
                    if (from[0] == to[0] - 1) {
                        switch (dir) {

                            case Down:
                                planSafe.add(Action.TURN_LEFT);
                                break;

                            case Left:
                                planSafe.add(Action.TURN_LEFT);
                                planSafe.add(Action.TURN_LEFT);
                                break;

                            case Up:
                                planSafe.add(Action.TURN_RIGHT);
                                break;

                            default:
                                break;
                        }
                        dir = Right;
                    }
                    //LEFT
                    else if (from[0] == to[0] + 1) {
                        switch (dir) {

                            case Right:
                                planSafe.add(Action.TURN_LEFT);
                                planSafe.add(Action.TURN_LEFT);
                                break;

                            case Up:
                                planSafe.add(Action.TURN_LEFT);
                                break;

                            case Down:
                                planSafe.add(Action.TURN_RIGHT);
                                break;

                            default:
                                break;
                        }
                        dir = Left;
                    }
                    //UP
                    else if (from[1] == to[1] - 1) {
                        switch (dir) {

                            case Right:
                                planSafe.add(Action.TURN_LEFT);
                                break;

                            case Down:
                                planSafe.add(Action.TURN_LEFT);
                                planSafe.add(Action.TURN_LEFT);
                                break;

                            case Left:
                                planSafe.add(Action.TURN_RIGHT);
                                break;

                            default:
                                break;
                        }
                        dir = Up;
                    }
                    //DOWN
                    else if (from[1] == to[1] + 1) {
                        switch (dir) {

                            case Right:
                                planSafe.add(Action.TURN_RIGHT);
                                break;

                            case Left:
                                planSafe.add(Action.TURN_LEFT);
                                break;

                            case Up:
                                planSafe.add(Action.TURN_LEFT);
                                planSafe.add(Action.TURN_LEFT);
                                break;


                            default:
                                break;
                        }
                        dir = Down;
                    }
                    planSafe.add(Action.FORWARD);
                    isFound = true;
                    break;
                }
            }

            if (!isFound) {
                visitedPath.remove(current);
                current = safeStart;
                dir = initdir;
                planSafe.clear();
            }

            if (current[0] == safeGoal[0] && current[1] == safeGoal[1]) {
                isFinished = true;
            }
        }
        return planSafe;
    }

    private static boolean isVisitedOrInQueue(int[] position, Queue<int[]> queue, List<int[]> visited) {

        boolean isVisited = visited.stream().anyMatch(ints -> position[0] == ints[0] && position[1] == ints[1]);
        List<int[]> que = new ArrayList<>(queue);
        boolean isQue = que.stream().anyMatch(ints -> position[0] == ints[0] && position[1] == ints[1]);
        return isVisited || isQue;
    }

    private int pathControl(World.Tile[][] board, boolean wumpusDead) {
        int[] startIndex = {0, 0};
        boolean isGoldAccessable = false;
        List<int[]> path;
        int points = 0;

        if (wumpusDead) {
            qw.add(startIndex);

            while (!qw.isEmpty() && !isGoldAccessable) {
                currentPosition = qw.remove();
                vw.add(currentPosition);

                if (board[currentPosition[0]][currentPosition[1]].getGold()) {
                    isGoldAccessable = true;
                    goldPosition = currentPosition;
                } else {
                    findNeighbours(board, qw, visited);
                }
            }

            if (isGoldAccessable) {
                path = findPath(vw);
                points = collectPoints(board, path);
            } else {
                points = Integer.MAX_VALUE;
            }

        } else {
            queue.add(startIndex);
            while (!queue.isEmpty() && !isGoldAccessable) {
                currentPosition = queue.remove();
                visited.add(currentPosition);

                if (board[currentPosition[0]][currentPosition[1]].getGold()) {
                    isGoldAccessable = true;
                    goldPosition = currentPosition;

                } else if (!board[currentPosition[0]][currentPosition[1]].getWumpus()) {
                    findNeighbours(board, queue, visited);
                }

            }
            if (isGoldAccessable) {
                path = findPath(visited);
                points = collectPoints(board, path);
            } else {
                points = Integer.MAX_VALUE;
            }
        }
        return points;
    }

    private int collectPoints(World.Tile[][] board, List<int[]> path) {
        int points = 0;
        int[] from = new int[]{-1, -1};
        int[] to = new int[]{-1, -1};

        for (int i = 0; i < path.size() - 1; i++) {
            from[0] = path.get(i)[0];
            from[1] = path.get(i)[1];
            to[0] = path.get(i + 1)[0];
            to[1] = path.get(i + 1)[1];

            //RIGHT
            if (from[0] == to[0] - 1) {
                switch (dir) {
                    case Down:

                    case Left:
                        points += 4;
                        break;

                    case Up:
                        points += 2;
                        break;

                    default:
                        break;
                }
                dir = Right;
            }
            //LEFT
            else if (from[0] == to[0] + 1) {
                switch (dir) {
                    case Down:

                    case Up:
                        points += 2;
                        break;

                    case Right:
                        points += 4;
                        break;

                    default:
                        break;
                }
                dir = Left;
            }
            //UP
            else if (from[1] == to[1] - 1) {
                switch (dir) {
                    case Left:

                    case Right:
                        points += 2;
                        break;

                    case Down:
                        points += 4;
                        break;

                    default:
                        break;
                }
                dir = Up;
            }

            //DOWN
            else if (from[1] == to[1] + 1) {
                switch (dir) {
                    case Right:

                    case Left:
                        points += 2;
                        break;

                    case Up:
                        points += 4;
                        break;

                    default:
                        break;
                }
                dir = Down;
            }
            if (board[to[0]][to[1]].getWumpus() && !wumpusDead) {
                points += 11;
            }
            points += 2;

        }
        dir = Right;
        return points;
    }

    private void proceed(int[] pos1, int[] pos2, World.Tile[][] board) {

        //RIGHT
        if (pos1[0] == pos2[0] - 1) {
            switch (dir) {
                case Down:
                    plan.add(Action.TURN_LEFT);
                    break;

                case Left:
                    plan.add(Action.TURN_LEFT);
                    plan.add(Action.TURN_LEFT);
                    break;

                case Up:
                    plan.add(Action.TURN_RIGHT);
                    break;
                default:
                    break;
            }
            dir = Right;
        }

        //LEFT
        else if (pos1[0] == pos2[0] + 1) {
            switch (dir) {

                case Right:
                    plan.add(Action.TURN_LEFT);
                    plan.add(Action.TURN_LEFT);
                    break;

                case Up:
                    plan.add(Action.TURN_LEFT);
                    break;

                case Down:
                    plan.add(Action.TURN_RIGHT);
                    break;
                default:
                    break;
            }
            dir = Left;
        }

        //UP
        else if (pos1[1] == pos2[1] - 1) {
            switch (dir) {
                case Right:
                    plan.add(Action.TURN_LEFT);
                    break;

                case Left:
                    plan.add(Action.TURN_RIGHT);
                    break;

                case Down:
                    plan.add(Action.TURN_LEFT);
                    plan.add(Action.TURN_LEFT);
                    break;

                default:
                    break;
            }
            dir = Up;
        }

        //DOWN
        else if (pos1[1] == pos2[1] + 1) {
            switch (dir) {

                case Right:
                    plan.add(Action.TURN_RIGHT);
                    break;
                case Left:
                    plan.add(Action.TURN_LEFT);
                    break;
                case Up:
                    plan.add(Action.TURN_LEFT);
                    plan.add(Action.TURN_LEFT);
                    break;

                default:
                    break;
            }
            dir = Down;
        }

        if (board[pos2[0]][pos2[1]].getWumpus() && !wumpusDead) {
            wumpusDead = true;
            plan.add(Action.SHOOT);
        }
        plan.add(Action.FORWARD);
    }

    private List<int[]> findPath(List<int[]> visitedPath) {
        List<int[]> path = new ArrayList<>();
        path.add(goldPosition);

        boolean isDone = false;
        int[] cp = goldPosition;

        while (!isDone) {

            boolean isFound = false;
            for (int i = 0; i < visitedPath.size() && !isFound; i++) {
                int[] ints = visitedPath.get(i);
                if (cp[0] == ints[0] && cp[1] == ints[1]) {
                    cp = new int[]{ints[2], ints[3]};
                    path.add(cp);
                    isFound = true;
                }
            }

            if (cp[0] == 0 && cp[1] == 0) {
                isDone = true;
            }
        }
        return path;
    }

    private void findNeighbours(World.Tile[][] board, Queue<int[]> queue, List<int[]> visited) {

        int[] nl = {currentPosition[0] - 1, currentPosition[1]};
        if (nl[0] >= 0 && !isVisitedOrInQueue(nl, queue, visited)
                && !board[nl[0]][nl[1]].getPit()) {
            queue.add(new int[]{nl[0], nl[1], currentPosition[0], currentPosition[1]});
        }

        int[] nr = {currentPosition[0] + 1, currentPosition[1]};
        if (nr[0] < (board.length) && !isVisitedOrInQueue(nr, queue, visited)
                && !board[nr[0]][nr[1]].getPit()) {
            queue.add(new int[]{nr[0], nr[1], currentPosition[0], currentPosition[1]});
        }

        int[] nu = {currentPosition[0], currentPosition[1] + 1};
        if (nu[1] < board[0].length && !isVisitedOrInQueue(nu, queue, visited) && !board[nu[0]][nu[1]].getPit()) {
            queue.add(new int[]{nu[0], nu[1], currentPosition[0], currentPosition[1]});
        }

        int[] nd = {currentPosition[0], currentPosition[1] - 1};
        if (nd[1] >= 0 && !isVisitedOrInQueue(nd, queue, visited) && !board[nd[0]][nd[1]].getPit()) {
            queue.add(new int[]{nd[0], nd[1], currentPosition[0], currentPosition[1]});
        }
    }

    @Override
    public Agent.Action getAction(boolean stench, boolean breeze, boolean glitter, boolean bump, boolean scream) {
        return planIterator.next();
    }
}