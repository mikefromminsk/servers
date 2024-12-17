package com.mfm_wallet.mfm_token;

import com.mfm_wallet.Contract;

import java.util.Map;


public class Send extends Contract {

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
