package com.metabrain.gdb.test;

import com.metabrain.gdb.BigMap;
import com.metabrain.gdb.model.LongCell;

public class BigArrayTest {

    public static void main(String[] args) {
        BigMap<LongCell> bigArray = new BigMap<>("BigMap" + System.currentTimeMillis());
        LongCell longCell = new LongCell(132);
        bigArray.put("keyd", longCell);
        longCell.value = 123L;
        bigArray.put("sef3", longCell);
        bigArray.get("keyd", longCell);
        System.out.println(longCell.value);
    }
}
