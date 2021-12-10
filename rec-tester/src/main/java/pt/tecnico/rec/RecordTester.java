package pt.tecnico.rec;

import io.grpc.StatusRuntimeException;
import pt.tecnico.rec.grpc.Rec.ReadRequest;
import pt.tecnico.rec.grpc.Rec.ReadResponse;
import pt.tecnico.rec.grpc.Rec.WriteRequest;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class RecordTester {
	
	public static void main(String[] args) throws ZKNamingException, InterruptedException {
		System.out.println(RecordTester.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}
		
		String host = args[0];
		String port = args[1];
		String path = args[4];
		
		RecFrontend frontend = new RecFrontend(host, port, path);
		
		try {
			WriteRequest request2 = WriteRequest.newBuilder().setKey("daniel batista").setTable(0).setColumn(0).setValue(10).build();
			frontend.write(request2);

			ReadRequest request3 = ReadRequest.newBuilder().setKey("daniel batista").setTable(0).setColumn(0).build();
			ReadResponse response3 = frontend.read(request3);

			
			System.out.println(response3.getValue());
		} catch (StatusRuntimeException e) {
			System.out.println("Caught exception with description: " +
			e.getStatus().getDescription());
		}

		frontend.shutdown();
	}
	
}








