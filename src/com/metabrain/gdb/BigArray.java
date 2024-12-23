package com.metabrain.gdb;

import com.metabrain.gdb.model.BigConstArrayCell;

public class BigArray<Type extends BigConstArrayCell> extends BigFile {

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
        byte[] readiedData = read(index * result.getSize(), result.getSize());
        if (readiedData != null) {
            result.parse(readiedData);
            return result;
        }
        return null;
    }

    public void set(long index, Type obj) {
        write(index * obj.getSize(), obj.build());
    }

    public long add(Type obj) {
        long lastMaxPosition = super.add(obj.build());
        return lastMaxPosition / obj.getSize();
    }
}
