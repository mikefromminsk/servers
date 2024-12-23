package com.metabrain.gdb.test;

import com.metabrain.gdb.BigMap;
import com.metabrain.gdb.model.LongCell;

public class BigArrayTest {

    public static void main(String[] args) {
        BigMap<LongCell> bigArray = new BigMap<>("BigMap" + System.currentTimeMillis(), LongCell.class);
        LongCell longCell = new LongCell(132);
        bigArray.put("keyd", longCell);
        longCell.value = 123L;
        bigArray.put("sef3", longCell);
        longCell = bigArray.get("keyd");
        System.out.println(longCell.value);
    }
}
