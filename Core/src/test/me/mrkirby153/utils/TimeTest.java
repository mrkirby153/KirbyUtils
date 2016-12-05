package me.mrkirby153.utils;

import org.junit.Assert;
import org.junit.Test;

public class TimeTest {

    @Test
    public void convert() throws Exception {
        long seconds = 60000;
        Assert.assertEquals(1, Time.convert(0, seconds, Time.TimeUnit.MINUTES), 0.1);
    }

    @Test
    public void format() throws Exception {
        long seconds = 60000;
        Assert.assertEquals("1.0 Minutes", Time.format(1, seconds, Time.TimeUnit.MINUTES));
    }

    @Test
    public void trim() throws Exception {
        Assert.assertEquals(1.1, Time.trim(1, 1.1234), 0.01);
    }

}