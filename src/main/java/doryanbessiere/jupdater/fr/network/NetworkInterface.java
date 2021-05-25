package doryanbessiere.jupdater.fr.network;

import java.io.IOException;

public abstract class NetworkInterface {

    public abstract void writeUTF(String value) throws IOException;
    public abstract void writeLong(long value) throws IOException;
    public abstract void writeInt(int value) throws IOException;
    public abstract void writeObject(Object object) throws IOException;

    public abstract String readUTF() throws IOException;
    public abstract long readLong() throws IOException;
    public abstract int readInt() throws IOException;
    public abstract Object readObject(Class<?> class_) throws IOException;

    public abstract void flush() throws IOException;
}
