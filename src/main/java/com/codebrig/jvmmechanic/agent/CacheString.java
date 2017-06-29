package com.codebrig.jvmmechanic.agent;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class CacheString {

    public static CacheString readFromBuffer(ConfigProperties configProperties, ByteBuffer buffer) {
        if (buffer.get() == 1) {
            //cached
            int cacheIndex = buffer.getInt();
            String cacheString = cachedStrings.computeIfAbsent(cacheIndex, i -> (String) configProperties.get("cache_string_" + i));
            return new CacheString(configProperties, cacheString);
        } else {
            //raw string
            if (buffer.get() == 1) {
                int strLength = buffer.getInt();
                byte[] strBytes = new byte[strLength];
                buffer.get(strBytes);
                return new CacheString(configProperties, new String(strBytes));
            } else {
                return new CacheString(configProperties, null);
            }
        }
    }

    private static final AtomicInteger cacheIndexKey = new AtomicInteger();
    private static final Map<Integer, String> cachedStrings = Maps.newConcurrentMap();
    private static final Cache<String, Integer> cacheCandiates = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    private final ConfigProperties configProperties;
    private final String string;
    private int cacheIndex = -1;

    public CacheString(ConfigProperties configProperties, String string) {
        this.configProperties = configProperties;
        this.string = string;
        tryToCache();
    }

    public void writeToBuffer(ByteBuffer buffer) {
        buffer.put((byte)(isCachable() ? 1 : 0));
        if (isCachable()) {
            buffer.putInt(cacheIndex);
        } else {
            buffer.put((byte)(getString() != null ? 1 : 0));
            if (getString() != null) {
                byte[] strBytes = getString().getBytes();
                buffer.putInt(strBytes.length);
                buffer.put(strBytes);
            }
        }

    }

    public String getString() {
        return string;
    }

    public boolean isCachable() {
        return getCacheIndex() != -1;
    }

    public int getCacheIndex() {
        return cacheIndex;
    }

    public void setCacheIndex(int cacheIndex) {
        this.cacheIndex = cacheIndex;
    }

    private void tryToCache() {
        if (getString() == null || configProperties == null) {
            return;
        }

        Integer possibleCacheIndex = cacheCandiates.getIfPresent(getString());
        if (possibleCacheIndex != null) {
            configProperties.put("cache_string_" + possibleCacheIndex, getString());
            configProperties.sync();

            cachedStrings.put(possibleCacheIndex, getString());
            setCacheIndex(possibleCacheIndex);
        } else {
            cacheCandiates.put(getString(), cacheIndexKey.getAndIncrement());
        }
    }

    @Override
    public String toString() {
        return getString();
    }

}
