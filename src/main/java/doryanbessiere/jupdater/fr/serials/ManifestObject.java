package doryanbessiere.jupdater.fr.serials;

import doryanbessiere.jupdater.fr.manifest.Manifest;
import doryanbessiere.jupdater.fr.manifest.ManifestFile;

import java.io.Serializable;
import java.util.ArrayList;

public class ManifestObject implements Serializable {

    private String version;
    private ArrayList<ManifestFile> files = new ArrayList<>();

    public ManifestObject(Manifest manifest) {
        this.version = manifest.getVersion();
        this.files = manifest.getFiles();
    }

    public ManifestObject(String version, ArrayList<ManifestFile> files) {
        this.version = version;
        this.files = files;
    }

    public String toJson(){
        return Manifest.GSON.toJson(this);
    }

    public static ManifestObject fromJson(String json){
        return Manifest.GSON.fromJson(json, ManifestObject.class);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public ArrayList<ManifestFile> getFiles() {
        return files;
    }
}
