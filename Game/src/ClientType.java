public enum ClientType {
    MASTER(0),
    SLAVE(1),
    WATCHER(2);
    int i;
    private ClientType(int i){
        this.i = i;
    }
    public static ClientType fromInt(int value){
        switch (value){
            case 0:
                return MASTER;
            case 1:
                return SLAVE;
            case 2:
                return WATCHER;
        }
        return null;
    }
    public int toInt(){
        return i;
    }
}
