package com.mfm_wallet.mfm_token;

import com.mfm_wallet.Contract;
import com.mfm_wallet.model.Account;

import java.util.Map;

public class Token extends Contract {
    @Override
    protected void run(Map<String, String> params) {
        String domain = getRequired(params, "domain");
        String address = getString(params, "address");
        com.mfm_wallet.model.Token token = allTokens.get(domain);
        if (token == null) error("Token not found");
        response.put("token", token);
        if (address != null) {
            Account account = getAccount(domain, address);
            if (account == null) error("Account not found");
            response.put("account", account);
        }
    }
}
