package test;

import doryanbessiere.jupdater.fr.JUpdater;
import doryanbessiere.jupdater.fr.manifest.Manifest;
import doryanbessiere.jupdater.fr.server.JUpdaterServer;

import java.io.File;
import java.io.IOException;

public class Server {

    public static void main(String[] args) {
        String version = "1.0.5";
        Manifest manifest = getManifest(version);
        if(manifest == null)
            return;

        JUpdater jupdater = new JUpdater(new File("C:\\Users\\Doryan\\Documents\\Pétanque Manager\\JUpdaterServer\\base"), version, manifest);
        JUpdaterServer server = new JUpdaterServer(jupdater, 222);
        server.start();
    }

    private static Manifest getManifest(String version){
        File directory = new File("C:\\Users\\Doryan\\Documents\\Pétanque Manager\\JUpdaterServer");
        File base = new File(directory, "base");
        File new_version = new File(directory, "new");
        File backup_directory = new File(directory, "backup");
        File manifestFile = new File(directory, "manifest.json");

        if(!base.exists())
            return null;

        if(!backup_directory.exists())
            backup_directory.mkdirs();
        if(!new_version.exists())
            new_version.mkdirs();

        Manifest.debug = true;

        Manifest manifest = null;
        if(!manifestFile.exists()) {
            try {
                manifest = new Manifest(manifestFile);
                if(manifest.initFiles(base, base, version)){
                    System.out.println("Init manifest success!");
                } else {
                    System.out.println("Init manifest failed!");
                }
                manifest.getFiles().forEach(file -> System.out.println(file.toString()));
                manifest.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else  {
            manifest = Manifest.readManifest(manifestFile);
            if(new_version.listFiles().length <= 0)
                return manifest;

            try {
                if(manifest.updateFiles(backup_directory, base, new_version, version)){
                    System.out.println("Update success!");
                } else {
                    System.out.println("Update failed!");
                }
                manifest.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return manifest;
    }
}
