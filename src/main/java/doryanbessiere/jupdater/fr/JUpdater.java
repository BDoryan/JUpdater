package doryanbessiere.jupdater.fr;

import java.io.File;

public class JUpdater {

    public static void log(String message){
        System.out.println("[JUpdater] "+message);
    }

    private File base;
    private String version;

    public JUpdater(File base, String version) {
        this.base = base;
        this.version = version;
    }

    public int countFiles(){
        return countFiles(base);
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

    public File getBase() {
        return base;
    }

    public String getVersion() {
        return version;
    }
}
