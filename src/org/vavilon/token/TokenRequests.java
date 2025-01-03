package org.vavilon.token;

import static org.vavilon.contracts.Contract.GAS_DOMAIN;
import static org.vavilon.utils.Params.map;

public abstract class TokenRequests extends TokenUtils {

    public boolean tokenRegScript(String domain, String address, String script) {
        if (getAccount(domain, address) == null) {
            tokenSendAndCommit(domain, GENESIS_ADDRESS, address, 0L, tokenPass(domain, address), script);
            return true;
        }
        return false;
    }

    public void tokenRegToken(String domain, String address, String password, Long amount) {
        tokenSendAndCommit(domain, GENESIS_ADDRESS, address, amount, ":" + tokenNextHash(domain, address, password, ""), null);
    }

    public void tokenRegAccount(String domain, String address, String password) {
        tokenRegToken(domain, address, password, 0L);
    }

    public void tokenSendAndCommit(String domain, String from, String to, double amount, String pass, String delegate) {
        new Send().run("mfm-token/send", map(
                "domain", domain,
                "from", from,
                "to", to,
                "pass", pass,
                "amount", "" + amount,
                "delegate", delegate
        )).commit();
    }

    public boolean botScriptReg(String domain, String botAddress) {
        String placeScript = "mfm-exchange/place";
        tokenRegScript(domain, botAddress, placeScript);
        return tokenRegScript(GAS_DOMAIN, botAddress, placeScript);
    }

/*


    public static boolean tokenDelegate(String domain, String address, String pass, String script) throws SQLException {
        if (getAccount(domain, address) != null) {
            return requestEquals("/mfm-token/send", map(
                    "domain", domain,
                    "from_address", GENESIS_ADDRESS,
                    "to_address", address,
                    "amount", "0",
                    "pass", pass
            ));
        } else {
            return false;
        }
    }

    public static void tokenChangePass(String domain, String address, String pass), SQLException {
        tokenSend(domain, address, address, 0, pass);
    }*/

}
