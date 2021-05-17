package doryanbessiere.jupdater.fr.server.remoteclient;

import doryanbessiere.jupdater.fr.JUpdater;
import doryanbessiere.jupdater.fr.network.Network;
import doryanbessiere.jupdater.fr.server.JUpdaterServer;

import java.io.*;
import java.net.Socket;

public class JUpdaterRemoteClient extends Thread {

    private JUpdaterServer server;
    private Socket socket;

    public JUpdaterRemoteClient(JUpdaterServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        super.run();
        try {
            accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;

    public void accept() throws IOException {
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());

        if(!dataInputStream.readUTF().equalsIgnoreCase(server.getJUpdater().getVersion())){
            dataOutputStream.writeInt(Network.NEED_UPDATE);
            dataOutputStream.flush();

            dataOutputStream.writeInt(server.getJUpdater().countFiles());
            dataOutputStream.flush();

            sendDirectory(server.getJUpdater().getBase());
        } else {
            dataOutputStream.writeInt(Network.VERSION_OK);
        }
        JUpdater.log("Socket ("+socket.getInetAddress().getHostAddress()+":"+socket.getPort()+") kicked !");
    }

    public void sendDirectory(File directory){
        JUpdater.log("sendDirectory("+directory.getPath()+");");
        for(File file : directory.listFiles()){
            if(file.isDirectory()){
                sendDirectory(file);
            } else {
                sendFile(file);
            }
        }
    }

    public void sendFile(File file){
        try {
            JUpdater.log("sendFile("+file.getPath()+");");
            String path = file.getPath().replace(server.getJUpdater().getBase().getPath(), "");
            long file_length = (long) file.length();
            long length_sended = 0l;

            dataOutputStream.writeUTF(path);
            dataOutputStream.writeLong(file_length);
            dataOutputStream.flush();

            FileInputStream fileInputStream = new FileInputStream(file);
            OutputStream outputStream = socket.getOutputStream();

            int buffer_size = server.getJUpdater().getBufferLength();

            while(true){
                int size = buffer_size;
                boolean finish = false;

                if(length_sended + buffer_size > file_length){
                    size = (int) (file_length - length_sended);
                    finish = true;
                }
                byte[] buffer = new byte[size];
                int read = 0;
                if((read = fileInputStream.read(buffer, 0, buffer.length)) < 0){
                    break;
                }
                outputStream.write(buffer, 0, buffer.length);
                outputStream.flush();
                length_sended+= read;

                if(finish)
                    break;
            }
            fileInputStream.close();
            JUpdater.log("finish();");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public JUpdaterServer getServer() {
        return server;
    }
}
