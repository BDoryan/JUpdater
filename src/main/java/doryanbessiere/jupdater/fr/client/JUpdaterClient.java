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
    private Network network;

    private ArrayList<JUpdaterClientTrafic> traficListeners = new ArrayList<>();

    public ArrayList<JUpdaterClientTrafic> getTraficListeners() {
        return traficListeners;
    }

    public void connect(String address, int port) throws IOException, ClassNotFoundException {
            this.socket = new Socket(address, port);
            this.network = new Network(this.socket.getInputStream(), this.socket.getOutputStream());
            JUpdater.log("Client connected on "+address+":"+port);

            JUpdater.log("Sending the customer's version.");
            network.writeUTF(jupdater.getVersion());

            JUpdater.log("Waiting for server response...");
            int result = network.readInt();
            if(result == Network.NEED_UPDATE){
                JUpdater.log("Update required, send your manifest file.");
                Manifest manifest = jupdater.getManifest();

                network.writeObject(new ManifestObject(manifest));

                int count_files = network.readInt();
                JUpdater.log("You have "+count_files+" files to update.");
                traficListeners.forEach(listener -> listener.updateStart(count_files));

                JUpdater.log("Receiving files....");
                receiveFiles(count_files);
                JUpdater.log("Recovery complete, update manifest file");

                ManifestObject manifestObject = (ManifestObject) network.readObject(ManifestObject.class);

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
                traficListeners.forEach(listener -> listener.upToDate());
                JUpdater.log("You are already up to date! Good bye.");
                socket.close();
            }
    }

    public void receiveFiles(int count_files) throws IOException, ClassNotFoundException {
        InputStream inputStream = socket.getInputStream();
        JUpdater.log("Downloading in progress, resolve "+count_files+" files...");

        for(int i = 0;i < count_files; i++){
            String path = network.readUTF();
            long length = network.readLong();
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

            long current = 0l;
            byte[] fileBuffer = new byte[(int) length];

            while(true){
                int read = inputStream.read(fileBuffer, (int) current, (int) (fileBuffer.length - current));
                if(read > 0){
                    current += read;
                    long finalCurrent = current;
                    traficListeners.forEach(listener -> listener.download(file, path, (int) finalCurrent, length, (finalI + 1), count_files));

                    JUpdater.log("Downloading : read="+read+" "+path+" ("+((long) current)+"/"+(length)+", "+(100 * (long) current / length)+"%)");
                } else {
                    break;
                }
            }
            fileOutputStream.write(fileBuffer);
            fileOutputStream.flush();
            traficListeners.forEach(listener -> listener.downloadFinish(file));
            JUpdater.log("Download finish.");
            network.writeInt(Network.DOWNLOAD_FINISH);
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
