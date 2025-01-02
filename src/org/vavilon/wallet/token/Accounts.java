package org.vavilon.wallet.token;

import org.vavilon.wallet.token.model.Account;

import java.util.List;

public class Accounts extends TokenUtils {

    @Override
    public void run() {
        String address = getRequired("address");
        List<Account> accounts = getSubAccounts(address);
        for (Account account : accounts)
            account.token = getToken(account.domain);
        response.put("accounts", accounts);
    }
}