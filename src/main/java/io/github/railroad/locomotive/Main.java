package io.github.railroad.locomotive;

import io.github.railroad.locomotive.packet.PacketHandler;
import io.github.railroad.locomotive.packet.Packet;
import io.github.railroad.locomotive.packet.PacketMethod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;

public class Main {
    public static final int PORT = 29687;

    public static void main(String[] args) {
        try (var serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (serverSocket.isBound() && !serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException exception) {
            System.err.println("Error starting server: " + exception.getMessage());
            exception.printStackTrace();
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
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
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
                    System.out.println("Received packet: " + method.getName() + " v" + version + " (" + packet.payloadLength() + " bytes)");

                    PacketHandler packetHandler = packet.getPacketMethod().createPacket(version, packet.payload());
                    byte[] responseData = packetHandler.getResponse();
                    PacketHelper.sendPacket(this.output, method, version, responseData);

                    System.out.println("Response sent");
                } catch (IOException exception) {
                    System.err.println("Error reading start marker: " + exception.getMessage());
                    exception.printStackTrace();
                    break;
                }
            }

            try {
                clientSocket.close();
            } catch (IOException exception) {
                System.err.println("Error closing client socket: " + exception.getMessage());
                exception.printStackTrace();
            }
        }
    }
}