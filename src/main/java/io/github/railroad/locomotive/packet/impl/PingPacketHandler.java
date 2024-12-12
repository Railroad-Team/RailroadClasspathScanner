package io.github.railroad.locomotive.packet.impl;

import io.github.railroad.locomotive.packet.PacketHandler;

public class PingPacketHandler extends PacketHandler {
    public PingPacketHandler(byte version, byte[] payload) {
        super(version, payload);
        checkPayloadIsEmpty(payload);
    }

    @Override
    public byte[] getResponse() {
        return new byte[0];
    }
}
