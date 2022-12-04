public class ServerMain {
    public static void main(String[] args){
        GameServer server = new GameServer(2333);
        server.serverBegin();
    }
}
