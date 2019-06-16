package gateway72.auth;

public class StatusException extends Exception {
    private final int httpStatus;
    
    public StatusException(int status, String msg) {
        super(status + ": " + msg);
        this.httpStatus = status;
    }
    
    public StatusException(int status, Throwable t) {
        this(status, t.getClass().getName() + ": " + t.getMessage());
    }

    public int getStatus() {
        return httpStatus;
    }
}
