package org.pogonin.shortlinkservice.core.utils.impl;

import org.pogonin.shortlinkservice.core.utils.CompressUtils;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@Component
public class UUIDCompressUtils implements CompressUtils {
    @Override
    public String compress(String src, int length) {
        UUID uuid = UUID.randomUUID();
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array()).substring(0, length);
    }
}
