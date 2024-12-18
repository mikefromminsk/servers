package com.mfm_wallet.mfm_token;

import com.mfm_wallet.Contract;
import com.mfm_wallet.model.Account;

import java.util.List;

public class Accounts extends Contract {

    @Override
    protected void run() {
        String address = getRequired("address");
        List<Account> accounts = getAccounts(address);
        for (Account account : accounts)
            account.token = tokensByDomain.get(account.domain);
        response.put("accounts", accounts);
    }
}
