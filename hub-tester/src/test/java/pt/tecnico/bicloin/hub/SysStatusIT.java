package pt.tecnico.bicloin.hub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import pt.tecnico.bicloin.hub.grpc.Hub;

public class SysStatusIT extends BaseIT {
    @Test
    public void sysStatusSuccessTest() throws InterruptedException {
        Hub.SysStatusResponse response = frontend.sysStatus(Hub.SysStatusRequest.getDefaultInstance());

        assertEquals("/grpc/bicloin/hub/1", response.getServerName(0));
        assertEquals("/grpc/bicloin/rec/1", response.getServerName(1));
        assertTrue(response.getIsUp(0));
        assertTrue(response.getIsUp(1));
    }

}
