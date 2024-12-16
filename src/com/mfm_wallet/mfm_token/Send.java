package com.mfm_wallet.mfm_token;

import com.mfm_wallet.Contract;

import java.util.Map;


public class Send extends Contract {

    @Override
    public void run(Map<String, String> params) {
        String script_path = getRequired(params, "script_path");
        String domain = getRequired(params, "domain");
        String from_address = getRequired(params, "from_address");
        String to_address = getRequired(params, "to_address");
        Double amount = getDoubleRequired(params, "amount");
        String pass = getRequired(params, "pass");
        String delegate = getString(params, "delegate");
        response.put("next_hash", tokenSend(script_path, domain, from_address, to_address, amount, pass, delegate));
    }
}
