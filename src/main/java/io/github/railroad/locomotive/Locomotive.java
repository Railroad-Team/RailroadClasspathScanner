package io.github.railroad.locomotive;

import io.github.railroad.locomotive.packet.PacketHandler;
import io.github.railroad.locomotive.packet.Packet;
import io.github.railroad.locomotive.packet.PacketMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;

public class Locomotive {
    public static final int PORT = 29687;
    public static final Logger LOGGER = LoggerFactory.getLogger(Locomotive.class);

    public static void main(String[] args) {
        try (var serverSocket = new ServerSocket(PORT)) {
            LOGGER.info("Server started on port {}", PORT);

            while (serverSocket.isBound() && !serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException exception) {
            LOGGER.error("Error starting server", exception);
        }
    }

    public static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final InputStream input;
        private final OutputStream output;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;

            try {
                this.input = clientSocket.getInputStream();
                this.output = clientSocket.getOutputStream();
            } catch (IOException exception) {
                throw new RuntimeException("Error getting input/output streams: " + exception.getMessage());
            }
        }

        @Override
        public void run() {
            LOGGER.info("Client connected: {}", clientSocket.getInetAddress().getHostAddress());
            while (true) {
                try {
                    while (input.available() == 0 && !clientSocket.isClosed()) {
                        Thread.onSpinWait();
                    }

                    if (clientSocket.isClosed())
                        break;

                    Optional<Packet> readPacket = PacketHelper.readPacket(input);
                    if (readPacket.isEmpty())
                        continue;

                    Packet packet = readPacket.get();
                    packet.validate(Version.VERSION_1);

                    byte version = packet.version();
                    PacketMethod method = packet.getPacketMethod();
                    LOGGER.info("Received packet: {} v{} ({} bytes)", method.getName(), version, packet.payloadLength());

                    PacketHandler packetHandler = packet.getPacketMethod().createPacket(version, packet.payload());
                    byte[] responseData = packetHandler.getResponse();
                    PacketHelper.sendPacket(this.output, version, method, responseData);

                    LOGGER.info("Sent response: {} v{} ({} bytes)", method.getName(), version, responseData.length);
                } catch (IOException exception) {
                    LOGGER.error("Error reading packet", exception);
                    break;
                }
            }

            try {
                clientSocket.close();
            } catch (IOException exception) {
                LOGGER.error("Error closing client socket", exception);
            }
        }
    }
}