package pt.tecnico.bicloin.hub;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pt.tecnico.bicloin.hub.grpc.Hub.LocateStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.LocateStationResponse;

public class LocateStationIT extends BaseIT {
    @Test
    public void locateStationSuccessTest() throws InterruptedException {
        double lat1 = 38.7370, long1 = -9.1366;
        LocateStationResponse response = frontend.locateStation(LocateStationRequest.newBuilder().setLongitude(long1).setLatitude(lat1).setK(4).build());
        assertEquals("ista", response.getStations(0));
        assertEquals("gulb", response.getStations(1));
        assertEquals("cate", response.getStations(2));
        assertEquals("prcm", response.getStations(3));
    }
}
