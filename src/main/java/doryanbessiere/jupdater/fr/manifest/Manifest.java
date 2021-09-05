package doryanbessiere.jupdater.fr.manifest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import doryanbessiere.jupdater.fr.manifest.ManifestFile;
import doryanbessiere.jupdater.fr.serials.ManifestObject;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;

public class Manifest implements Serializable {

    public static final Gson GSON = new GsonBuilder().create();

    private String filePath;
    private String version;
    private ArrayList<ManifestFile> files = new ArrayList<>();

    public Manifest(File file) {
        this.filePath = file.getPath();
    }

    public boolean save() throws IOException {
        File file = getFile();
        if(file.exists()){
            if(!file.delete())
                return false;
        }
        file.createNewFile();

        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(GSON.toJson(this));
        fileWriter.flush();
        fileWriter.close();

        return true;
    }

    public boolean initFiles(File base, File directory, String version){
        this.version = version;
        for(File file : directory.listFiles(filter)){
            if(file.isDirectory()){
                initFiles(base, file, version);
            } else {
                String path = file.getPath().replace(base.getPath(), "");
                ManifestFile manifestFile = new ManifestFile(path, version);
                files.add(manifestFile);
                debug("Detection of the file ("+manifestFile.toString()+"), definition of its version : "+version);
            }
        }
        return true;
    }

    /**
     *
     * - Application du nouveau fichier manifest
     * - Lancement de la nouvelle copie dans le dossier 'base'
     *
     * @param base_directory set the base directory (old version directory)
     * @param new_base_directory set the new directory (new version directory)
     * @param new_version set the new version
     * @return true=success, false=error
     */
    public boolean updateFiles(File backup_directory, File base_directory, File new_base_directory, String new_version){
        if(!(base_directory.exists() && base_directory.isDirectory() && new_base_directory.exists() && new_base_directory.isDirectory())){
            error("File not existing or else they are not back");
            return false;
        }

        String old_version = version;

        ArrayList<ManifestFile> manifestFiles = new ArrayList<>();
        manifestFiles.addAll(getFiles());

        for(ManifestFile manifestFile : manifestFiles){
            String path = manifestFile.getPath();

            File oldFile = new File(base_directory, path);
            File newFile = new File(new_base_directory, path);
            if(newFile.exists()){
                try {
                    if(FileUtils.contentEquals(oldFile, newFile)){
                        debug(path+" : File still exists, and no changes applied.");
                    } else {
                        files.remove(manifestFile);
                        files.add(new ManifestFile(path, new_version));
                        debug(path+" : File still exists, but modification detected.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                files.remove(manifestFile);
                debug(manifestFile.getPath()+" : Remove file from the new version");
            }
        }

        for(File file : scanFiles(new_base_directory)){
            String path = file.getPath().replace(new_base_directory.getPath(), "");
            File oldFile = new File(base_directory, path);

            if(!oldFile.exists()){
                files.add(new ManifestFile(path, new_version));
                debug(path+" : New file detected in the new version.");
            }
        }

        try {
            if(backup_directory != null){
                File backup = null;
                int i = 0;
                while(true){
                    backup = new File(backup_directory, i == 0 ? old_version : old_version+"_"+i);
                    if(!backup.exists())break;
                    i++;
                }
                backup.mkdirs();

                FileUtils.copyDirectory(base_directory, backup);
                debug("Setting up the backup of the old version...");
            }
            FileUtils.cleanDirectory(base_directory);
            debug("Removal of the old version.");
            FileUtils.copyDirectory(new_base_directory, base_directory);
            FileUtils.cleanDirectory(new_base_directory);
            debug("Copy of the new version.");

            this.version = version;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private FileFilter filter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return !pathname.getName().equals("manifest.json");
        }
    };

    private ArrayList<File> scanFiles(File directory){
        ArrayList<File> files = new ArrayList<>();
        for(File file : directory.listFiles(filter)){
            if(file.isDirectory())
                files.addAll(scanFiles(file));
            else
                files.add(file);
        }
        return files;
    }

    public ArrayList<ManifestFile> getFiles() {
        return files;
    }

    public static Manifest readManifest(File file){
        if(file.exists()){
            try {
                FileReader reader = new FileReader(file);
                Manifest manifest = GSON.fromJson(reader, Manifest.class);
                reader.close();
                return manifest;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public ArrayList<String> compare(ManifestObject manifestObject) {
        ArrayList<String> updates = new ArrayList<>();
        /**
         * Analyse des fichiers nouveau ou modifier
         */
        for(ManifestFile serverManifestFile : files){
            ManifestFile found = null;
            for(ManifestFile clientManifestFile : manifestObject.getFiles()){
                debug("Check SERVER["+serverManifestFile.toString()+"] -> CLIENT["+clientManifestFile.toString()+"]");
                if(serverManifestFile.equalsPath(clientManifestFile)){
                    found = clientManifestFile;
                    break;
                }
            }

            /**
             * Fichier détecté, vérication voir si celui-ci est ancien
             */
            if(found != null){
                debug(found.getPath()+" : Check if need update to this ManifestFile");
                if(!serverManifestFile.getVersion().equals(found.getVersion())){
                    updates.add(serverManifestFile.getPath());
                    found.setVersion(version);
                }
            } else {
                /**
                 * Fichier non trouver, donc ajout de celui-ci
                 */
                debug(serverManifestFile.getPath()+" : new ManifestFile");
                updates.add(serverManifestFile.getPath());
                manifestObject.getFiles().add(serverManifestFile);
            }
        }

        /**
         * Analyse des fichiers supprimées
         */
        ArrayList<ManifestFile> copy = new ArrayList<>();
        copy.addAll(manifestObject.getFiles());
        for(ManifestFile clientManifestFile : copy){
            ManifestFile found = null;
            for(ManifestFile serverManifestFile : files){
                if(serverManifestFile.equalsPath(clientManifestFile)){
                    found = serverManifestFile;
                }
            }
            if(found == null){
                manifestObject.getFiles().remove(clientManifestFile);
            }
        }
        return updates;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public File getFile(){
        return new File(filePath);
    }

    public void setFiles(ArrayList<ManifestFile> files) {
        this.files = files;
    }

    public String getVersion() {
        return version;
    }

    public static boolean debug = false;

    public static void setDebug(boolean debug) {
        Manifest.debug = debug;
    }

    public static void debug(String message){
        if(!debug)return;
        System.out.println("[MANIFEST:debug] "+message);
    }

    public static void error(String message){
        System.out.println("[MANIFEST:error] "+message);
    }
}
