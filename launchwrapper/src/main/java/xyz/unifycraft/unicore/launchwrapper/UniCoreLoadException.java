package xyz.unifycraft.unicore.launchwrapper;

public class UniCoreLoadException extends RuntimeException {
    public UniCoreLoadException(String message) {
        super(message);
    }

    public UniCoreLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}