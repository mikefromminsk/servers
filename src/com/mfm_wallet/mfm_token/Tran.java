package com.mfm_wallet.mfm_token;

import com.mfm_wallet.Contract;

import java.util.Map;

public class Tran extends Contract {
    @Override
    protected void run(Map<String, String> params) {
        String next_hash = getRequired(params, "next_hash");
        response.put("tran", getTran(next_hash));
    }
}
