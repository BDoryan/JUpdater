package doryanbessiere.jupdater.fr;

import doryanbessiere.jupdater.fr.listeners.JUpdaterListener;
import doryanbessiere.jupdater.fr.manifest.Manifest;
import doryanbessiere.jupdater.fr.manifest.ManifestFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class JUpdater {

    public static void log(String message){
        System.out.println("["+(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(System.currentTimeMillis())) +"] [JUpdater] "+message);
    }

    private int bufferLength = 1024*4; // 4 Mb
    private File base;
    private String version;
    private Manifest manifest;
    private ArrayList<JUpdaterListener> listeners = new ArrayList<>();

    private String[] ignoreFiles;

    public JUpdater(File base, String version,Manifest manifest) {
        this.base = base;
        this.version = version;
        this.manifest = manifest;
    }

    public void setIgnoreFiles(String... ignoreFiles) {
        this.ignoreFiles = ignoreFiles;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public int countFiles(){
        return countFiles(base);
    }

    public int getBufferLength() {
        return bufferLength;
    }

    public void setBufferLength(int bufferLength) {
        this.bufferLength = bufferLength;
    }

    public int countFiles(File directory){
        int count = 0;
        for(File file : directory.listFiles()){
            if(file.isDirectory()){
                count += countFiles(file);
            } else {
                count++;
            }
        }
        return count;
    }

    public ArrayList<JUpdaterListener> getListeners() {
        return listeners;
    }

    public File getBase() {
        return base;
    }

    public String getVersion() {
        return version;
    }
}
