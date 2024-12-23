package com.metabrain.gdb.model;

import com.metabrain.gdb.utils.Bytes;

public class String32 implements BigConstArrayCell {

    public static final int BYTES = 32;
    public String str;

    @Override
    public int getSize() {
        return BYTES;
    }

    @Override
    public byte[] build() {
        return Bytes.fromString(str, BYTES);
    }

    @Override
    public void parse(byte[] data) {
        str = Bytes.toString(data, 0, BYTES);
    }
}
