package com.metabrain.gdb.model;

import com.metabrain.gdb.utils.Bytes;

public interface BigArrayCell {
    void parse(Bytes data);
    byte[] build();
}
