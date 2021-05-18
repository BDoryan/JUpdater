package doryanbessiere.jupdater.fr.client;

import doryanbessiere.jupdater.fr.JUpdater;
import doryanbessiere.jupdater.fr.listeners.JUpdaterClientTrafic;
import doryanbessiere.jupdater.fr.manifest.Manifest;
import doryanbessiere.jupdater.fr.manifest.ManifestFile;
import doryanbessiere.jupdater.fr.network.Network;
import doryanbessiere.jupdater.fr.serials.ManifestObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class JUpdaterClient {

    private JUpdater jupdater;

    public JUpdaterClient(JUpdater jupdater) {
        this.jupdater = jupdater;
    }

    private Socket socket;
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;

    private ArrayList<JUpdaterClientTrafic> traficListeners = new ArrayList<>();

    public ArrayList<JUpdaterClientTrafic> getTraficListeners() {
        return traficListeners;
    }

    public void connect(String address, int port){
        try {
            this.socket = new Socket(address, port);
            JUpdater.log("Client connected on "+address+":"+port);

            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            JUpdater.log("Sending the customer's version.");
            dataOutputStream.writeUTF(jupdater.getVersion());
            dataOutputStream.flush();

            JUpdater.log("Waiting for server response...");
            if(dataInputStream.readInt() == Network.NEED_UPDATE){
                JUpdater.log("Update required, send your manifest file.");
                Manifest manifest = jupdater.getManifest();

                dataOutputStream.writeUTF(new ManifestObject(manifest).toJson());
                dataOutputStream.flush();

                int count_files = dataInputStream.readInt();
                JUpdater.log("You have "+count_files+" files to update.");
                traficListeners.forEach(listener -> listener.updateStart(count_files));

                JUpdater.log("Receiving files....");
                receiveFiles(count_files);
                JUpdater.log("Recovery complete, update manifest file");

                ManifestObject manifestObject = ManifestObject.fromJson(dataInputStream.readUTF());

                for(ManifestFile manifestFile : manifest.getFiles()){
                    boolean found = false;
                    for(ManifestFile newManifestFile : manifestObject.getFiles()){
                        if(manifestFile.equalsPath(newManifestFile)){
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        File file = new File(jupdater.getBase(), manifestFile.getPath());
                        if(file.exists())
                            file.delete();
                    }
                }
                manifest.setFiles(manifestObject.getFiles());
                manifest.setVersion(manifestObject.getVersion());
                manifest.save();
                JUpdater.log("Update completed");

                traficListeners.forEach(listener -> listener.updateFinish());
            } else {
                JUpdater.log("You are already up to date! Good bye.");
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveFiles(int count_files) throws IOException {
        InputStream inputStream = socket.getInputStream();
        JUpdater.log("Downloading in progress, resolve "+count_files+" files...");

        for(int i = 0;i < count_files; i++){
            String path = dataInputStream.readUTF();
            long length = dataInputStream.readLong();
            JUpdater.log("Download start (path="+path+", length="+length+") ("+ (i + 1)+"/"+count_files+")");

            File file = new File(jupdater.getBase(), path);

            int finalI = i;
            traficListeners.forEach(listener -> listener.downloadStart(file, (finalI + 1), count_files));
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            } else {
                if(file.exists()){
                    file.delete();
                }
            }
            file.createNewFile();

            FileOutputStream fileOutputStream = new FileOutputStream(file);

            int buffer_size = jupdater.getBufferLength();

            while(true){
                int size = buffer_size;
                boolean finish = false;
                if(file.length() + buffer_size > length){
                    size = (int) (length - file.length());
                    finish = true;
                }

                byte[] buffer = new byte[size];
                int read = 0;
                if((read = inputStream.read(buffer, 0, buffer.length)) < 0){
                    break;
                }
                fileOutputStream.write(buffer, 0, buffer.length);
                fileOutputStream.flush();
                traficListeners.forEach(listener -> listener.download(file, path, file.length(), length, (finalI + 1), count_files));

                JUpdater.log("Downloading : "+path+" ("+((long) file.length())+"/"+(length)+", "+(100 * (long) file.length() / length)+"%)");
                if(finish)
                    break;
            }
            traficListeners.forEach(listener -> listener.downloadFinish(file));
            JUpdater.log("Download finish.");
            fileOutputStream.close();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public JUpdater getJUpdater() {
        return jupdater;
    }
}
