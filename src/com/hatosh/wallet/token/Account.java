package com.hatosh.wallet.token;

public class Account extends TokenUtils {
    @Override
    public void run() {
        String domain = getRequired("domain");
        String address = getRequired("address");
        com.hatosh.wallet.token.model.Account account = getAccount(domain, address);
        if (account == null) error("Account not found");
        response.put("account", account);
    }
}
