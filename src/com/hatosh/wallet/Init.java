package com.hatosh.wallet;


import com.hatosh.wallet.data.Contract;

import java.util.Arrays;
import java.util.List;

import static com.hatosh.exchange.BotUtils.BOT_PREFIX;

public class Init extends Contract {

    class Distribution {
        String domain;
        Long supply;
        int miningPercent;
        int exchangePercent;

        public Distribution(String domain, Long supply, int miningPercent, int exchangePercent) {
            this.domain = domain;
            this.supply = supply;
            this.miningPercent = miningPercent;
            this.exchangePercent = exchangePercent;
        }
    }

    private void launchList(List<Distribution> tokens, String password) {
        for (Distribution token : tokens) {
            tokenRegToken(token.domain, GAS_OWNER, password, token.supply);
            if (token.exchangePercent > 0) {
                String botAddress = BOT_PREFIX + token.domain;
                botScriptReg(token.domain, botAddress);
                tokenSendAndCommit(token.domain, GAS_OWNER, botAddress, round(token.supply * token.exchangePercent / 100.0),
                        tokenPass(token.domain, GAS_OWNER, password), null);
                tokenSendAndCommit(GAS_DOMAIN, GAS_OWNER, botAddress, 100,
                        tokenPass(GAS_DOMAIN, GAS_OWNER, password), null);
            }
            if (token.miningPercent > 0) {
                tokenRegScript(token.domain, "mining", "mfm-mining/mint");
                tokenSendAndCommit(token.domain, GAS_OWNER, "mining", round(token.supply * token.miningPercent / 100.0),
                        tokenPass(token.domain, GAS_OWNER, password), null);
            }
            /*if (token.staking > 0) {
                tokenRegScript(domain, "staking", "mfm-bank/unstake");
                tokenSendAndCommit(domain, GAS_OWNER, "staking", Math.round(total * value / 100.0), password);
            }*/
        }
    }

    @Override
    public void run() {
        String password = getRequired("admin_password");

        tokenRegToken(GAS_DOMAIN, GAS_OWNER, password, 100_000_000L);
        tokenRegAccount(GAS_DOMAIN, "user", password);
        tokenSendAndCommit(GAS_DOMAIN, GAS_OWNER, "user", 1000, tokenPass(GAS_DOMAIN, GAS_OWNER, password), null);
        tokenRegAccount(GAS_DOMAIN, "support", password);
        //trackFill(GAS_DOMAIN, 1, 1);

        List<Distribution> tokens = Arrays.asList(
                new Distribution("gold", 1_000_000L, 90, 10),
                new Distribution("diamond", 100_000L, 100, 0),
                new Distribution("redstone", 5_000_000L, 100, 0),
                new Distribution("iron", 100_000_000L, 100, 0),
                new Distribution("bee_nest", 1_000_000L, 0, 90),
                new Distribution("emerald", 1_000_000L, 0, 100)
        );

        launchList(tokens, password);

        /*String nonce = requestEquals("/mfm-mining/miner", Map.of("domain", "diamond")).get("nonce");

        tokenRegAccount("diamond", "user", password);
        requestEquals("/mfm-mining/mint", Map.of(
                "domain", "diamond",
                "nonce", nonce,
                "gas_address", "user",
                "gas_pass", tokenPass(GAS_DOMAIN, "user", password)
        ));

        requestEquals("/mfm-exchange/place", Map.of(
                "domain", "diamond",
                "is_sell", 1,
                "address", "user",
                "price", 1,
                "amount", 1,
                "total", 1,
                "pass", tokenPass("diamond", "user", password)
        ));

        requestEquals("/mfm-exchange/place", Map.of(
                "domain", "diamond",
                "is_sell", 0,
                "address", "user",
                "price", 1,
                "amount", 1,
                "total", 1,
                "pass", tokenPass(GAS_DOMAIN, "user", password)
        ));

        requestEquals("/mfm-exchange/spred", Map.of("domain", "bee_nest"));

        delegateBalanceToScript(GAS_DOMAIN, GAS_OWNER, "bank", "mfm-bank/owner", password);

        String htaccess = fileGetContents("/mfm-root/.htaccess");
        filePutContents("/.htaccess", htaccess);

        System.out.println("{\"success\": true}");*/

    }
}