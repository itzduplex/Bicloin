 package pt.tecnico.rec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pt.tecnico.rec.grpc.Rec.Tag;
import pt.tecnico.rec.grpc.Rec.ReadRequest;
import pt.tecnico.rec.grpc.Rec.ReadResponse;
import pt.tecnico.rec.grpc.Rec.WriteRequest;

public class ReadWriteIT extends BaseIT {
    @Test
    public void readAndWriteTestForUsers() throws InterruptedException {
        int value = 5678;
        seq++;
        Tag tag = Tag.newBuilder().setSeq(seq).setCid(1).build();

        WriteRequest write_request = WriteRequest.newBuilder().setKey("daniel batista").setTable(0).setColumn(0).setValue(value).setTag(tag).build();
        frontend.write(write_request);
        
        ReadRequest read_request = ReadRequest.newBuilder().setKey("daniel batista").setTable(0).setColumn(0).build();
        ReadResponse read_response = frontend.read(read_request);
		assertEquals(value, read_response.getValue());
    }

    @Test
    public void readAndWriteTestForStations() throws InterruptedException {
        int value = 5678;
        Tag tag = Tag.newBuilder().setSeq(seq).setCid(1).build();
        WriteRequest write_request = WriteRequest.newBuilder().setKey("daniel batista").setTable(1).setColumn(0).setValue(value).setTag(tag).build();
        frontend.write(write_request);

        ReadRequest read_request = ReadRequest.newBuilder().setKey("daniel batista").setTable(1).setColumn(0).build();
        ReadResponse read_response = frontend.read(read_request);
        assertEquals(value, read_response.getValue());
    }
}
