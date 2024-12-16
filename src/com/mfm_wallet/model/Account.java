package com.mfm_wallet.model;

public class Account {
    public String domain;
    public String address;
    public String prev_key;
    public String next_hash;
    public Double balance;
    public String delegate;
    public Token token;

    public Account clone() {
        Account account = new Account();
        account.domain = domain;
        account.address = address;
        account.prev_key = prev_key;
        account.next_hash = next_hash;
        account.balance = balance;
        account.delegate = delegate;
        return account;
    }
}