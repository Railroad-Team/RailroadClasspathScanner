package io.github.railroad.locomotive;

public interface Version {
    byte VERSION_1 = 0x01;

    byte[] VERSIONS = getVersions();
    byte MIN_VERSION = getMinVersion();
    byte MAX_VERSION = getMaxVersion();

    static boolean isValid(byte version) {
        for(byte validVersion : VERSIONS) {
            if(version == validVersion)
                return true;
        }

        return false;
    }

    private static byte[] getVersions() {
        return new byte[] {VERSION_1};
    }

    private static byte getMinVersion() {
        byte minVersion = VERSIONS[0];
        for(byte version : VERSIONS) {
            if(version < minVersion)
                minVersion = version;
        }

        return minVersion;
    }

    private static byte getMaxVersion() {
        byte maxVersion = VERSIONS[0];
        for(byte version : VERSIONS) {
            if(version > maxVersion)
                maxVersion = version;
        }

        return maxVersion;
    }
}
