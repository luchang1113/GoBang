import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class RoomServer {
    private final ServerSocket server;
    private final List<GameServer> rooms = new LinkedList<>();

    RoomServer() throws IOException {
        server = new ServerSocket(2333);

    }

    void SetupNewRoom(){
        rooms.add(new GameServer(2334+rooms.size()));
    }

}
