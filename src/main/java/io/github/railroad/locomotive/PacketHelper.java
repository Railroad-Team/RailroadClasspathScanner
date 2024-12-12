package io.github.railroad.locomotive;

import io.github.railroad.locomotive.packet.PacketMethod;
import io.github.railroad.locomotive.packet.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.zip.CRC32C;

public class PacketHelper {
    public static long calculateCRC(byte[] data) {
        var crc = new CRC32C();
        crc.update(data);

       return crc.getValue();
    }

    public static void writeStartMarker(OutputStream outputStream) throws IOException {
        writeByte(outputStream, (byte) 0x69);
        writeByte(outputStream, (byte) 0x42);
    }

    public static void writeByteArr(OutputStream outputStream, byte[] bytes) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(bytes).put(bytes);
        outputStream.write(buf.array());
    }

    public static void writePacketMethod(OutputStream outputStream, PacketMethod method) throws IOException {
        writeShort(outputStream, (short) method.getId());
    }

    public static void writeShort(OutputStream outputStream, short value) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(Short.BYTES).putShort(value);
        outputStream.write(buf.array());
    }

    public static void writeInt(OutputStream outputStream, int value) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES).putInt(value);
        outputStream.write(buf.array());
    }

    public static void writeByte(OutputStream outputStream, byte value) throws IOException {
        outputStream.write(value);
    }

    public static void writeLong(OutputStream output, long value) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(Long.BYTES).putLong(value);
        output.write(buf.array());
    }

    public static void writePayload(OutputStream output, byte[] payload) throws IOException {
        writeInt(output, payload.length);
        writeByteArr(output, payload);
        writeLong(output, calculateCRC(payload));
    }

    public static void writePayload(OutputStream output, String payload) throws IOException {
        writePayload(output, payload.getBytes());
    }

    public static void sendPacket(OutputStream output, PacketMethod method, byte version, byte[] payload) throws IOException {
        writeStartMarker(output);
        writeByte(output, version);
        writePacketMethod(output, method);
        writePayload(output, payload);
        output.flush();
    }

    public static void sendPacket(OutputStream output, byte version, PacketMethod method, String payload) throws IOException {
        sendPacket(output, method, version, payload.getBytes());
    }

    public static byte[] readByteArr(InputStream input, int length) throws IOException {
        byte[] bytes = new byte[length];
        int read = input.read(bytes);

        if (read == -1)
            throw new IOException("End of stream reached");

        bytes = ByteBuffer.wrap(bytes).array();

        if (read != length)
            throw new IOException("Failed to read all bytes");

        return bytes;
    }

    public static byte readByte(InputStream input) throws IOException {
        int read = input.read();

        if (read == -1)
            throw new IOException("End of stream reached");

        return (byte) read;
    }

    public static short readShort(InputStream input) throws IOException {
        byte[] bytes = readByteArr(input, Short.BYTES);
        return ByteBuffer.wrap(bytes).getShort();
    }

    public static int readInt(InputStream input) throws IOException {
        byte[] bytes = readByteArr(input, Integer.BYTES);
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static long readLong(InputStream input) throws IOException {
        byte[] bytes = readByteArr(input, Long.BYTES);
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static boolean checkForStartMarker(InputStream input) throws IOException {
        byte[] startMarker = readByteArr(input, 2);
        return startMarker[0] == 0x69 && startMarker[1] == 0x42;
    }

    public static Optional<Packet> readPacket(InputStream input) throws IOException {
        if (!checkForStartMarker(input))
            return Optional.empty();

        byte version = readByte(input);
        short methodId = readShort(input);
        int length = readInt(input);
        byte[] payload = readByteArr(input, length);
        long crc = readLong(input);

        return Optional.of(new Packet(version, methodId, length, payload, crc));
    }
}
