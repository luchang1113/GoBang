public enum Chess {
    BLACK(0),
    WHITE(1),
    EMPTY(2);
    int i;
    Chess(int i){
        this.i = i;
    }
    public static Chess fromInt(int value){
        switch (value){
            case 0:
                return BLACK;
            case 1:
                return WHITE;
            case 2:
                return EMPTY;
        }
        return null;
    }
    public int toInt(){
        return i;
    }
}
