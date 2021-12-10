package pt.tecnico.bicloin.hub;

import static io.grpc.Status.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.CtrlPingRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.CtrlPingResponse;

public class PingIT extends BaseIT {
	@Test
	public void pingOKTest() throws InterruptedException {
		CtrlPingRequest request = CtrlPingRequest.newBuilder().setInput("friend").build();
		CtrlPingResponse response = frontend.ctrlPing(request);
		assertEquals("Hello friend!", response.getOutput());
	}

	@Test
	public void emptyPingTest() {
		CtrlPingRequest request = CtrlPingRequest.newBuilder().setInput("").build();
		assertEquals(INVALID_ARGUMENT.getCode(),
				assertThrows(StatusRuntimeException.class, () -> frontend.ctrlPing(request)).getStatus().getCode());
	}
}
