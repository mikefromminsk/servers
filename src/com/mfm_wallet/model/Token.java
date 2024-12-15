package com.mfm_wallet.model;

import java.util.Date;

public class Token {
    public String domain;
    public String owner;
    public Double supply;
    public Double price = 0.0;
    public Double price24 = 0.0;
    public Double volume24 = 0.0;
    public Long created;

    public Token(String domain, String owner, Double supply, Long created) {
        this.domain = domain;
        this.owner = owner;
        this.supply = supply;
        this.created = created;
    }
}