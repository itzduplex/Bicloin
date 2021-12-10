package pt.tecnico.bicloin.hub;

public class HubUserEntry {
    private String user;
    private String name;
    private String phone;

    public HubUserEntry(String user, String name, String phone) {
        this.user = user;
        this.name = name;
        this.phone = phone;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}

