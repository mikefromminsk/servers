package com.hatosh.wallet.token;

public class Trans extends TokenUtils {

    @Override
    public void run() {
        String domain = getString("domain");
        String from_address = getRequired("address");
        String to_address = getString("to_address");
        response.put("trans", tokenTrans(domain, from_address, to_address));
    }
}
