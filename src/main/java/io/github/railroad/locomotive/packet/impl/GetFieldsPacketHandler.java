package io.github.railroad.locomotive.packet.impl;

import io.github.railroad.locomotive.packet.PacketHandler;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

public class GetFieldsPacketHandler extends PacketHandler {
    public GetFieldsPacketHandler(byte version, byte[] payload) {
        super(version, payload);
    }

    @Override
    public byte[] getResponse() {
        String name = new String(this.payload, StandardCharsets.UTF_8);

        try {
            Class<?> clazz = Class.forName(name);
            Field[] fields = clazz.getDeclaredFields();
            StringBuilder response = new StringBuilder();

            for (Field field : fields) {
                response.append(field.getType().getName())
                        .append(" ")
                        .append(field.getName())
                        .append(";");
            }

            return response.toString().getBytes(StandardCharsets.UTF_8);
        } catch (ClassNotFoundException exception) {
            return exception.getMessage().getBytes(StandardCharsets.UTF_8);
        }
    }
}
