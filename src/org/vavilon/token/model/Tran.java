package org.vavilon.token.model;

import com.metabrain.gdb.model.BigArrayCell;
import com.metabrain.gdb.model.String32;
import com.metabrain.gdb.utils.Bytes;

import java.io.ByteArrayOutputStream;

public class Tran implements BigArrayCell {
    public String domain;
    public String from;
    public String to;
    public Double amount;
    public Double fee;
    public String key;
    public String next_hash;
    public String prev_hash;
    public String delegate;
    public Long time;

    public Tran() {
    }

    public Tran(String domain,
                String from,
                String to,
                Double amount,
                Double fee,
                String key,
                String next_hash,
                String prev_hash,
                String delegate,
                Long time) {
        this.domain = domain;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.fee = fee;
        this.key = key;
        this.next_hash = next_hash;
        this.prev_hash = prev_hash;
        this.delegate = delegate;
        this.time = time;
    }

    @Override
    public byte[] build() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            bos.write(Bytes.fromString(domain, String32.BYTES));
            bos.write(Bytes.fromString(from, String32.BYTES));
            bos.write(Bytes.fromString(to, String32.BYTES));
            bos.write(Bytes.fromDouble(amount));
            bos.write(Bytes.fromDouble(fee));
            bos.write(Bytes.fromString(key, String32.BYTES));
            bos.write(Bytes.fromString(next_hash, String32.BYTES));
            bos.write(Bytes.fromString(prev_hash, String32.BYTES));
            bos.write(Bytes.fromString(delegate, String32.BYTES));
            bos.write(Bytes.fromLong(time));
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public void parse(Bytes data) {
        domain = data.readString32();
        from = data.readString32();
        to = data.readString32();
        amount = data.readDouble();
        fee =  data.readDouble();
        key = data.readString32();
        next_hash = data.readString32();
        prev_hash = data.readString32();
        delegate = data.readString32();
        time = data.readLong();
    }
}