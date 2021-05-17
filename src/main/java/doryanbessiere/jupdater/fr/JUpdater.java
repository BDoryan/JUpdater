package doryanbessiere.jupdater.fr;

import java.io.File;

public class JUpdater {

    private File base;
    private String version;
    private String[] ignored;

    public JUpdater(File base, String version) {
        this.base = base;
        this.version = version;
    }

    public String[] getIgnored() {
        return ignored;
    }

    /**
     *
     * Permet de définir les fichiers à ignorés lors des sauvegardes de sécurité
     * (pour éviter la perte de fichier lors d'une MAJ).
     *
     * @param ignored
     */
    public void setIgnored(String... ignored) {
        this.ignored = ignored;
    }

    public File getBase() {
        return base;
    }

    public String getVersion() {
        return version;
    }

}
