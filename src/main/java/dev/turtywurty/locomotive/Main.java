package dev.turtywurty.locomotive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Main {
    private static final int PORT = 25565;

    public static void main(String[] args) {
        try(var serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while(serverSocket.isBound() && !serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch(IOException exception) {
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
            } catch(IOException exception) {
                throw new RuntimeException("Error getting input/output streams: " + exception.getMessage());
            }
        }

        private byte[] readBytes(int length) throws IOException {
            byte[] bytes = new byte[length];
            int read = input.read(bytes);

            if(read == -1)
                throw new IOException("End of stream reached");

            bytes = ByteBuffer.wrap(bytes).array();

            if(read != length)
                throw new IOException("Failed to read all bytes");

            return bytes;
        }

        private byte readByte() throws IOException {
            int read = input.read();

            if(read == -1)
                throw new IOException("End of stream reached");

            return (byte) read;
        }

        private short readShort() throws IOException {
            byte[] bytes = readBytes(Short.BYTES);
            return ByteBuffer.wrap(bytes).getShort();
        }

        private int readInt() throws IOException {
            byte[] bytes = readBytes(Integer.BYTES);
            return ByteBuffer.wrap(bytes).getInt();
        }

        @Override
        public void run() {
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
            try {
                while(true) {
                    try {
                        byte[] startMarker = readBytes(2);

                        if(startMarker[0] != 0x69 || startMarker[1] != 0x42)
                            continue;

                        byte version = readByte();
                        System.out.println("Version: " + version);

                        short method = readShort();
                        System.out.println("Method: " + method);

                        int payloadLength = readInt();
                        System.out.println("Payload length: " + payloadLength);

                        byte[] payload = readBytes(payloadLength);
                        System.out.println("Payload: " + new String(payload));

                        byte[] crc = readBytes(32);
                        System.out.println("CRC: " + ByteBuffer.wrap(crc).getInt());

                        var packet = new Packet(version, method, payloadLength, payload, crc);
                    } catch(IOException exception) {
                        System.err.println("Error reading start marker: " + exception.getMessage());
                        exception.printStackTrace();
                        break;
                    }
                }
            } finally {
                try {
                    clientSocket.close();
                } catch(IOException exception) {
                    System.err.println("Error closing client socket: " + exception.getMessage());
                    exception.printStackTrace();
                }
            }
        }
    }
}