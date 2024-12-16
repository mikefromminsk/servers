package com.mfm_wallet.mfm_token;

import com.mfm_wallet.Contract;

import java.util.Map;

public class Account extends Contract {
    @Override
    protected void run(Map<String, String> params) {
        String domain = getRequired(params, "domain");
        String address = getRequired(params, "address");
        com.mfm_wallet.model.Account account = getAccount(domain, address);
        if (account == null) error("Account not found");
        response.put("account", account);
    }
}
