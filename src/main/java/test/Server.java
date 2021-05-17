package test;

import doryanbessiere.jupdater.fr.JUpdater;
import doryanbessiere.jupdater.fr.server.JUpdaterServer;

import java.io.File;

public class Server {

    public static void main(String[] args) {
        JUpdater jupdater = new JUpdater(new File("C:\\Users\\Doryan\\Documents\\Pétanque Manager\\JUpdaterServer\\base"), "1.0.0");
        JUpdaterServer server = new JUpdaterServer(jupdater, 222);
        server.start();
    }
}
