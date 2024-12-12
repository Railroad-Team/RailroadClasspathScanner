package io.github.railroad.locomotive.packet;

import lombok.Getter;

@Getter
public abstract class PacketHandler {
    protected final byte version;
    protected final byte[] payload;

    public PacketHandler(byte version, byte[] payload) {
        this.version = version;
        this.payload = payload;
    }

    public abstract byte[] getResponse();

    protected static void checkPayloadIsEmpty(byte[] payload) {
        if(payload.length != 0)
            throw new IllegalArgumentException("Payload must be empty");
    }

    public interface PacketFactory {
        PacketHandler create(byte version, byte[] payload);
    }
}
