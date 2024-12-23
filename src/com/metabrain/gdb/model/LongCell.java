package com.metabrain.gdb.model;

import com.metabrain.gdb.utils.Bytes;

public class LongCell implements BigArrayCell {
    public long value;

    public LongCell(long value) {
        this.value = value;
    }

    public void setData(long data) {
        value = data;
    }

    @Override
    public byte[] build() {
        return Bytes.fromLong(value);
    }

    @Override
    public void parse(Bytes data) {
        value = data.readLong();
    }
}
