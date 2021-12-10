package pt.tecnico.rec;

import java.util.HashMap;

import pt.tecnico.rec.grpc.Rec.Tag;
import pt.tecnico.rec.grpc.Rec.ReadRequest;
import pt.tecnico.rec.grpc.Rec.ReadResponse;
import pt.tecnico.rec.grpc.Rec.WriteRequest;
import pt.tecnico.rec.grpc.Rec.WriteResponse;

public class RecBackend {
	private final HashMap<String, RecUserEntry> users = new HashMap<>();
	private final HashMap<String, RecStationEntry> stations = new HashMap<>();
	private final int weight;

    public RecBackend(int weight) {
        this.weight = weight;
    }
	

    public WriteResponse write(WriteRequest request) {

		String key = request.getKey(); //id 
		int table = request.getTable(); //user or station
		int column = request.getColumn(); //which attribute to change
		int value = request.getValue(); //value to change
		int seq = request.getTag().getSeq();
		int cid = request.getTag().getCid();

		if (table == 0) {    // user table
			users.computeIfAbsent(key, k -> new RecUserEntry(0, 0));

			EntryTag tag = new EntryTag(seq, cid);
			if (tag.compareTo(users.get(key).getTagByIndex(column)) > 0) {
				users.get(key).setTagByIndex(column, tag);
				users.get(key).setByColumn(column, value);

			}
		}
		else {
			stations.computeIfAbsent(key, k -> new RecStationEntry(-1, -1, -1));

			EntryTag tag = new EntryTag(seq, cid);
			
			if (tag.compareTo(stations.get(key).getTagByIndex(column)) > 0) {
				stations.get(key).setTagByIndex(column, tag);
				stations.get(key).setByColumn(column, value);
			}


		}

		return WriteResponse.getDefaultInstance();
    }

    public ReadResponse read(ReadRequest request) {
		String key = request.getKey();
		int table = request.getTable(); //user or station
		int column = request.getColumn(); //which attribute to change
		int output = -1;
		
		int cid, seq;

		if (table == 0) {    // user table
			users.computeIfAbsent(key, k -> new RecUserEntry(0, 0));
			EntryTag usersTag = users.get(key).getTagByIndex(column);
			seq = usersTag.getSeq();
			cid = usersTag.getCid();
			output = users.get(key).getByColumn(column);
		}
		else { 				// table stations
			stations.computeIfAbsent(key, k -> new RecStationEntry(-1, -1, -1));
			EntryTag stationsTag = stations.get(key).getTagByIndex(column);
			seq = stationsTag.getSeq();
			cid = stationsTag.getCid();
			output = stations.get(key).getByColumn(column);
		}
		Tag tag = Tag.newBuilder().setSeq(seq).setCid(cid).setWeight(weight).build();
		return ReadResponse.newBuilder().setValue(output).setTag(tag).build();
    }
}
