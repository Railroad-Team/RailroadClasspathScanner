package io.github.railroad.locomotive.packet;

import io.github.railroad.locomotive.packet.impl.GetFieldsPacketHandler;
import io.github.railroad.locomotive.packet.impl.GetMethodsPacketHandler;
import io.github.railroad.locomotive.packet.impl.PingPacketHandler;
import lombok.Getter;

import java.util.Locale;

@Getter
public enum PacketMethod {
    PING(0x01, PingPacketHandler::new),
    GET_FIELDS(0x02, GetFieldsPacketHandler::new),
    GET_METHODS(0x03, GetMethodsPacketHandler::new);

    public static final PacketMethod[] VALUES = PacketMethod.values();

    private final String name = name().toLowerCase(Locale.ROOT);
    private final int id;
    private final PacketHandler.PacketFactory factory;

    PacketMethod(int id, PacketHandler.PacketFactory factory) {
        this.id = id;
        this.factory = factory;
    }

    public PacketHandler createPacket(byte version, byte[] payload) {
        return factory.create(version, payload);
    }

    public static boolean isValid(int id) {
        if(id < 0 || id > Short.MAX_VALUE)
            return false;

        for(PacketMethod method : PacketMethod.VALUES) {
            if(id == method.id)
                return true;
        }

        return false;
    }

    public static PacketMethod fromValue(int id) {
        if (id < 0 || id > Short.MAX_VALUE)
            throw new IllegalArgumentException("Invalid method: " + id);

        for(PacketMethod method : PacketMethod.VALUES) {
            if(method.id == id)
                return method;
        }

        throw new IllegalArgumentException("Invalid method: " + id);
    }
}
