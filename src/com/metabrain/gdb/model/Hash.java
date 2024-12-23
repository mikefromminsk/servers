package com.metabrain.gdb.model;

import com.metabrain.gdb.utils.Bytes;

public class Hash implements BigArrayCell {

    public long[] links = new long[16];

    @Override
    public byte[] build() {
        return Bytes.fromLongArray(links);
    }

    @Override
    public void parse(Bytes data) {
        links = data.readLongArray();
    }
}
