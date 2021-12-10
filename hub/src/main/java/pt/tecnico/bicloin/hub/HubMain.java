package pt.tecnico.bicloin.hub;

import java.io.IOException;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class HubMain {

	public static void main(String[] args) throws IOException, InterruptedException, ZKNamingException, NumberFormatException {
		System.out.println(HubMain.class.getSimpleName());
		
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

		final String usersPath = args[5];
		final String stationsPath = args[6];


		String[] splitPath = path.split("/");
		final int cid = Integer.parseInt(splitPath[splitPath.length - 1]);


		boolean initRec = false;
		ZKNaming zkNaming = new ZKNaming(zooHost, zooPort);
		String recs = "/grpc/bicloin/rec";
		int combinedWeight = zkNaming.listRecords(recs).size();

		if (args.length >= 8) {
			initRec = true;
		}

		if (args.length == 9) {
			combinedWeight = Integer.parseInt(args[8]);
		}


		try {
			zkNaming.rebind(path, host, port);

			Runtime.getRuntime().addShutdownHook(new Termination(zkNaming, path, host, port));

			final BindableService impl = new HubImpl(zkNaming, path, usersPath, stationsPath, cid, initRec, combinedWeight);

			Server hub = ServerBuilder.forPort(Integer.parseInt(port)).addService(impl).build();
	
			hub.start();
	
			System.out.println("Hub started");
	
			hub.awaitTermination();

		} catch (HubException he) {
			System.out.println(he.getMessage());
		} finally  {
			if (zkNaming != null) {
				zkNaming.unbind(path,host,port);
			}
		}
	}
}
