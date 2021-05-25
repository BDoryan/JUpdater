package test;

import doryanbessiere.jupdater.fr.JUpdater;
import doryanbessiere.jupdater.fr.client.JUpdaterClient;
import doryanbessiere.jupdater.fr.listeners.JUpdaterClientTrafic;
import doryanbessiere.jupdater.fr.manifest.Manifest;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Client {

    private static File directory = new File("C:\\Users\\Doryan\\Documents\\Pétanque Manager\\Software");
    private static File base = new File(directory, "base");

    public static void main(String[] args) {
        Manifest manifest = getManifest();
        if(manifest == null)
            return;

        JUpdater jupdater = new JUpdater(new File("C:\\Users\\Doryan\\Documents\\Pétanque Manager\\Software\\base"), manifest.getVersion(), manifest);
        JUpdaterClient client = new JUpdaterClient(jupdater);
        client.getTraficListeners().add(new JUpdaterClientTrafic() {
            @Override
            public void upToDate() {

            }

            @Override
            public void updateStart(int total_files) {
                File backup_directory = new File(directory, "backup");

                if(!backup_directory.exists())
                    backup_directory.mkdirs();

                File backup = null;
                int i = 0;
                while(true){
                    backup = new File(backup_directory, i == 0 ? manifest.getVersion() : manifest.getVersion()+"_"+i);
                    if(!backup.exists())break;
                    i++;
                }
                backup.mkdirs();

                try {
                    FileUtils.copyDirectory(base, backup);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void updateFinish() {
            }

            @Override
            public void downloadStart(File file, int file_index, int total_files) {

            }

            @Override
            public void download(File file, String path, long current, long total, int file_index, int total_files) {

            }

            @Override
            public void downloadFinish(File file) {

            }
        });
        try {
            client.connect("localhost", 222);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Manifest getManifest(){
        File manifestFile = new File(directory, "manifest.json");

        if(!base.exists())
            return null;

        Manifest.debug = true;

        Manifest manifest = null;
        if(!manifestFile.exists()) {
            try {
                manifest = new Manifest(manifestFile);
                if(manifest.initFiles(base, base, "local")){
                    System.out.println("Init manifest success!");
                } else {
                    System.out.println("Init manifest failed!");
                }
                manifest.getFiles().forEach(file -> System.out.println(file.toString()));
                manifest.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            manifest = Manifest.readManifest(manifestFile);
        }
        return manifest;
    }
}
