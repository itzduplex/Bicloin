package pt.tecnico.bicloin.app;

import java.util.Collection;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class AppMain {

	public static void main(String[] args) throws NumberFormatException, ZKNamingException, InterruptedException {
		System.out.println(AppMain.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		final String zooHost = args[0];
		final String zooPort = args[1];
		final String nameId = args[2];
		final String phone = args[3];
		double lat = Double.parseDouble(args[4]);
		double lon = Double.parseDouble(args[5]);

		Scanner scanner = new Scanner(System.in);
		ZKNaming zkNaming = new ZKNaming(zooHost, zooPort);

		String hub = "/grpc/bicloin/hub";
		Collection<ZKRecord> hubs = zkNaming.listRecords(hub);
		Optional<ZKRecord> possibleHub = hubs.stream().findAny();

		if (possibleHub.isEmpty()) {
			System.out.println("There are no hubs available.");
			scanner.close();
			return;
		}

		String uri = possibleHub.get().getURI();
		App appFrontend = new App(zooHost, zooPort, uri, lat, lon);

		long startTime = System.currentTimeMillis();

		while (true) {
			System.out.print("> ");

			// clause that breaks the while loop
			if (!scanner.hasNextLine()) {
				System.out.print("\b\b");
				scanner.close();
				appFrontend.shutdown();
				System.exit(0);
			}

			String line = scanner.nextLine();
			String[] splitLine = line.split("\\s+");
			try {
				switch (splitLine[0]) {
					case "balance":
						appFrontend.balance(nameId);
						break;

					case "top-up":
						appFrontend.topUp(nameId, splitLine[1], phone);
						break;

					case "tag":
						appFrontend.tag(splitLine[1], splitLine[2], splitLine[3]);
						break;

					case "move":
						appFrontend.move(nameId, splitLine);
						break;

					case "at":
						appFrontend.at(nameId);
						break;

					case "scan":
						appFrontend.scan(splitLine[1]);
						break;

					case "info":
						appFrontend.info(splitLine[1]);
						break;

					case "bike-up":
						appFrontend.bikeUp(nameId, splitLine[1]);
						break;

					case "bike-down":
						appFrontend.bikeDown(nameId, splitLine[1]);
						break;

					case "exit":
						long endTime = System.currentTimeMillis();
       					System.out.println("Total execution time: " + (endTime-startTime) + "ms"); 
						appFrontend.shutdown();
						System.exit(0);
						break;

					case "sys-status":
						appFrontend.sysStatus();
						break;

					case "ping":
						appFrontend.ping();
						break;

					case "zzz":
						System.out.println("Dormir");
						TimeUnit.MILLISECONDS.sleep(Integer.parseInt(splitLine[1]));
						break;

					case "#":
					case "":
						System.out.print("\b\b");
						break;

					case "help":
						appFrontend.help();
						break;

					default:
						System.out.println("Invalid function call");
						break;
				}
			} catch (IndexOutOfBoundsException ioobe) {
				System.out.println("Invalid command");
			}
		}
	
	}
}
