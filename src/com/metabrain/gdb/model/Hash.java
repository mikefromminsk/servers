package com.metabrain.gdb.model;

import com.metabrain.gdb.utils.Bytes;

public class Hash implements BigConstArrayCell {

    public long[] links = new long[16];

    @Override
    public int getSize() {
        return Long.BYTES * 16;
    }

    @Override
    public byte[] build() {
        return Bytes.fromLongArray(links);
    }

    @Override
    public void parse(byte[] data) {
        links = Bytes.toLongArray(data);
    }
}
