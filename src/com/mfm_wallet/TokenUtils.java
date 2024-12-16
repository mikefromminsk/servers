package com.mfm_wallet;

import com.mfm_wallet.model.Account;
import com.mfm_wallet.model.Token;
import com.mfm_wallet.model.Transaction;

class TokenUtils extends AnalyticsUtils {
    static final String GENESIS_ADDRESS = "owner";

    String tokenSend(String scriptPath,
                     String domain,
                     String from_address,
                     String to_address,
                     Double amount,
                     String pass,
                     String delegate) {
        if (from_address.equals(to_address) && amount != 0) error("from_address and to_address are the same");
        String key = pass != null ? pass.split(":")[0] : null;
        String next_hash = pass != null ? pass.split(":")[1] : null;
        if (amount != Math.round(amount * 100) / 100.0) error("amount tick is 0.01");
        if (amount < 0) error("amount less than 0");
        if (from_address.equals(GENESIS_ADDRESS)) {
            if (domain.length() < 3 || domain.length() > 16) error("domain length has to be between 3 and 16");
            if (tokenBalance(domain, GENESIS_ADDRESS) == null) {
                Account owner = new Account();
                owner.domain = domain;
                owner.address = GENESIS_ADDRESS;
                owner.prev_key = "";
                owner.next_hash = "";
                owner.balance = amount;
                owner.delegate = "mfm-token/send.php";
                saveAccount(owner);
                if (amount > 0) {
                    saveToken(new Token(domain, to_address, amount, System.currentTimeMillis()));
                    trackAccumulate("tokens_count");
                }
            }
            if (tokenBalance(domain, to_address) == null) {
                Account to = new Account();
                to.domain = domain;
                to.address = to_address;
                to.prev_key = "";
                to.next_hash = next_hash;
                to.balance = 0.0;
                to.delegate = delegate;
                saveAccount(to);
            }
        }

        Account from = getAccount(domain, from_address);
        Account to = getAccount(domain, to_address);
        if (from.balance < amount)
            error(domain.toUpperCase() + " balance is not enough in " + from_address + " wallet. Balance: " + from.balance + " Need: " + amount);
        if (to == null) error(to_address + " receiver doesn't exist");
        if (from.delegate != null) {
            if (!from.delegate.equals(scriptPath))
                error("script " + scriptPath + " cannot use " + from_address + " address. Only " + from.delegate);
        } else {
            if (!from.next_hash.equals(md5(key))) error(domain + " key is not right");
        }

        if (from.delegate != null) {
            from.balance = Math.round((from.balance - amount) * 100) / 100.0;
            saveAccount(from);
        } else {
            from.prev_key = key;
            from.next_hash = next_hash;
            from.balance = Math.round((from.balance - amount) * 100) / 100.0;
            saveAccount(from);
        }

        double fee = 0;

        to.balance = Math.round((to.balance + amount - fee) * 100) / 100.0;
        saveAccount(to);

        saveTran(new Transaction(domain,
                from_address,
                to_address,
                amount,
                fee,
                key,
                next_hash,
                delegate,
                System.currentTimeMillis()
        ));
        return next_hash;
    }

}
