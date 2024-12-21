package com.hatosh.wallet;


import com.hatosh.wallet.data.Contract;

import java.util.Arrays;
import java.util.List;

public class Init extends Contract {

    class Distribution {
        String domain;
        Long supply;
        Long mining;
        Long exchange;

        Distribution(String domain, Long supply, Long mining, Long exchange) {
            this.domain = domain;
            this.supply = supply;
            this.mining = mining;
            this.exchange = exchange;
        }
    }

    private void launchList(List<Distribution> tokens, String password) {
        for (Distribution token : tokens) {
            tokenRegToken(token.domain, GAS_OWNER, password, token.supply);
            if (token.exchange > 0) {
                String botAddress = "bot_spred_" + token.domain;
                botScriptReg(token.domain, botAddress);
                tokenSendAndCommit(token.domain, GAS_OWNER, botAddress, round(token.supply * token.exchange / 100.0, 2),
                        tokenPass(token.domain, GAS_OWNER, password), null);
                tokenSendAndCommit(GAS_DOMAIN, GAS_OWNER, botAddress, token.exchange,
                        tokenPass(GAS_DOMAIN, GAS_OWNER, password), null);
            }
            if (token.mining > 0) {
                tokenRegScript(token.domain, "mining", "mfm-mining/mint.php");
                tokenSendAndCommit(token.domain, GAS_OWNER, "mining", round(token.supply * token.mining / 100.0, 2),
                        tokenPass(token.domain, GAS_OWNER, password), null);
            }
            /*if (token.staking > 0) {
                tokenRegScript(domain, "staking", "mfm-bank/unstake.php");
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
                new Distribution("diamond", 100_000L, 100L, 0L),
                new Distribution("gold", 1_000_000L, 100L, 0L),
                new Distribution("redstone", 5_000_000L, 100L, 0L),
                new Distribution("iron", 100_000_000L, 100L, 0L),
                new Distribution("bee_nest", 1_000_000L, 0L, 90L),
                new Distribution("emerald", 1_000_000L, 0L, 100L)
        );

        launchList(tokens, password);

        /*String nonce = requestEquals("/mfm-mining/miner.php", Map.of("domain", "diamond")).get("nonce");

        tokenRegAccount("diamond", "user", password);
        requestEquals("/mfm-mining/mint.php", Map.of(
                "domain", "diamond",
                "nonce", nonce,
                "gas_address", "user",
                "gas_pass", tokenPass(GAS_DOMAIN, "user", password)
        ));

        requestEquals("/mfm-exchange/place.php", Map.of(
                "domain", "diamond",
                "is_sell", 1,
                "address", "user",
                "price", 1,
                "amount", 1,
                "total", 1,
                "pass", tokenPass("diamond", "user", password)
        ));

        requestEquals("/mfm-exchange/place.php", Map.of(
                "domain", "diamond",
                "is_sell", 0,
                "address", "user",
                "price", 1,
                "amount", 1,
                "total", 1,
                "pass", tokenPass(GAS_DOMAIN, "user", password)
        ));

        requestEquals("/mfm-exchange/spred.php", Map.of("domain", "bee_nest"));

        delegateBalanceToScript(GAS_DOMAIN, GAS_OWNER, "bank", "mfm-bank/owner.php", password);

        String htaccess = fileGetContents("/mfm-root/.htaccess");
        filePutContents("/.htaccess", htaccess);

        System.out.println("{\"success\": true}");*/

    }
}