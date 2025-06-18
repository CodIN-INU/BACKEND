package inu.codin.common.exception;

public class CodinBaseException extends RuntimeException {
    private final int code;
    
    public CodinBaseException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public CodinBaseException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
}
