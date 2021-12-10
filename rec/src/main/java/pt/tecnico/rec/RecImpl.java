package pt.tecnico.rec;


import static io.grpc.Status.INVALID_ARGUMENT;

import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.grpc.Rec.CtrlPingRequest;
import pt.tecnico.rec.grpc.Rec.CtrlPingResponse;
import pt.tecnico.rec.grpc.Rec.ReadRequest;
import pt.tecnico.rec.grpc.Rec.ReadResponse;
import pt.tecnico.rec.grpc.Rec.WriteRequest;
import pt.tecnico.rec.grpc.Rec.WriteResponse;
import pt.tecnico.rec.grpc.RecordServiceGrpc;



public class RecImpl extends RecordServiceGrpc.RecordServiceImplBase {
	final private RecBackend backend;

	public RecImpl(int weight) {
		this.backend = new RecBackend(weight);
	}

    @Override
    public void ctrlPing(CtrlPingRequest request, StreamObserver<CtrlPingResponse> responseObserver) {
		String input = request.getInput();
		if (input == null || input.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Input cannot be empty!").asRuntimeException());
			return;
		}
		String output = "Hello " + input + "!";
		CtrlPingResponse response = CtrlPingResponse.newBuilder().setOutput(output).build();	
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void write(WriteRequest request, StreamObserver<WriteResponse> responseObserver) {
		WriteResponse response = backend.write(request);
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
		ReadResponse response = backend.read(request);
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}