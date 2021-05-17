package doryanbessiere.jupdater.fr.server;

import doryanbessiere.jupdater.fr.serials.ClientData;
import doryanbessiere.jupdater.fr.serials.ConnectionResult;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class JUpdateRemoteClient extends Thread {

    private JUpdateServer server;
    private Socket socket;

    public JUpdateRemoteClient(JUpdateServer server, Socket socket) {
        this.server =server;
        this.socket = socket;
    }

    @Override
    public void run() {
        super.run();

        try {
            ObjectOutputStream writer = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());

            ClientData data = (ClientData) reader.readObject();

            if(!data.getVersion().equals(server.getJUpdater().getVersion())){
                writer.writeObject(new ConnectionResult(ConnectionResult.NEED_UPDATE));
                sendUpdate(server.getJUpdater().getBase());
            } else {
                writer.writeObject(new ConnectionResult(ConnectionResult.CHECK_SUCCESS));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public int countFiles(File from){
        int count = 0;
        for(File file : from.listFiles()){
            if(!file.isDirectory())
                count++;
            else
                count += countFiles(file);
        }
        return count;
    }

    public void sendUpdate(File directory) throws IOException {
        DataOutputStream ps = new DataOutputStream(socket.getOutputStream());
        ps.writeInt(countFiles(directory));
        ps.flush();

        for(File file : directory.listFiles()){
            DataInputStream fis = new DataInputStream(new FileInputStream(file));

            String path = file.getPath().replace(server.getJUpdater().getBase().getPath(), "").substring(1);
            if(file.isDirectory()){
                sendUpdate(file);
            } else {
                ps.writeUTF(path);
                ps.flush();
                ps.writeLong((long) file.length());
                ps.flush();

                int bufferSize = 8192;
                byte[] buf = new byte[bufferSize];

                while (true) {
                    int read = 0;
                    if (fis != null) {
                        read = fis.read(buf);
                    }

                    ps.write(buf, 0, read);
                    if (read == -1) {
                        break;
                    }
                }
                ps.flush();
            }
            fis.close();
        }
        ps.close();
        socket.close();
    }

    public JUpdateServer getServer() {
        return server;
    }

    public Socket getSocket() {
        return socket;
    }
}
