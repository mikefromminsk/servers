package com.hatosh.wallet.token.model;

import com.metabrain.gdb.model.BigConstArrayCell;
import com.metabrain.gdb.utils.Bytes;

import java.io.ByteArrayOutputStream;

public class Transaction implements BigConstArrayCell {
    public static final int md5Size = 32;
    public static final int domainSize = 16;
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
        return md5Size * 3 + domainSize + 2 * Double.BYTES + 2 * Long.BYTES;
    }

    @Override
    public byte[] build() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            bos.write(Bytes.fromString(domain, domainSize));
            bos.write(Bytes.fromString(from, domainSize));
            bos.write(Bytes.fromString(to, domainSize));
            bos.write(Bytes.fromDouble(amount));
            bos.write(Bytes.fromDouble(fee));
            bos.write(Bytes.fromString(key, md5Size));
            bos.write(Bytes.fromString(next_hash, md5Size));
            bos.write(Bytes.fromString(delegate, md5Size));
            bos.write(Bytes.fromLong(time));
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public void parse(byte[] data) {
        domain = Bytes.toString(data, 0, domainSize);
        from = Bytes.toString(data, domainSize, domainSize);
        to = Bytes.toString(data, 2 * domainSize, domainSize);
        amount = Bytes.toDouble(data, 3 * domainSize);
        fee = Bytes.toDouble(data, 3 * domainSize + Double.BYTES);
        key = Bytes.toString(data, 3 * domainSize + 2 * Double.BYTES, md5Size);
        next_hash = Bytes.toString(data, 3 * domainSize + 2 * Double.BYTES + md5Size, md5Size);
        delegate = Bytes.toString(data, 3 * domainSize + 2 * Double.BYTES + 2 * md5Size, md5Size);
        time = Bytes.toLong(data, 3 * domainSize + 2 * Double.BYTES + 3 * md5Size);
    }
}