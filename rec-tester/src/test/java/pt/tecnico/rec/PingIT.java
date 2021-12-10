package pt.tecnico.rec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.grpc.StatusRuntimeException;
import pt.tecnico.rec.grpc.Rec.CtrlPingRequest;
import pt.tecnico.rec.grpc.Rec.CtrlPingResponse;
import static io.grpc.Status.INVALID_ARGUMENT;

public class PingIT extends BaseIT {
	@Test
	public void pingOKTest() throws InterruptedException {
		CtrlPingRequest request = CtrlPingRequest.newBuilder().setInput("friend").build();
		CtrlPingResponse response = frontend.ctrlPing(request);
		System.out.println(response.getOutput());
		assertEquals("Hello friend!", response.getOutput());
	}

	@Test
	public void emptyPingTest() {
		CtrlPingRequest request = CtrlPingRequest.newBuilder().setInput("").build();
		assertEquals(INVALID_ARGUMENT.getCode(),
				assertThrows(StatusRuntimeException.class, () -> frontend.ctrlPing(request)).getStatus().getCode());
	}
}
