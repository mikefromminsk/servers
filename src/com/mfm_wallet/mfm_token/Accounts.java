package com.mfm_wallet.mfm_token;

import com.mfm_wallet.Contract;
import com.mfm_wallet.model.Account;

import java.util.List;
import java.util.Map;

public class Accounts extends Contract {

    @Override
    protected void run(Map<String, String> params) {
        String address = getRequired(params, "address");
        List<Account> accounts = getAccounts(address);
        for (Account account : accounts)
            account.token = allTokens.get(account.domain);
        response.put("accounts", accounts);
    }
}
