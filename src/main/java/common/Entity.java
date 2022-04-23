package common;

public enum Entity {
    WUMPUS('W'),
    PIT('P'),
    GOLD('G'),
    BREEZE('B'),
    STENCH('S'),
    WALL('L'),
    SAFE('N'),
    VISITED('V');

    char prop;

    Entity(char prop) {
        this.prop = prop;
    }

   public char idchar() {
        return prop;
    }

    public String idstr() {
        return String.valueOf(prop);
    }
}
