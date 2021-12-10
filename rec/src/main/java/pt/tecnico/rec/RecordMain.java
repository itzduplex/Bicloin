package pt.tecnico.rec;

import java.io.IOException;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class RecordMain {
	
	public static void main(String[] args) throws IOException, InterruptedException, ZKNamingException {
		System.out.println(RecordMain.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		final String zooHost = args[0];
		final String zooPort = args[1];

		final String host = args[2];
		final String port = args[3];

		final String path = args[4];

		String[] splitPath = path.split("/");
		int instance = Integer.parseInt(splitPath[splitPath.length - 1]);

		int weight = 1;

		if (args.length == 6)
			weight = Integer.parseInt(args[5]);

		final BindableService impl = new RecImpl(weight);

		ZKNaming zkNaming = new ZKNaming(zooHost, zooPort);

		try {
			Runtime.getRuntime().addShutdownHook(new Termination(zkNaming, path, host, port));

			zkNaming.rebind(path, host, port);

			Server rec = ServerBuilder.forPort(Integer.parseInt(port)).addService(impl).build();
	
			rec.start();
	
	
			System.out.println("Rec " + instance + " started");
	
			rec.awaitTermination();

		} finally  {	
			if (zkNaming != null) {
				zkNaming.unbind(path,host,port);
			}
		}

	}
	
}
