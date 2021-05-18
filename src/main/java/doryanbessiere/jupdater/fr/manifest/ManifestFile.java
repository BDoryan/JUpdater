package doryanbessiere.jupdater.fr.manifest;

import java.io.File;

public class ManifestFile {

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

    @Override
    public String toString() {
        return "ManifestFile{" +
                "path='" + path + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
