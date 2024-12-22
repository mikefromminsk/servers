package com.hatosh.wallet.token;

import com.hatosh.wallet.token.model.Account;

import java.util.List;

public class Accounts extends TokenUtils {

    @Override
    public void run() {
        String address = getRequired("address");
        List<Account> accounts = getSubAccounts(address);
        for (Account account : accounts)
            account.token = tokensByDomain.get(account.domain);
        response.put("accounts", accounts);
    }
}