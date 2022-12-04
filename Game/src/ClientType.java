public enum ClientType {
    HOST(0),
    CLIENT(1),
    WATCHER(2);
    int i;
    ClientType(int i){
        this.i = i;
    }
    public static ClientType fromInt(int value){
        switch (value){
            case 0:
                return HOST;
            case 1:
                return CLIENT;
            case 2:
                return WATCHER;
        }
        return null;
    }
    public int toInt(){
        return i;
    }
}
