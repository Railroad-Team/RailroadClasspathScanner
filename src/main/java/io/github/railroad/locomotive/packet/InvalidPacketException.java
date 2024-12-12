package io.github.railroad.locomotive.packet;

public class InvalidPacketException extends RuntimeException {
    public InvalidPacketException(String message) {
        super(message);
    }
}
