package com.hatosh.wallet.token;


public class Send extends TokenUtils {

    @Override
    public void run() {
        String domain = getRequired("domain");
        String from_address = getRequired("from_address");
        String to_address = getRequired("to_address");
        Double amount = getDoubleRequired("amount");
        String pass = getRequired("pass");
        String delegate = getString("delegate");
        response.put("next_hash", tokenSend(scriptPath, domain, from_address, to_address, amount, pass, delegate));
    }
}
