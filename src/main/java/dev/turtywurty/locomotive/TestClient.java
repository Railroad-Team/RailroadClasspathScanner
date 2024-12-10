package dev.turtywurty.locomotive;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

public class TestClient {
    public static void main(String[] args) throws IOException {
        var socket = new Socket("localhost", 25565);
        System.out.println("Connected to server");

        OutputStream outputStream = socket.getOutputStream();

        // Write the magic bytes
        writeByte(outputStream, (byte) 0x69);
        writeByte(outputStream, (byte) 0x42);

        // Write the version
        writeByte(outputStream, (byte) 0x01);

        // Write the method
        writeShort(outputStream, PacketMethod.PING);

        String payload = "Hello, World!";
        byte[] payloadBytes = payload.getBytes();
        // Write the length of the payload
        writeInt(outputStream, payloadBytes.length);

        // Write the payload
        writeByteArr(outputStream, payloadBytes);

        byte[] checksum = calculateChecksum(payloadBytes);
        // Write the checksum
        writeByteArr(outputStream, checksum);

        socket.close();
    }

    private static void writeByteArr(OutputStream outputStream, byte[] bytes) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(bytes).put(bytes);
        outputStream.write(buf.array());
    }

    private static void writeShort(OutputStream outputStream, short value) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(Short.BYTES).putShort(value);
        outputStream.write(buf.array());
    }

    private static void writeInt(OutputStream outputStream, int value) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES).putInt(value);
        outputStream.write(buf.array());
    }

    private static void writeByte(OutputStream outputStream, byte value) throws IOException {
        outputStream.write(value);
    }

    private static byte[] calculateChecksum(byte[] data) {
        ByteBuffer buf = ByteBuffer.allocate(32);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] checksum = digest.digest(data);
            if (checksum.length != 32) {
                throw new RuntimeException("Checksum length is not 32, got: " + checksum.length);
            }

            buf.put(checksum);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return buf.array();
    }
}
