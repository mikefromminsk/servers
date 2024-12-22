package com.metabrain.gdb.model;

import com.metabrain.gdb.utils.Bytes;

import java.io.ByteArrayOutputStream;

public class KeyVal implements BigConstArrayCell {
    public static final int KEY_LENGTH = 16;

    public String key;
    public long value_index;
    public long next_key_index;

    @Override
    public int getSize() {
        return KeyVal.KEY_LENGTH + Long.BYTES * 2;
    }

    @Override
    public byte[] build() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(Bytes.fromString(key, 16));
            bos.write(Bytes.fromLong(value_index));
            bos.write(Bytes.fromLong(next_key_index));
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void parse(byte[] data) {
        key = Bytes.toString(data, 0, KEY_LENGTH);
        value_index = Bytes.toLong(data, KEY_LENGTH);
        next_key_index = Bytes.toLong(data, KEY_LENGTH + Long.BYTES);
    }
}
