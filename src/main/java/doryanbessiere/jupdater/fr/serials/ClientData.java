package doryanbessiere.jupdater.fr.serials;

import java.io.Serializable;

public class ClientData implements Serializable {

    private String version;

    public ClientData(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
