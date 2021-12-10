package pt.tecnico.bicloin.hub;

public class HubException extends Exception {
	private static final long serialVersionUID = 1L;

	public HubException() {
	}

	public HubException(String message) {
		super(message);
	}

	public HubException(Throwable cause) {
		super(cause);
	}

	public HubException(String message, Throwable cause) {
		super(message, cause);
	}

    public HubException(HubError hubError) {
		super(hubError.label);
    }
}
