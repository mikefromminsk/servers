package com.metabrain.gdb.model;

import com.metabrain.gdb.utils.Bytes;

import java.io.ByteArrayOutputStream;

public class KeyVal implements BigArrayCell {

    public String key;
    public long value_index;
    public long next_key_index;

    public KeyVal() {
    }

    public KeyVal(String key, long value_index) {
        this.key = key;
        this.value_index = value_index;
    }

    @Override
    public byte[] build() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(Bytes.fromString(key, String32.BYTES));
            bos.write(Bytes.fromLong(value_index));
            bos.write(Bytes.fromLong(next_key_index));
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void parse(Bytes data) {
        key = data.readString32();
        value_index = data.readLong();
        next_key_index = data.readLong();
    }
}
