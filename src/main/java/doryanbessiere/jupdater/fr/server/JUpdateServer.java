package doryanbessiere.jupdater.fr.server;

import doryanbessiere.jupdater.fr.JUpdater;
import doryanbessiere.jupdater.fr.commons.logger.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class JUpdateServer extends Thread {

    public static final Logger LOGGER = new Logger("JUpdateServer");

    private JUpdater jupdater;

    private int port;
    private ServerSocket socket;

    public JUpdateServer(JUpdater jupdater, int port) {
        this.jupdater = jupdater;
        this.port = port;
    }

    @Override
    public void run() {
        super.run();
        try {
            this.socket = new ServerSocket(port);
            Logger.getInstance().info("Starting JUpdateServer on port : "+port);
            Logger.getInstance().info("Waiting clients...");
            while(!this.socket.isClosed()){
                Socket socket = this.socket.accept();
                Logger.getInstance().info("Client connect: "+socket.getInetAddress().getHostAddress()+":"+socket.getPort());
                connect(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect(Socket socket){
        new JUpdateRemoteClient(this, socket).start();
    }

    public JUpdater getJUpdater() {
        return jupdater;
    }

    public static final Logger getLogger(){
        return LOGGER;
    }
}
