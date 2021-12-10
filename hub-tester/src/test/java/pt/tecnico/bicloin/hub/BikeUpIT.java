package pt.tecnico.bicloin.hub;

import static io.grpc.Status.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpRequest;

public class BikeUpIT extends BaseIT {
    @Test
    public void bikeUpSuccessTest() throws InterruptedException {
        double lat = 38.7371;
        double lon = -9.3024;
        frontend.topUp(TopUpRequest.newBuilder().setUser("carlos").setAmount(20).setPhone("+34203040").build());
        BikeUpRequest request = BikeUpRequest.newBuilder().setUser("carlos").setLatitude(lat).setLongitude(lon).setStation("istt").build();
        assertDoesNotThrow(() -> frontend.bikeUp(request));
    }

    @Test
    public void bikeUpFarTest() {
        double lat = 3800.7371;
        double lon = -9313.3024; 
        BikeUpRequest request = BikeUpRequest.newBuilder().setUser("carlos").setLatitude(lat).setLongitude(lon).setStation("istt").build();
        assertEquals(INVALID_ARGUMENT.getCode(),
				assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getCode());

    }

    @Test
    public void bikeUpInvalidStationTest() {
        double lat = 38.7371;
        double lon = -9.3024; 
        BikeUpRequest request = BikeUpRequest.newBuilder().setUser("carlos").setLatitude(lat).setLongitude(lon).setStation("invalid station").build();

        assertEquals(INVALID_ARGUMENT.getCode(),
				assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getCode());

    }

    @Test
    public void bikeUpInvalidUserNameTest() {
        double lat = 38.7371;
        double lon = -9.3024; 
        BikeUpRequest request = BikeUpRequest.newBuilder().setUser("franciscadalgarve").setLatitude(lat).setLongitude(lon).setStation("istt").build();

        assertEquals(INVALID_ARGUMENT.getCode(),
				        assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getCode());

    }

    @Test
    public void bikeUpAlreadyHasBikeTest() {
        double lat = 38.7371;
        double lon = -9.3024; 
        BikeUpRequest request = BikeUpRequest.newBuilder().setUser("carlos").setLatitude(lat).setLongitude(lon).setStation("istt").build();

        assertEquals(INVALID_ARGUMENT.getCode(),
				assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getCode());

    }
}
