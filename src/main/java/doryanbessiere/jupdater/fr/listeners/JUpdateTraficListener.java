package doryanbessiere.jupdater.fr.listeners;

public abstract class JUpdateTraficListener {

    public abstract void downloadProgress(String file, int file_index, double percent, long currently, long total);
    public abstract void downloadStart(String file, int file_index);
    public abstract boolean downloadFinish();

    public abstract void updateStart(int files_count);
    public abstract void updateFinish();

}
