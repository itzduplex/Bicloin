package pt.tecnico.bicloin.hub;

import static io.grpc.Status.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.BalanceRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BalanceResponse;

public class BalanceIT extends BaseIT {
    @Test
    public void balanceSuccessTest() throws InterruptedException {

        BalanceRequest request = BalanceRequest.newBuilder().setUser("alice").build();
        BalanceResponse response = frontend.balance(request);

        assertEquals(0, response.getBalance());
    }

    @Test
    public void balanceInvalidUserTest() {
        BalanceRequest request = BalanceRequest.newBuilder().setUser("daniel b@tista").build();

        assertEquals(INVALID_ARGUMENT.getCode(),
				assertThrows(StatusRuntimeException.class, () -> frontend.balance(request)).getStatus().getCode());

    }
}
