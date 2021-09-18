package doryanbessiere.jupdater.fr.listeners;

import doryanbessiere.jupdater.fr.server.remoteclient.JUpdaterRemoteClient;

public abstract class JUpdaterListener {

    public abstract void update(JUpdaterRemoteClient remoteClient, String version);
    public abstract void connection(JUpdaterRemoteClient remoteClient, String version);

}
