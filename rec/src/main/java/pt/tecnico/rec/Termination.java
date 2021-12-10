package pt.tecnico.rec;

import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class Termination extends Thread {
    private ZKNaming zkNaming;
    private String path;
    private String host;
    private String port;

    public Termination(ZKNaming zkNaming, String path, String host, String port) {
        this.zkNaming = zkNaming;
        this.path = path;
        this.host = host;
        this.port = port;
    }
    
    @Override
    public void run() {
        try {
            zkNaming.unbind(path,host,port);
        } catch (ZKNamingException ignored) {

        }
    }
}
