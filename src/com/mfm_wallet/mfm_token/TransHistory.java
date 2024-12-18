package com.mfm_wallet.mfm_token;

import com.mfm_wallet.Contract;

public class TransHistory extends Contract {
    @Override
    protected void run() {
        response.put("trans", transHistory);
    }
}
