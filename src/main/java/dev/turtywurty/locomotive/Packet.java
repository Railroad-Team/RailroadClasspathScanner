package dev.turtywurty.locomotive;

public record Packet(byte version, short method, int length, byte[] data, byte[] checksum) {
}
