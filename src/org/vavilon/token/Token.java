package org.vavilon.token;

import org.vavilon.token.model.Account;

public class Token extends TokenUtils {
    @Override
    public void run() {
        String domain = getRequired("domain");
        String address = getString("address");
        org.vavilon.token.model.Token token = getToken(domain);
        if (token == null) error("Token not found");
        response.put("token", token);
        if (address != null) {
            Account account = getAccount(domain, address);
            if (account != null)
                response.put("account", account);
        }
    }
}
