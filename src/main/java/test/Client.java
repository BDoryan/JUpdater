package test;

import doryanbessiere.jupdater.fr.JUpdater;
import doryanbessiere.jupdater.fr.client.JUpdateClient;
import doryanbessiere.jupdater.fr.listeners.JUpdateTraficListener;

import java.io.File;
import java.io.IOException;

public class Client {

    public static void main(String[] args) {

        JUpdater jupdater = new JUpdater(new File("C:\\Users\\Doryan\\Documents\\Pétanque Manager\\Software"), "0.0.1");
        JUpdateClient client = new JUpdateClient(jupdater);
        client.getListeners().add(new JUpdateTraficListener() {
            @Override
            public void downloadProgress(String file, int file_index, double percent, long currently, long total) {
                System.out.println("Téléchargement en cours du fichier : "+file+" ("+percent+"%)");
            }

            @Override
            public void downloadStart(String file, int file_index) {
                System.out.println("Lancement du télécharger du fichier : "+file);
            }

            @Override
            public boolean downloadFinish() {
                System.out.println("Téléchargement terminée.");
                return false;
            }

            @Override
            public void updateStart(int files_count) {
                System.out.println("Mise à jour nécessaire, lancement de celle-ci.");
            }

            @Override
            public void updateFinish() {
                System.out.println("Mise à jour nécessaire, faites correctement.");
            }
        });
        try {
            System.out.println("Connexion à la base de mise à jour...");
            client.connect("localhost", 222);
            System.out.println("Connexion réalisé avec succès.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
