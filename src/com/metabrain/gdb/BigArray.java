package com.metabrain.gdb;

import com.metabrain.gdb.model.BigConstArrayCell;

public class BigArray<Type extends BigConstArrayCell> extends BigFile {

    public BigArray(String infinityFileID) {
        super(infinityFileID);
    }

    public void get(long index, Type dest) {
        byte[] readiedData = read(index * dest.getSize(), dest.getSize());
        if (readiedData != null)
            dest.parse(readiedData);
    }

    public void set(long index, Type obj) {
        write(index * obj.getSize(), obj.build());
    }

    public long add(Type obj) {
        long lastMaxPosition = super.add(obj.build());
        return lastMaxPosition / obj.getSize();
    }
}
