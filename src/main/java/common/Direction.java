package common;

public enum Direction {

    NoDirection(0),
    Right(1), // RIGHT (0)
    Down(2), //DOWN  (1)
    Left(3), // LEFT  (2)
    Up(4); // UP   (3)


    private int dir;

     private Direction(int dir) {
        this.dir = dir;
    }

    public int getDir() {
        return dir;
    }
}
