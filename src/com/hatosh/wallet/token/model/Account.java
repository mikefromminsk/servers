package com.hatosh.wallet.token.model;

public class Account {
    public String domain;
    public String address;
    public String prev_key;
    public String prev_hash;
    public String next_hash;
    public Double balance;
    public String delegate;
    public Token token;
    public String domains;

    public Account clone() {
        Account account = new Account();
        account.domain = domain;
        account.address = address;
        account.prev_key = prev_key;
        account.prev_hash = prev_hash;
        account.next_hash = next_hash;
        account.balance = balance;
        account.delegate = delegate;
        account.token = token;
        account.domains = domains;
        return account;
    }
}