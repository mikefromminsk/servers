package com.hatosh.wallet.token.model;

public class Token {
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
}