package com.hatosh.wallet.token.model;

import com.metabrain.gdb.model.BigArrayCell;
import com.metabrain.gdb.model.String32;
import com.metabrain.gdb.utils.Bytes;

import java.io.ByteArrayOutputStream;


public class Token implements BigArrayCell {
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
    public void parse(Bytes data) {
        domain = data.readString32();
        owner = data.readString32();
        supply = data.readDouble();
        price = data.readDouble();
        price24 = data.readDouble();
        volume24 = data.readDouble();
        created = data.readLong();
    }
}