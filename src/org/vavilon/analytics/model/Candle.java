package org.vavilon.analytics.model;

import com.metabrain.gdb.model.BigArrayCell;
import com.metabrain.gdb.utils.Bytes;

public class Candle implements BigArrayCell {
    public String period;
    public long time;
    public double low;
    public double high;
    public double open;
    public double close;
    public double value;


    @Override
    public byte[] build() {
        Bytes bytes = new Bytes();
        bytes.addString32(period);
        bytes.addLong(time);
        bytes.addDouble(low);
        bytes.addDouble(high);
        bytes.addDouble(open);
        bytes.addDouble(close);
        bytes.addDouble(value);
        return bytes.toByteArray();
    }

    @Override
    public void parse(Bytes data) {
        period = data.readString32();
        time = data.readLong();
        low = data.readDouble();
        high = data.readDouble();
        open = data.readDouble();
        close = data.readDouble();
        value = data.readDouble();
    }
}

