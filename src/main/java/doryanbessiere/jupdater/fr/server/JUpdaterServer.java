package doryanbessiere.jupdater.fr.server;

import doryanbessiere.jupdater.fr.JUpdater;
import doryanbessiere.jupdater.fr.server.remoteclient.JUpdaterRemoteClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class JUpdaterServer {

    private JUpdater jupdater;
    private int port;

    public JUpdaterServer(JUpdater jupdater, int port) {
        this.jupdater = jupdater;
        this.port = port;
    }

    private ServerSocket serverSocket;

    public void start(){
        try {
            serverSocket = new ServerSocket(port);
            JUpdater.log("Server started in port "+port);
            JUpdater.log("Waiting clients...");
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                JUpdaterRemoteClient remoteClient = new JUpdaterRemoteClient(this, socket);
                remoteClient.start();
                JUpdater.log("Client connected : "+socket.getInetAddress().getHostAddress()+":"+socket.getPort());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    public JUpdater getJUpdater() {
        return jupdater;
    }
}
