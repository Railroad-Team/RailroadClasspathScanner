package io.github.railroad.locomotive;

import io.github.railroad.locomotive.packet.PacketMethod;
import io.github.railroad.locomotive.packet.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Optional;

public class TestClient {
    public static void main(String[] args) throws IOException {
        var socket = new Socket("localhost", Main.PORT);
        System.out.println("Connected to server");

        OutputStream outputStream = socket.getOutputStream();
        PacketHelper.sendPacket(outputStream, Version.VERSION_1, PacketMethod.GET_FIELDS, "java.lang.String");

        while (true) {
            Thread.onSpinWait();

            if (socket.isClosed())
                break;

            if (socket.getInputStream().available() == 0)
                continue;

            InputStream inputStream = socket.getInputStream();
            Optional<Packet> packetResponse = PacketHelper.readPacket(inputStream);
            if (packetResponse.isEmpty())
                continue;

            Packet response = packetResponse.get();
            response.validate(Version.VERSION_1, PacketMethod.GET_FIELDS);

            System.out.println("Received response: " + response);
            break;
        }

        socket.close();
    }
}
