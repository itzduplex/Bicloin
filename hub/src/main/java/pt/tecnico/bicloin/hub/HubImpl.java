package pt.tecnico.bicloin.hub;

import static io.grpc.Status.INVALID_ARGUMENT;

import io.grpc.stub.StreamObserver;
import pt.tecnico.bicloin.hub.grpc.Hub.BalanceRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BalanceResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeDownRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeDownResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeUpResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.CtrlPingRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.CtrlPingResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.InfoStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.InfoStationResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.LocateStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.LocateStationResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.SysStatusRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SysStatusResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpResponse;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;


public class HubImpl extends HubServiceGrpc.HubServiceImplBase {
	private final HubBackend backend;
	
	public HubImpl(ZKNaming zkGaming, String path, String users, String stations, int cid, boolean initRec, int combinedWeight) throws ZKNamingException, HubException, InterruptedException {
		this.backend = new HubBackend(path, zkGaming, users, stations, cid, initRec, combinedWeight);
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
	public void sysStatus(SysStatusRequest request, StreamObserver<SysStatusResponse> responseObserver) {
		try {
			SysStatusResponse response = backend.sysStatus();
			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (ZKNamingException e) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ZKNamingException").asRuntimeException());
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

	}

	@Override
	public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
		if (!request.getUser().matches("[A-Za-z0-9]+") || request.getUser().length() < 3 || request.getUser().length() > 10) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid username").asRuntimeException());
			return;
		}

		try {
			BalanceResponse response = backend.balance(request);
			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (InterruptedException he) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription(he.getMessage()).asRuntimeException());
		}

	}

	@Override
	public void topUp(TopUpRequest request, StreamObserver<TopUpResponse> responseObserver) {
		if (!request.getUser().matches("[A-Za-z0-9]+") || request.getUser().length() < 3 || request.getUser().length() > 10) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid username").asRuntimeException());
			return;
		}
		
		if (request.getAmount() < 1 || request.getAmount() > 20) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid amount").asRuntimeException());
			return;
		}
		
		if (!request.getPhone().matches("[+][0-9-]+") || request.getPhone().length() > 15) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid phone number").asRuntimeException());
			return;
		}
		

		try {
			TopUpResponse response = backend.topUp(request);
			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (HubException | InterruptedException he) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Phone number doesn't match").asRuntimeException());
		}

	}

	@Override
	public void bikeUp(BikeUpRequest request, StreamObserver<BikeUpResponse> responseObserver) {
		if (!request.getUser().matches("[A-Za-z0-9]+") || request.getUser().length() < 3 || request.getUser().length() > 10)  {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid username").asRuntimeException());
			return;
		}

		if (!request.getStation().matches("[A-Za-z0-9]+") || request.getStation().length() != 4) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid station").asRuntimeException());
			return;
		}
		
		try {
			BikeUpResponse response = backend.bikeUp(request);
			responseObserver.onNext(response);
			responseObserver.onCompleted();

		} catch (HubException | InterruptedException he) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription(he.getMessage()).asRuntimeException());
		}
	}

	@Override
	public void bikeDown(BikeDownRequest request, StreamObserver<BikeDownResponse> responseObserver) {
		if (!request.getUser().matches("[A-Za-z0-9]+") || request.getUser().length() < 3 || request.getUser().length() > 10) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid username").asRuntimeException());
			return;
		}

		if (!request.getStation().matches("[A-Za-z0-9]+") || request.getStation().length() != 4) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid station").asRuntimeException());
			return;
		}
		
		try {
			BikeDownResponse response = backend.bikeDown(request);
			responseObserver.onNext(response);
			responseObserver.onCompleted();

		}  catch (HubException | InterruptedException he) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription(he.getMessage()).asRuntimeException());
		}
	}

	@Override
	public void infoStation(InfoStationRequest request, StreamObserver<InfoStationResponse> responseObserver) {
		if (!request.getStation().matches("[A-Za-z0-9]+") || request.getStation().length() != 4) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid station").asRuntimeException());
			return;
		}
		
		try {
			InfoStationResponse response = backend.infoStation(request);
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (HubException | InterruptedException he) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription(he.getMessage()).asRuntimeException());
		}
	}

	@Override
	public void locateStation(LocateStationRequest request, StreamObserver<LocateStationResponse> responseObserver) {
		try {
			LocateStationResponse response = backend.locateStation(request);
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (HubException he) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription(he.getMessage()).asRuntimeException());
		}
	}
}