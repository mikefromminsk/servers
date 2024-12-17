package com.mfm_wallet.mfm_token;

import com.mfm_wallet.Contract;

import java.util.Map;

public class Tran extends Contract {
    @Override
    protected void run() {
        String next_hash = getRequired("next_hash");
        response.put("tran", getTran(next_hash));
    }
}
