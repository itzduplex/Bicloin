package pt.tecnico.bicloin.hub;

import static io.grpc.Status.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpResponse;

public class TopUpIT extends BaseIT {
	@Test
	public void topUpSuccessTest() throws InterruptedException {
        int amount_to_add = 4;

        TopUpRequest request = TopUpRequest.newBuilder().setUser("bruno").setAmount(amount_to_add).setPhone("+35193334444").build();
        TopUpResponse response = frontend.topUp(request);


        assertEquals(amount_to_add * 10, response.getBalance());
	}

	@Test
	public void topUpInvalidAmountTest() {
        TopUpRequest request = TopUpRequest.newBuilder().setUser("bruno").setAmount(500).setPhone("+35193334444").build();
		assertEquals(INVALID_ARGUMENT.getCode(),
				assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request)).getStatus().getCode());
	}

    @Test
	public void topUpInvalidPhoneTest() {
        TopUpRequest request = TopUpRequest.newBuilder().setUser("bruno").setAmount(3).setPhone("numerotelemovel").build();
		assertEquals(INVALID_ARGUMENT.getCode(),
				assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request)).getStatus().getCode());
	}

}
