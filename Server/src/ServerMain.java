public class ServerMain {
    public static void main(String[] args){
        GameServer server = new GameServer();
        server.initServer();
        server.serverBegin();
    }
}
