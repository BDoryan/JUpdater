package doryanbessiere.jupdater.fr.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Network extends NetworkInterface {

    public static final int DOWNLOAD_FINISH = 2;

    public static final int NEED_UPDATE = 1;
    public static final int VERSION_OK = 0;

    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public static boolean debug = false;

    public Network(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = new DataInputStream(inputStream);
        this.outputStream = new DataOutputStream(outputStream);
    }

    private static final Gson GSON = new GsonBuilder().create();

    /*

    public void writeObject(Object object) throws IOException {
        String json = GSON.toJson(object);
        log("writeObject("+json+");");
        writeString(json);
    }

    public void writeString(String string) throws IOException {
        writeBytes(string.getBytes());
        log("writeString("+string+");");
    }

    public void writeUTF8(String string) throws IOException {
        writeBytes(string.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        log("writeUTF8("+string+");");
    }

    public void writeLong(long w) throws IOException {
        byte[] buffer = new byte[] {
                (byte) (w >> 56),
                (byte) (w >> 48),
                (byte) (w >> 40),
                (byte) (w >> 32),
                (byte) (w >> 24),
                (byte) (w >> 16),
                (byte) (w >> 8),
                (byte) (w)
        };
        outputStream.write(buffer, 0, buffer.length);
        outputStream.flush();
        log("writeLong("+w+");");
    }

    public void writeInt(int w) throws IOException {
        byte[] buffer = new byte[] {
                (byte) (w >> 24),
                (byte) (w >> 16),
                (byte) (w >> 8),
                (byte) (w)
        };
        outputStream.write(buffer, 0, buffer.length);
        outputStream.flush();
        log("writeInt("+w+");");
    }

    public void writeBytes(byte[] buffer) throws IOException {
        int w = buffer.length;
        byte[] buffer_size = new byte[] {
                (byte) (w >> 24),
                (byte) (w >> 16),
                (byte) (w >> 8),
                (byte) (w)
        };
        outputStream.write(buffer_size, 0, buffer_size.length);
        outputStream.write(buffer);
        outputStream.flush();
        log("writeBytes("+buffer.length+");");
    }

    public Object readObject(Class<?> class_) throws IOException {
        String json = readString();
        log("readObject(); = "+json);
        return GSON.fromJson(json, class_);
    }

    public long readLong() throws IOException {
        byte[] buffer = new byte[8];
        inputStream.read(buffer);
        long value = ByteBuffer.wrap(buffer).getLong();
        log("readLong(); = "+value);
        return value;
    }

    public byte[] readBytes() throws IOException {
        int length = readInt();
        byte[] buffer = new byte[length];
        int current = 0;
        while(true){
            int read = inputStream.read(buffer, current, buffer.length - current);
            if(read > 0){
                current += read;
            } else {
                break;
            }
        }
        return buffer;
    }

    public String readString() throws IOException {
        String value = new String(readBytes());
        log("readString(); = "+value);
        return value;
    }

    public int readInt() throws IOException {
        int ch1 = inputStream.read();
        int ch2 = inputStream.read();
        int ch3 = inputStream.read();
        int ch4 = inputStream.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        int value = ((ch1 << 24) & (ch2 << 16) & (ch3 << 8) & (ch4 << 0));
        log("readInt(); = "+value);
        return value;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }*/

    public void log(String message){
        if(!debug)return;
        System.out.println("[Network:debug] "+message);
    }

    @Override
    public void writeUTF(String value) throws IOException {
        outputStream.writeUTF(value);
        log("writeUTF("+value+")");
        flush();
    }

    @Override
    public void writeLong(long value) throws IOException {
        outputStream.writeLong(value);
        log("writeLong("+value+")");
        flush();
    }

    @Override
    public void writeInt(int value) throws IOException {
        outputStream.writeInt(value);
        log("writeInt("+value+")");
        flush();
    }

    @Override
    public void writeObject(Object object) throws IOException {
        String json = GSON.toJson(object);
        outputStream.writeUTF(json);
        log("writeObject("+json+")");
        flush();
    }

    @Override
    public void flush() throws IOException {
        log("flush();");
        outputStream.flush();
    }

    public Object readObject(Class<?> class_) throws IOException {
        String json = readUTF();
        log("readObject(); = "+json);
        return GSON.fromJson(json, class_);
    }

    @Override
    public String readUTF() throws IOException {
        String value = inputStream.readUTF();
        log("readUTF(); = "+value);
        return value;
    }

    @Override
    public long readLong() throws IOException {
        long value = inputStream.readLong();
        log("readLong(); = "+value);
        return value;
    }

    @Override
    public int readInt() throws IOException {
        int value = inputStream.readInt();
        log("readInt(); = "+value);
        return value;
    }
}
