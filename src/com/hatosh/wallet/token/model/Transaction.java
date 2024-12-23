package com.hatosh.wallet.token.model;

import com.metabrain.gdb.model.BigConstArrayCell;
import com.metabrain.gdb.model.String32;
import com.metabrain.gdb.utils.Bytes;

import java.io.ByteArrayOutputStream;

public class Transaction implements BigConstArrayCell {
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

    public Transaction() {
    }

    public Transaction(String domain,
                       String from,
                       String to,
                       Double amount,
                       Double fee,
                       String key,
                       String next_hash,
                       String delegate,
                       Long time) {
        this.domain = domain;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.fee = fee;
        this.key = key;
        this.next_hash = next_hash;
        this.delegate = delegate;
        this.time = time;
    }

    @Override
    public int getSize() {
        return String32.BYTES * 3 + String32.BYTES + 2 * Double.BYTES + 2 * Long.BYTES;
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
            bos.write(Bytes.fromString(delegate, String32.BYTES));
            bos.write(Bytes.fromLong(time));
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public void parse(byte[] data) {
        domain = Bytes.toString(data, 0, String32.BYTES);
        from = Bytes.toString(data, String32.BYTES, String32.BYTES);
        to = Bytes.toString(data, 2 * String32.BYTES, String32.BYTES);
        amount = Bytes.toDouble(data, 3 * String32.BYTES);
        fee = Bytes.toDouble(data, 3 * String32.BYTES + Double.BYTES);
        key = Bytes.toString(data, 3 * String32.BYTES + 2 * Double.BYTES, String32.BYTES);
        next_hash = Bytes.toString(data, 3 * String32.BYTES + 2 * Double.BYTES + String32.BYTES, String32.BYTES);
        delegate = Bytes.toString(data, 3 * String32.BYTES + 2 * Double.BYTES + 2 * String32.BYTES, String32.BYTES);
        time = Bytes.toLong(data, 3 * String32.BYTES + 2 * Double.BYTES + 3 * String32.BYTES);
    }
}