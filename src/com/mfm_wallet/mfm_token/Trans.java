package com.mfm_wallet.mfm_token;

import com.mfm_wallet.Contract;

import java.util.Map;

public class Trans extends Contract {

    @Override
    protected void run() {
        String domain = getRequired("domain");
        String from_address = getRequired("from_address");
        String to_address = getString("to_address");
        response.put("trans", tokenTrans(domain, from_address, to_address));
    }
}
