package doryanbessiere.jupdater.fr.serials;

public class ConnectionResult {

    public static final int NEED_UPDATE = 2;
    public static final int CHECK_SUCCESS = 1;
    public static final int FAILED = 0;

    private int result;

    public ConnectionResult(int result) {
        this.result = result;
    }

    public int getResult() {
        return result;
    }
}
