package test;

import doryanbessiere.jupdater.fr.JUpdater;
import doryanbessiere.jupdater.fr.client.JUpdaterClient;
import doryanbessiere.jupdater.fr.manifest.Manifest;

import java.io.File;
import java.io.IOException;

public class Client {

    public static void main(String[] args) {
        Manifest manifest = getManifest();
        if(manifest == null)
            return;

        JUpdater jupdater = new JUpdater(new File("C:\\Users\\Doryan\\Documents\\Pétanque Manager\\Software\\base"), manifest.getVersion(), manifest);
        JUpdaterClient client = new JUpdaterClient(jupdater);
        client.connect("localhost", 222);
    }

    private static Manifest getManifest(){
        File directory = new File("C:\\Users\\Doryan\\Documents\\Pétanque Manager\\Software");
        File base = new File(directory, "base");
        File backup_directory = new File(directory, "backup");
        File manifestFile = new File(directory, "manifest.json");

        if(!base.exists())
            return null;

        if(!backup_directory.exists())
            backup_directory.mkdirs();

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
