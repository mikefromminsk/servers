package com.mfm_wallet.model;

import java.util.Date;

public class Transaction {
    public String domain;
    public String from;
    public String to;
    public Double amount;
    public Double fee;
    public String key;
    public String nextHash;
    public String delegate;
    public Long time;

    public Transaction(String domain,
                       String from,
                       String to,
                       Double amount,
                       Double fee,
                       String key,
                       String nextHash,
                       String delegate,
                       Long time) {
        this.domain = domain;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.fee = fee;
        this.key = key;
        this.nextHash = nextHash;
        this.delegate = delegate;
        this.time = time;
    }
}