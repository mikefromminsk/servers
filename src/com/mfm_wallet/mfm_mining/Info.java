package com.mfm_wallet.mfm_mining;

import com.mfm_wallet.mfm_data.DataContract;

import static com.mfm_wallet.mfm_mining.Mint.REWARD_MULTIPLIER;

public class Info extends DataContract {
    @Override
    protected void run() {
        String domain = getRequired("domain");
        response.put("last_hash", dataGet("mining/" + domain + "/last_hash"));
        response.put("difficulty", dataGet("mining/" + domain + "/difficulty"));
        response.put("bank", tokenBalance(domain, "mining"));
        response.put("last_reward", round(tokenBalance(domain, "mining") * REWARD_MULTIPLIER, 2));
    }
}
