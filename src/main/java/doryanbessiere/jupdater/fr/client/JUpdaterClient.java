package doryanbessiere.jupdater.fr.client;

import doryanbessiere.jupdater.fr.JUpdater;

import java.io.*;
import java.net.Socket;

public class JUpdaterClient {

    private JUpdater jupdater;

    public JUpdaterClient(JUpdater jupdater) {
        this.jupdater = jupdater;
    }

    private Socket socket;
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;

    public void connect(String address, int port){
        try {
            this.socket = new Socket(address, port);
            JUpdater.log("Client connected on "+address+":"+port);

            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            int count_files = dataInputStream.readInt();
            receiveFiles(count_files);
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
            JUpdater.log("Download start (path="+path+", length"+length+", ("+ (i + 1)+"/"+count_files+")");

            File file = new File(jupdater.getBase(), path);
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            } else {
                if(file.exists()){
                    file.delete();
                }
            }
            file.createNewFile();

            FileOutputStream fileOutputStream = new FileOutputStream(file);

            int buffer_size = 8;

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

                JUpdater.log("Downloading : "+path+" ("+((long) file.length())+"/"+(length)+", "+(100 * (long) file.length() / length)+"%)");
                if(finish)
                    break;
            }
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
