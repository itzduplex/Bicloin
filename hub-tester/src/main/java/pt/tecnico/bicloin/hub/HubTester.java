package pt.tecnico.bicloin.hub;

import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class HubTester {
	
	public static void main(String[] args) throws ZKNamingException, InterruptedException {
		System.out.println(HubTester.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		String host = args[0];
		String port = args[1];
		String path = args[4];

		HubFrontend frontend = new HubFrontend(host, port, path, true);
		try {
			double lat1 = 38.7370, long1 = -9.1366;
			System.out.println(frontend.locateStation(LocateStationRequest.newBuilder().setLongitude(long1).setLatitude(lat1).setK(4).build()));
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " +
			e.getStatus().getDescription());
		} 

		frontend.shutdown();
	}
	
}
