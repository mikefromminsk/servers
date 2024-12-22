package com.metabrain.gdb.model;

import com.metabrain.gdb.utils.Bytes;

public class LongCell implements BigConstArrayCell {
    public long value;

    public LongCell(long value) {
        this.value = value;
    }

    @Override
    public void parse(byte[] data) {
        value = Bytes.toLong(data);
    }

    public void setData(long data) {
        value = data;
    }

    @Override
    public byte[] build() {
        return Bytes.fromLong(value);
    }

    @Override
    public int getSize() {
        return Long.BYTES;
    }
}
