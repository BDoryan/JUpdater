package doryanbessiere.jupdater.fr.client;

import doryanbessiere.jupdater.fr.JUpdater;
import doryanbessiere.jupdater.fr.commons.logger.Logger;
import doryanbessiere.jupdater.fr.listeners.JUpdateTraficListener;
import doryanbessiere.jupdater.fr.serials.ClientData;
import doryanbessiere.jupdater.fr.serials.ConnectionResult;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Properties;

public class JUpdateClient extends Thread {

    private JUpdater jupdater;
    private Socket socket;

    private ArrayList<JUpdateTraficListener> listeners = new ArrayList<>();

    public JUpdateClient(JUpdater jupdater) {
        this.jupdater = jupdater;
    }

    public ArrayList<JUpdateTraficListener> getListeners() {
        return listeners;
    }

    public void connect(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        start();
    }

    @Override
    public void run() {
        super.run();

        try {
            ObjectOutputStream writer = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());

            writer.writeObject(new ClientData(jupdater.getVersion()));
            ConnectionResult result = (ConnectionResult) reader.readObject();
            if(result.getResult() == ConnectionResult.CHECK_SUCCESS){
                stop();
            } else if(result.getResult() == ConnectionResult.NEED_UPDATE){
                update(jupdater.getIgnored());
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

    public boolean update(String... ignored) throws IOException {
        File old_version_directory = new File(jupdater.getBase().getPath()+"/backup", jupdater.getVersion());
        if(old_version_directory.exists())
            if(!old_version_directory.delete())return false;

        old_version_directory.mkdirs();

        if(!copyContentsTo(old_version_directory, ignored))
            return false;

        if(!downloadUpdate())
            return false;

        return true;
    }

    public boolean copyContentsTo(File to, String... ignored) throws IOException {
        for(File file : jupdater.getBase().listFiles()) {
            String path = file.getPath().replace(jupdater.getBase().getPath(), "").substring(1);

            boolean mustIgnore = false;
            for(String ignore : ignored){
                if(path.startsWith(ignore))
                    mustIgnore= true;
            }
            if(!mustIgnore){
                File copyFile = new File(to, path);

                if (file.isDirectory()){
                    copyFile.mkdirs();
                    if(!copyContentsTo(file))return false;
                } else {
                    copyFile.createNewFile();
                    Files.copy(Paths.get(file.toURI()), Paths.get(copyFile.toURI()), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        return true;
    }

    public boolean downloadUpdate() throws IOException {
        DataInputStream inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        int files_count = inputStream.readInt();
        listeners.forEach(listener -> listener.updateStart(files_count));

        for(int i = 0; i < files_count; i++){
            int bufferSize = 8192;
            byte[] buf = new byte[bufferSize];
            long passedlen = 0;
            long len = 0;

            File file = new File(jupdater.getBase(), inputStream.readUTF());
            if(file.exists()){
                if(file.delete()) {
                    System.err.println("Failed to delete file : "+file.getPath());
                    return false;
                }
            }

            File parent = file.getParentFile();
            if(!parent.exists()){
                parent.mkdirs();
            }
            file.createNewFile();

            DataOutputStream fileOut = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(file)));
            len = inputStream.readLong();

            while (true) {
                int read = 0;
                if (inputStream != null) {
                    read = inputStream.read(buf);
                }
                if (read == -1) {
                    break;
                }
                passedlen += read;

                double percent = (passedlen * 100 / len);
                // The following progress bar is made for the prograssBar of the graphical interface. If you are typing a file, you may repeat the same percentage.
                fileOut.write(buf, 0, read);

                long finalPassedlen = passedlen;
                long finalLen = len;
                int finalI = i;
                listeners.forEach(listener -> listener.downloadProgress(file.getPath(), (finalI + 1), percent, finalPassedlen, finalLen));
            }
            fileOut.close();
        }
        listeners.forEach(listener -> listener.updateFinish());

        return true;
    }

    public JUpdater getJUpdater() {
        return jupdater;
    }
}
