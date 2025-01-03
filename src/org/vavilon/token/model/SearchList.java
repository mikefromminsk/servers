package org.vavilon.token.model;

import com.metabrain.gdb.model.BigArrayCell;
import com.metabrain.gdb.utils.Bytes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchList implements BigArrayCell {

    public List<String> list = new ArrayList<>();

    public void add(String item) {
        if (!list.contains(item)){
            list.add(item);
            Collections.sort(list);
            if (list.size() > 10) {
                list.remove(list.size() - 1);
            }
        }
    }

    @Override
    public byte[] build() {
        Bytes bytes = new Bytes();
        for (int i = 0; i < 10; i++) {
            bytes.addString32(i < list.size() ? list.get(i) : null);
        }
        return bytes.toByteArray();
    }

    @Override
    public void parse(Bytes data) {
        for (int i = 0; i < 10; i++) {
            list.add(data.readString32());
        }
    }
}
