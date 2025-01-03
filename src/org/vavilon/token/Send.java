package org.vavilon.token;


public class Send extends TokenUtils {

    @Override
    public void run() {
        String domain = getRequired("domain");
        String from_address = getRequired("from");
        String to_address = getRequired("to");
        Double amount = getDoubleRequired("amount");
        String pass = getString("pass");
        if (pass == null) {
            String next_hash = getRequired("next_hash");
            pass = getString("key", "") + ":" + next_hash;
        }
        String delegate = getString("delegate");
        response.put("next_hash", tokenSend(scriptPath, domain, from_address, to_address, amount, pass, delegate));
    }
}
