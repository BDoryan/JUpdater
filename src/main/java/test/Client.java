package test;

import doryanbessiere.jupdater.fr.JUpdater;
import doryanbessiere.jupdater.fr.client.JUpdaterClient;

import java.io.File;

public class Client {

    public static void main(String[] args) {
        JUpdater jupdater = new JUpdater(new File("C:\\Users\\Doryan\\Documents\\Pétanque Manager\\Software"), "1.0.1");
        JUpdaterClient client = new JUpdaterClient(jupdater);
        client.connect("localhost", 222);
    }
}
