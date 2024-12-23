package com.metabrain.gdb;

import com.metabrain.gdb.model.BigArrayCell;
import com.metabrain.gdb.utils.Bytes;

public class BigArray<Type extends BigArrayCell> extends BigFile {

    private final Class<Type> valClass;

    public BigArray(String infinityFileID, Class<Type> valClass) {
        super(infinityFileID);
        this.valClass = valClass;
    }

    public Type createValInstance() {
        try {
            return valClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Type get(long index) {
        Type result = createValInstance();
        byte[] data = result.build();
        byte[] readiedData = read(index * data.length, data.length);
        if (readiedData != null) {
            result.parse(new Bytes(readiedData));
            return result;
        }
        return null;
    }

    public void set(long index, Type obj) {
        byte[] data = obj.build();
        write(index * data.length, data);
    }

    public long add(Type obj) {
        byte[] data = obj.build();
        long lastMaxPosition = super.add(data);
        return lastMaxPosition / data.length - 1;
    }
}
