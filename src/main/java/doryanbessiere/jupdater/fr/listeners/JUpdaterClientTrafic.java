package doryanbessiere.jupdater.fr.listeners;

import java.io.File;

public abstract class JUpdaterClientTrafic {

    public abstract void updateStart(int total_files);
    public abstract void updateFinish();

    public abstract void upToDate();

    public abstract void downloadStart(File file, int file_index, int total_files);
    public abstract void download(File file, String path, long current, long total, int file_index, int total_files);
    public abstract void downloadFinish(File file);

}
