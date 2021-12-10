package pt.tecnico.bicloin.hub;

public enum HubError {
    NO_BIKES_AVAILABLE("No bikes available"),
    INSUFFICIENT_BALANCE("Insufficient balance"),
    USER_HAS_BIKE("User already has bike"),
    STATION_DOESNT_EXIST("Station does not exist"),
    USER_OUT_OF_RANGE("User out of range"),
    NO_DOCKS("No docks available"),
    USER_HAS_NO_BIKE("User has no bike"),
    INVALID_K_GIVEN("Invalid k number given"),
    WRONG_PHONE_NUMBER("Phone number doesn't match user");

    public final String label;

    HubError(String label) {
        this.label = label;
    }
}
