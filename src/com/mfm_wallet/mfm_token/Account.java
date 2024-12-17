package com.mfm_wallet.mfm_token;

import com.mfm_wallet.Contract;

import java.util.Map;

public class Account extends Contract {
    @Override
    protected void run() {
        String domain = getRequired("domain");
        String address = getRequired("address");
        com.mfm_wallet.model.Account account = getAccount(domain, address);
        if (account == null) error("Account not found");
        response.put("account", account);
    }
}
