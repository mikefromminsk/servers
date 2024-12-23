package com.hatosh.wallet.token.model;

import com.metabrain.gdb.model.BigConstArrayCell;
import com.metabrain.gdb.model.String32;
import com.metabrain.gdb.utils.Bytes;

import java.io.ByteArrayOutputStream;


public class Account implements BigConstArrayCell {
    public String domain;
    public String address;
    public String prev_key;
    public String prev_hash;
    public String next_hash;
    public String next_domain;
    public String delegate;
    public Double balance;
    public Token token;

    public Account clone() {
        Account account = new Account();
        account.domain = domain;
        account.address = address;
        account.prev_key = prev_key;
        account.prev_hash = prev_hash;
        account.next_hash = next_hash;
        account.next_domain = next_domain;
        account.balance = balance;
        account.delegate = delegate;
        account.token = token;
        return account;
    }

    @Override
    public int getSize() {
        return String32.BYTES * 7 + Double.BYTES;
    }

    @Override
    public byte[] build() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            bos.write(Bytes.fromString(domain, String32.BYTES));
            bos.write(Bytes.fromString(address, String32.BYTES));
            bos.write(Bytes.fromString(prev_key, String32.BYTES));
            bos.write(Bytes.fromString(prev_hash, String32.BYTES));
            bos.write(Bytes.fromString(next_hash, String32.BYTES));
            bos.write(Bytes.fromString(next_domain, String32.BYTES));
            bos.write(Bytes.fromString(delegate, String32.BYTES));
            bos.write(Bytes.fromDouble(balance));
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public void parse(byte[] data) {
        domain = Bytes.toString(data, 0, String32.BYTES);
        address = Bytes.toString(data, String32.BYTES, String32.BYTES);
        prev_key = Bytes.toString(data, String32.BYTES * 2, String32.BYTES);
        prev_hash = Bytes.toString(data, String32.BYTES * 3, String32.BYTES);
        next_hash = Bytes.toString(data, String32.BYTES * 4, String32.BYTES);
        next_domain = Bytes.toString(data, String32.BYTES * 5, String32.BYTES);
        delegate = Bytes.toString(data, String32.BYTES * 6, String32.BYTES);
        balance = Bytes.toDouble(data, String32.BYTES * 7);
    }
}