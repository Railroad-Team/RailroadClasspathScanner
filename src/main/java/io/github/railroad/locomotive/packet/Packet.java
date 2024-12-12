package io.github.railroad.locomotive.packet;

import io.github.railroad.locomotive.PacketHelper;
import io.github.railroad.locomotive.Version;

import java.util.Arrays;
import java.util.Objects;

public record Packet(byte version, short methodId, int payloadLength, byte[] payload, long crc) {
    public PacketMethod getPacketMethod() {
        return PacketMethod.fromValue(methodId);
    }

    public void validate(byte expectedVersion) throws InvalidPacketException {
        if (!Version.isValid(version))
            throw new InvalidPacketException("Invalid version: " + version + ". Expected: " + Version.MIN_VERSION + "-" + Version.MAX_VERSION);

        if (!PacketMethod.isValid(methodId))
            throw new InvalidPacketException(methodId + " is not a valid method!");

        if (payloadLength != payload.length)
            throw new InvalidPacketException("Invalid payload length. Expected: " + payloadLength + ", received: " + payload.length);

        if (crc != PacketHelper.calculateCRC(payload))
            throw new InvalidPacketException("Checksum is invalid");

        if (version > expectedVersion)
            throw new InvalidPacketException("Version mismatch, expected: " + expectedVersion + ", received: " + version);
    }

    public void validate(byte expectedVersion, PacketMethod expectedMethod) throws InvalidPacketException {
        validate(expectedVersion);

        if (methodId != expectedMethod.getId()) {
            throw new InvalidPacketException("Method mismatch, expected: " + expectedMethod.getName() + ", received: " + getPacketMethod().getName());
        }
    }

    @Override
    public String toString() {
        return "PacketResponse{" +
                "version=" + version +
                ", methodId=" + getPacketMethod().getName() +
                ", payloadLength=" + payloadLength +
                ", payload=" + new String(payload) +
                ", crc=" + crc +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Packet that = (Packet) o;
        return crc == that.crc && version == that.version && methodId == that.methodId && payloadLength == that.payloadLength && Objects.deepEquals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, methodId, payloadLength, Arrays.hashCode(payload), crc);
    }
}
