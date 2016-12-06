package me.mrkirby153.kcutils;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class UUIDFetcherTest {
    private static UUID NOTCH_UUID = java.util.UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
    private static UUID UUID = java.util.UUID.fromString("8c8785dd-f067-41e1-ad19-7559c842ca45");
    private static byte[] UUID_BYTES = {-116, -121, -123, -35, -16, 103, 65, -31, -83, 25, 117, 89, -56, 66, -54, 69};

    @Test
    public void fromBytes() throws Exception {
        Assert.assertEquals(UUID, UUIDFetcher.fromBytes(UUID_BYTES));
    }

    @Test
    public void getUUIDOf() throws Exception {
        Assert.assertEquals(NOTCH_UUID, UUIDFetcher.getUUIDOf("Notch"));
    }

    @Test
    public void toBytes() throws Exception {
        Assert.assertArrayEquals(UUID_BYTES, UUIDFetcher.toBytes(UUID));
    }

}