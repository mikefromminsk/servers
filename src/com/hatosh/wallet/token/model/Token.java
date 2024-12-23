package com.hatosh.wallet.token.model;

import com.metabrain.gdb.model.BigConstArrayCell;
import com.metabrain.gdb.model.String32;
import com.metabrain.gdb.utils.Bytes;

import java.io.ByteArrayOutputStream;


public class Token implements BigConstArrayCell {
    public String domain;
    public String owner;
    public Double supply;
    public Double price = 0.0;
    public Double price24 = 0.0;
    public Double volume24 = 0.0;
    public Long created;

    public Token() {
    }

    public Token(String domain, String owner, Double supply, Long created) {
        this.domain = domain;
        this.owner = owner;
        this.supply = supply;
        this.created = created;
    }

    public Token clone() {
        Token token = new Token();
        token.domain = domain;
        token.owner = owner;
        token.supply = supply;
        token.price = price;
        token.price24 = price24;
        token.volume24 = volume24;
        token.created = created;
        return token;
    }

    @Override
    public int getSize() {
        return String32.BYTES * 2 + 4 * Double.BYTES + Long.BYTES;
    }

    @Override
    public byte[] build() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            bos.write(Bytes.fromString(domain, String32.BYTES));
            bos.write(Bytes.fromString(owner, String32.BYTES));
            bos.write(Bytes.fromDouble(supply));
            bos.write(Bytes.fromDouble(price));
            bos.write(Bytes.fromDouble(price24));
            bos.write(Bytes.fromDouble(volume24));
            bos.write(Bytes.fromLong(created));
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public void parse(byte[] data) {
        domain = Bytes.toString(data, 0, String32.BYTES);
        owner = Bytes.toString(data, String32.BYTES, String32.BYTES);
        supply = Bytes.toDouble(data, String32.BYTES * 2);
        price = Bytes.toDouble(data, String32.BYTES * 2 + Double.BYTES);
        price24 = Bytes.toDouble(data, String32.BYTES * 2 + 2 * Double.BYTES);
        volume24 = Bytes.toDouble(data, String32.BYTES * 2 + 3 * Double.BYTES);
        created = Bytes.toLong(data, String32.BYTES * 2 + 4 * Double.BYTES);
    }
}