package pt.tecnico.bicloin.hub;

import static io.grpc.Status.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeDownRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpRequest;

public class BikeDownIT extends BaseIT {
    @Test
    public void bikeDownHasNoBikeTest() {
        double lat = 38.7371;
        double lon = -9.3024;
        BikeDownRequest request = BikeDownRequest.newBuilder().setUser("diana").setLatitude(lat).setLongitude(lon).setStation("istt").build();
        assertEquals(INVALID_ARGUMENT.getCode(),
                assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request)).getStatus().getCode());

    }

    @Test
    public void bikeDownSuccessTest() throws InterruptedException {
        double lat = 38.7371;
        double lon = -9.3024;
        frontend.topUp(TopUpRequest.newBuilder().setUser("diana").setAmount(20).setPhone("+34010203").build());
        frontend.bikeUp(BikeUpRequest.newBuilder().setUser("diana").setLatitude(lat).setLongitude(lon).setStation("istt").build());
        BikeDownRequest request = BikeDownRequest.newBuilder().setUser("diana").setLatitude(lat).setLongitude(lon).setStation("istt").build();
        assertDoesNotThrow(() -> frontend.bikeDown(request));
    }

    @Test
    public void bikeDownFarTest() throws InterruptedException {
        double lat = 3800.7371;
        double lon = -9313.3024;
        frontend.topUp(TopUpRequest.newBuilder().setUser("diana").setAmount(20).setPhone("+34010203").build());
        BikeDownRequest request = BikeDownRequest.newBuilder().setUser("diana").setLatitude(lat).setLongitude(lon).setStation("istt").build();

        assertEquals(INVALID_ARGUMENT.getCode(),
				assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request)).getStatus().getCode());

    }

    @Test
    public void bikeDownInvalidStationTest() {
        double lat = 38.7371;
        double lon = -9.3024; 
        BikeDownRequest request = BikeDownRequest.newBuilder().setUser("diana").setLatitude(lat).setLongitude(lon).setStation("invalid station").build();

        assertEquals(INVALID_ARGUMENT.getCode(),
				assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request)).getStatus().getCode());

    }

    @Test
    public void bikeDownInvalidUserNameTest() {
        double lat = 38.7371;
        double lon = -9.3024; 
        BikeDownRequest request = BikeDownRequest.newBuilder().setUser("franciscadalgarve").setLatitude(lat).setLongitude(lon).setStation("istt").build();

        assertEquals(INVALID_ARGUMENT.getCode(),
				        assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request)).getStatus().getCode());

    }

}
