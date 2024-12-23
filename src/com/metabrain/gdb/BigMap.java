package com.metabrain.gdb;

import com.metabrain.gdb.model.BigArrayCell;
import com.metabrain.gdb.model.Crc16;
import com.metabrain.gdb.model.Hash;
import com.metabrain.gdb.model.KeyVal;

public class BigMap<Val extends BigArrayCell> {
    private final BigArray<KeyVal> keys;
    private final BigArray<Hash> hashes;
    private final BigArray<Val> values;

    public BigMap(String infinityFileID, Class<Val> valClass) {
        keys = new BigArray<>(infinityFileID + ".keys", KeyVal.class);
        hashes = new BigArray<>(infinityFileID + ".hashes", Hash.class);
        values = new BigArray<>(infinityFileID + ".values", valClass);
        if (hashes.fileSize == 0) {
            hashes.add(new Hash());
            keys.add(new KeyVal());
        }
    }

    public Val get(String key) {
        String hexHash = String.format("%04x", Crc16.hash(key));
        long index = 0;
        for (int i = 0; i < 4; i++) {
            Hash currentHash = hashes.get(index);
            index = currentHash.links[Integer.parseInt("" + hexHash.charAt(i), 16)];
            if (index == 0) {
                break;
            }
        }
        while (index != 0) {
            KeyVal keyVal = keys.get(index);
            if (key.equals(keyVal.key)) {
                return values.get(keyVal.value_index);
            }
            index = keyVal.next_key_index;
        }
        return null;
    }

    public void put(String key, Val value) {
        String hexHash = String.format("%04x", Crc16.hash(key));
        long hashIndex = 0;
        int link_index = 0;
        // search in hash tree
        for (int i = 0; i < 3; i++) {
            Hash currentHash = hashes.get(hashIndex);
            link_index = Integer.parseInt("" + hexHash.charAt(i), 16);
            if (currentHash.links[link_index] == 0) {
                currentHash.links[link_index] = hashes.add(new Hash());
                hashes.set(hashIndex, currentHash);
            }
            hashIndex = currentHash.links[link_index];
        }
        // search in key chain
        Hash lastHash = hashes.get(hashIndex);
        link_index = Integer.parseInt("" + hexHash.charAt(3), 16);
        if (lastHash.links[link_index] == 0) {
            lastHash.links[link_index] = keys.add(new KeyVal(key, values.add(value)));
            hashes.set(hashIndex, lastHash);
        } else {
            long keyIndex = lastHash.links[link_index];
            while (true) {
                KeyVal keyVal = keys.get(keyIndex);
                if (key.equals(keyVal.key)) {
                    values.set(keyVal.value_index, value);
                    return;
                } else if (keyVal.next_key_index == 0) {
                    keyVal.next_key_index = keys.add(new KeyVal(key, values.add(value)));
                    keys.set(keyIndex, keyVal);
                    return;
                }
                keyIndex = keyVal.next_key_index;
            }
        }
    }

    public boolean containsKey(String key) {
        return get(key) != null;
    }

}