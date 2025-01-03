package org.vavilon.token.tests;

import org.vavilon.token.TokenUtils;
import org.vavilon.token.model.Account;

public class SumTokens extends TokenUtils {
    @Override
    public void run() {
        String domain = getRequired("domain");
        double sum = 0;
        for (int i = 0; i < allAccounts.values.size(); i++) {
            Account account = allAccounts.values.get(i);
            if (account.domain.equals(domain)) {
                sum += account.balance;
            }
        }
        response.put("sum", sum);
    }
}
