package pt.tecnico.bicloin.hub;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pt.tecnico.bicloin.hub.grpc.Hub.BikeDownRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.InfoStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.InfoStationResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpRequest;

public class InfoStationIT extends BaseIT {
    @Test
    public void infoStationSuccessTest() throws InterruptedException {
        String station = "gulb", user = "eva";
        double lat = 38.7376, longt = -9.1545;
        
        frontend.topUp(TopUpRequest.newBuilder().setUser(user).setAmount(20).setPhone("+155509080706").build());
        frontend.bikeUp(BikeUpRequest.newBuilder().setUser(user).setLatitude(lat).setLongitude(longt).setStation(station).build());
        frontend.bikeDown(BikeDownRequest.newBuilder().setUser(user).setLatitude(lat).setLongitude(longt).setStation(station).build());
        
        InfoStationRequest request = InfoStationRequest.newBuilder().setStation("gulb").build();
        InfoStationResponse response = frontend.infoStation(request);
        
        assertEquals("Gulbenkian", response.getStationName());
        assertEquals(lat, response.getLatitude());
        assertEquals(longt, response.getLongitude());
        assertEquals(30, response.getNumBicycles());
        assertEquals(30, response.getNumDocks());
        assertEquals(2, response.getPrize());
        assertEquals(1, response.getNumDrops());
        assertEquals(1, response.getNumPickups());
    }
}
