package doryanbessiere.jupdater.fr.manifest;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public class ManifestFile implements Serializable {

    private String path;
    private String version;

    public ManifestFile(String path, String version) {
        this.path = path;
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ManifestFile{" +
                "path='" + path + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    public boolean equalsPath(ManifestFile file){
        return file.getPath().equals(this.path);
    }
}
