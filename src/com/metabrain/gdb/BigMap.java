package com.metabrain.gdb;

import com.metabrain.gdb.model.BigConstArrayCell;
import com.metabrain.gdb.model.Crc16;
import com.metabrain.gdb.model.Hash;
import com.metabrain.gdb.model.KeyVal;

public class BigMap<Val extends BigConstArrayCell> {
    private final BigArray<KeyVal> keys;
    private final BigArray<Hash> hashes;
    private final BigArray<Val> values;

    public BigMap(String infinityFileID) {
        keys = new BigArray<>(infinityFileID + ".keys");
        hashes = new BigArray<>(infinityFileID + ".hashes");
        values = new BigArray<>(infinityFileID + ".values");
        if (hashes.fileData.sumFilesSize == 0) {
            hashes.add(new Hash());
            keys.add(new KeyVal());
            keys.add(new KeyVal());
            keys.add(new KeyVal());
            keys.add(new KeyVal());
            keys.add(new KeyVal());
            keys.add(new KeyVal());
        }
    }

    public void get(String key, Val value) {
        String hexHash = Integer.toHexString(Crc16.hash(key));
        int link_index = 0;
        long index = 0;
        Hash currentHash = new Hash();
        for (int i = 0; i < 4; i++) {
            hashes.get(index, currentHash);
            link_index = Integer.parseInt("" + hexHash.charAt(i), 16);
            index = currentHash.links[link_index];
            if (index == 0) {
                break;
            }
        }
        KeyVal keyVal = new KeyVal();
        while (index != 0) {
            keys.get(index, keyVal);
            if (key.equals(keyVal.key)) {
                values.get(keyVal.value_index, value);
                break;
            }
            index = keyVal.next_key_index;
        }
    }

    public void put(String key, Val value) {
        String hexHash = Integer.toHexString(Crc16.hash(key));
        long index = 0;
        int link_index = 0;
        Hash currentHash = new Hash();
        for (int i = 0; i < 3; i++) {
            hashes.get(index, currentHash);
            link_index = Integer.parseInt("" + hexHash.charAt(i), 16);
            if (currentHash.links[link_index] == 0) {
                currentHash.links[link_index] = hashes.add(new Hash());
                hashes.set(index, currentHash);
            }
            index = currentHash.links[link_index];
        }
        hashes.get(index, currentHash);
        link_index = Integer.parseInt("" + hexHash.charAt(3), 16);
        KeyVal keyVal = new KeyVal();
        if (currentHash.links[link_index] == 0) {
            keyVal.key = key;
            keyVal.value_index = values.add(value);
            currentHash.links[link_index] = keys.add(keyVal);
            hashes.set(index, currentHash);
        } else {
            while (true) {
                keys.get(index, keyVal);
                if (key.equals(keyVal.key)) {
                    keyVal.value_index = values.add(value);
                    keys.set(index, keyVal);
                    break;
                } else if (keyVal.next_key_index == 0) {
                    KeyVal newKeyVal = new KeyVal();
                    newKeyVal.key = key;
                    newKeyVal.value_index = values.add(value);
                    keyVal.next_key_index = keys.add(newKeyVal);
                    keys.set(index, keyVal);
                    break;
                }
                index = keyVal.next_key_index;
            }
        }
    }
}