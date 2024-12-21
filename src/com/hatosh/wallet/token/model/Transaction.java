package com.hatosh.wallet.token.model;

public class Transaction {
    public String domain;
    public String from;
    public String to;
    public Double amount;
    public Double fee;
    public String key;
    public String next_hash;
    public String delegate;
    public Long time;

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
}