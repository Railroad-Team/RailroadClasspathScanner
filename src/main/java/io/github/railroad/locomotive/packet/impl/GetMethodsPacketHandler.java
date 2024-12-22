package io.github.railroad.locomotive.packet.impl;

import io.github.railroad.locomotive.packet.PacketHandler;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class GetMethodsPacketHandler extends PacketHandler {
    public GetMethodsPacketHandler(byte version, byte[] payload) {
        super(version, payload);
    }

    @Override
    public byte[] getResponse() {
        var name = new String(this.payload, StandardCharsets.UTF_8);

        try {
            Class<?> clazz = Class.forName(name);
            Method[] methods = clazz.getDeclaredMethods();
            var response = new StringBuilder();

            for (Method method : methods) {
                response.append(method.getReturnType().getName())
                        .append(" ")
                        .append(method.getName())
                        .append("(")
                        .append(String.join(", ", Arrays.stream(method.getParameters())
                                .map(parameter -> parameter.getType().getName() + " " + parameter.getName())
                                .toArray(String[]::new)))
                        .append(");");
            }

            return response.toString().getBytes(StandardCharsets.UTF_8);
        } catch (ClassNotFoundException exception) {
            return exception.getMessage().getBytes(StandardCharsets.UTF_8);
        }
    }
}
