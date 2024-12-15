package com.mfm_wallet;

import com.mfm_wallet.model.Account;
import com.mfm_wallet.model.Token;
import com.mfm_wallet.model.Transaction;

class TokenUtils extends AnalyticsUtils {
    static final String GAS_DOMAIN = "usdt";
    static final String GENESIS_ADDRESS = "owner";


    String tokenSend(String scriptPath,
                     String domain,
                     String fromAddress,
                     String toAddress,
                     Double amount,
                     String pass,
                     String delegate) {

        if (fromAddress.equals(toAddress) && amount != 0) error("from_address and to_address are the same");
        String key = pass != null ? pass.split(":")[0] : null;
        String nextHash = pass != null ? pass.split(":")[1] : null;
        if (amount != Math.round(amount * 100) / 100.0) error("amount tick is 0.01");
        if (amount < 0) error("amount less than 0");
        if (fromAddress.equals(GENESIS_ADDRESS)) {
            if (domain.length() < 3 || domain.length() > 16) error("domain length has to be between 3 and 16");
            if (tokenBalance(domain, GENESIS_ADDRESS) == null) {
                Account owner = new Account();
                owner.domain = domain;
                owner.address = GENESIS_ADDRESS;
                owner.prevKey = "";
                owner.nextHash = "";
                owner.balance = amount;
                owner.delegate = "mfm-token/send.php";
                saveAccount(owner);
                if (amount > 0) {
                    saveToken(new Token(domain, toAddress, amount, System.currentTimeMillis()));
                    trackAccumulate("tokens_count");
                }
            }
            Account gasAccount = getAccount(GAS_DOMAIN, toAddress);
            if (!domain.equals(GAS_DOMAIN) && gasAccount.delegate != null) {
                delegate = gasAccount.delegate;
            }
            if (tokenBalance(domain, toAddress) == null) {
                Account to = new Account();
                to.domain = domain;
                to.address = GENESIS_ADDRESS;
                to.prevKey = "";
                to.nextHash = nextHash;
                to.balance = 0.0;
                to.delegate = delegate;
                saveAccount(to);
            }
        }

        Account from = getAccount(domain, fromAddress);
        Account to = getAccount(domain, toAddress);
        if (from.balance < amount)
            error(domain.toUpperCase() + " balance is not enough in " + fromAddress + " wallet. Balance: " + from.balance + " Need: " + amount);
        if (to == null) error(toAddress + " receiver doesn't exist");
        if (from.delegate != null) {
            if (!from.delegate.equals(scriptPath))
                error("script " + scriptPath + " cannot use " + fromAddress + " address. Only " + from.delegate);
        } else {
            if (!from.nextHash.equals(md5(key))) error(domain + " key is not right");
        }

        if (from.delegate != null) {
            from.balance = Math.round((from.balance - amount) * 100) / 100.0;
            saveAccount(from);
        } else {
            from.prevKey = key;
            from.nextHash = nextHash;
            from.balance = Math.round((from.balance - amount) * 100) / 100.0;
            saveAccount(from);
        }

        double fee = 0;

        to.balance = Math.round((from.balance - fee) * 100) / 100.0;
        saveAccount(to);

        saveTran(new Transaction(domain,
                fromAddress,
                toAddress,
                amount,
                fee,
                key,
                nextHash,
                delegate,
                System.currentTimeMillis()
        ));

        return nextHash;
    }

}
