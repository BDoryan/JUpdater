package doryanbessiere.jupdater.fr.server.remoteclient;

import doryanbessiere.jupdater.fr.JUpdater;
import doryanbessiere.jupdater.fr.manifest.Manifest;
import doryanbessiere.jupdater.fr.network.Network;
import doryanbessiere.jupdater.fr.serials.ManifestObject;
import doryanbessiere.jupdater.fr.server.JUpdaterServer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class JUpdaterRemoteClient extends Thread {

    private JUpdaterServer server;

    private Socket socket;
    private Network network;

    public JUpdaterRemoteClient(JUpdaterServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            this.network = new Network(socket.getInputStream(), socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            JUpdater.log("Client connected : "+socket.getInetAddress().getHostAddress()+":"+socket.getPort());
            accept();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("["+socket.getInetAddress().getHostAddress()+":"+socket.getPort()+"] Connection closed : "+e.getMessage());
        }
    }

    public void accept() throws IOException, ClassNotFoundException {
        JUpdater.log("Waiting for the client's version");
        if(!network.readUTF().equalsIgnoreCase(server.getJUpdater().getVersion())){
            server.getJUpdater().getListeners().forEach(listener -> listener.connection(this, server.getJUpdater().getVersion()));
            network.writeInt(Network.NEED_UPDATE);

            JUpdater.log("Waiting for the client's manifest file");
            ManifestObject manifestObject = (ManifestObject) network.readObject(ManifestObject.class);

            sendUpdate(manifestObject);
            socket.close();
        } else {
            server.getJUpdater().getListeners().forEach(listener -> listener.connection(this, server.getJUpdater().getVersion()));
            network.writeInt(Network.VERSION_OK);
        }
        JUpdater.log("Socket ("+socket.getInetAddress().getHostAddress()+":"+socket.getPort()+") kicked !");
    }

    public void sendUpdate(ManifestObject manifestObject) throws IOException {
        server.getJUpdater().getListeners().forEach(listener -> listener.update(this, server.getJUpdater().getVersion()));
        Manifest serverManifest = server.getJUpdater().getManifest();
        ArrayList<String> paths = serverManifest.compare(manifestObject);

        JUpdater.log("File recovery, start checking for out-of-date files");
        network.writeInt(paths.size());

        JUpdater.log("The client has "+paths.size()+" files to update");

        ArrayList<File> files = new ArrayList<>();
        for(String path : paths){
            File file = new File(server.getJUpdater().getBase(), path);
            files.add(file);
        }

        JUpdater.log("Sending updated files...");
        sendFiles(files);

        JUpdater.log("Sending the updated manifest file");
        manifestObject.setVersion(server.getJUpdater().getVersion());

        manifestObject.getFiles().clear();
        manifestObject.getFiles().addAll(serverManifest.getFiles());

        network.writeObject(manifestObject);
        JUpdater.log("Update completed, good bye!");
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

    public void sendFiles(ArrayList<File> files){
        files.forEach(file -> sendFile(file));
    }

    public void sendFile(File file){
        try {
            JUpdater.log("sendFile("+file.getPath()+");");
            String path = file.getPath().replace(server.getJUpdater().getBase().getPath(), "");
            long file_length = (long) file.length();
            long length_sended = 0l;

            network.writeUTF(path);
            network.writeLong(file_length);

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
                if((read = fileInputStream.read(buffer)) < 0){
                    break;
                }
                outputStream.write(buffer);
                length_sended+= read;
                JUpdater.log("Uploading: read="+read+" "+length_sended+"/"+file_length);

                if(finish) {
                    //network.writeInt(Network.FILE_SEND_FINISH);
                    break;
                }
            }
            fileInputStream.close();
            outputStream.flush();
            JUpdater.log("finish();");

            JUpdater.log("Waiting for the client to download...");
            if(network.readInt() == Network.DOWNLOAD_FINISH){
                JUpdater.log("The client is finally ready to download the next file.");
            } else {
                throw new IllegalStateException("The customer cannot continue the download!");
            }
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
