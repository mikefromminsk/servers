package org.vavilon.token.model;

import com.metabrain.gdb.model.BigArrayCell;
import com.metabrain.gdb.model.String32;
import com.metabrain.gdb.utils.Bytes;

import java.io.ByteArrayOutputStream;


public class Account implements BigArrayCell {
    public String domain;
    public String address;
    public String prev_key;
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
        account.next_hash = next_hash;
        account.next_domain = next_domain;
        account.balance = balance;
        account.delegate = delegate;
        account.token = token;
        return account;
    }

    @Override
    public byte[] build() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            bos.write(Bytes.fromString(domain, String32.BYTES));
            bos.write(Bytes.fromString(address, String32.BYTES));
            bos.write(Bytes.fromString(prev_key, String32.BYTES));
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
    public void parse(Bytes data) {
        domain = data.readString32();
        address = data.readString32();
        prev_key = data.readString32();
        next_hash = data.readString32();
        next_domain = data.readString32();
        delegate = data.readString32();
        balance = data.readDouble();
    }
}