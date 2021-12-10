package pt.tecnico.bicloin.hub;

public class HubStationEntry {
    private String stationName;
    private String abbrev;
    private double latitude;
    private double longitude;

    public HubStationEntry(String stationName, String abbrev, double latitude, double longitude) {
        this.stationName = stationName;
        this.abbrev = abbrev;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getStationName() {
        return this.stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getAbbrev() {
        return this.abbrev;
    }

    public void setAbbrev(String abbrev) {
        this.abbrev = abbrev;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}

