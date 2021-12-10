package pt.tecnico.bicloin.hub;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import pt.tecnico.rec.grpc.Rec.WriteRequest;
import pt.tecnico.rec.grpc.Rec.Tag;


public class HubParsing {
    public HashMap<String, HubUserEntry> parseUsers(String usersPath, boolean initRec, HubBackend backend) throws HubException, InterruptedException {
        HashMap<String, HubUserEntry> users = new HashMap<>();
        try (BufferedReader usersReader = new BufferedReader(new FileReader(usersPath));) {
            String[] data;
            String row;
            while ((row = usersReader.readLine()) != null) {
                data = row.split(",");
                if (!data[0].matches("[A-Za-z0-9]+") || data[0].length() < 3 || data[0].length() > 10 || data[1].length() < 3 || data[1].length() > 30
                  || !data[2].matches("[+][0-9-]+") || data[2].length() > 15)
                    throw new HubException("User: Invalid Arguments");

                users.put(data[0], new HubUserEntry(data[0], data[1], data[2]));  //data[0] -> username, data[1] -> name, data[2] -> phone number

                if (initRec) {
                    Tag tag = Tag.newBuilder().setSeq(1).setCid(1).build();
                    backend.writeBack(WriteRequest.newBuilder().setKey(data[0]).setTable(0).setColumn(0).setValue(0).setTag(tag).build()); // balance
                }

            }
        } catch (FileNotFoundException fnfe) {
            throw new HubException("File was not found");
        } catch (IOException ioe) {
            throw new HubException("User Table has invalid arguments");
        }
        return users;
    }

    public HashMap<String, HubStationEntry> parseStations(String stationsPath, boolean initRec, HubBackend backend) throws HubException {
        HashMap<String, HubStationEntry> stations = new HashMap<>();
        try (BufferedReader stationsReader = new BufferedReader(new FileReader(stationsPath))) {
            String[] data;
            String row;
            while ((row = stationsReader.readLine()) != null) {
                data = row.split(",");

                if (!data[1].matches("[A-Za-z0-9]+") || data[1].length() != 4)
                    throw new HubException("Station: Invalid arguments");

                // data[0] -> name, data[1] -> abbreviation, data[2] -> latitude, data[3] -> longitude
                stations.put(data[1], new HubStationEntry(data[0], data[1], Double.parseDouble(data[2]), Double.parseDouble(data[3])));
                if (initRec) {
                    if (Integer.parseInt(data[6]) <= 0 || Integer.parseInt(data[5]) < 0 || Integer.parseInt(data[4]) <= 0)
                        throw new HubException("Station Registry: Invalid Arguments");
                    Tag tag = Tag.newBuilder().setSeq(1).setCid(1).build();
                    backend.writeBack(WriteRequest.newBuilder().setKey(data[1]).setTable(1).setColumn(0).setValue(Integer.parseInt(data[4])).setTag(tag).build()); //numDocks
                    backend.writeBack(WriteRequest.newBuilder().setKey(data[1]).setTable(1).setColumn(1).setValue(Integer.parseInt(data[5])).setTag(tag).build()); //numBikes
                    backend.writeBack(WriteRequest.newBuilder().setKey(data[1]).setTable(1).setColumn(2).setValue(Integer.parseInt(data[6])).setTag(tag).build()); //prize
                    backend.writeBack(WriteRequest.newBuilder().setKey(data[1]).setTable(1).setColumn(3).setValue(0).setTag(tag).build()); // numPickups
                    backend.writeBack(WriteRequest.newBuilder().setKey(data[1]).setTable(1).setColumn(4).setValue(0).setTag(tag).build()); // numDrops
                }
            }
        } catch (IOException | NumberFormatException | InterruptedException e) {
            throw new HubException("Station Table has invalid arguments");
        }
        return stations;
    }
}