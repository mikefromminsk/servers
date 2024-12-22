package com.metabrain.gdb.model;


public class StringCell implements BigConstArrayCell {

    public String str;

    @Override
    public void parse(byte[] data) {
        str = new String(data);
    }

    @Override
    public byte[] build() {
        return str.getBytes();
    }

    @Override
    public int getSize() {
        return str.length();
    }
}