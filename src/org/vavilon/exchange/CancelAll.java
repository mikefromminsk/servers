package org.vavilon.exchange;

public class CancelAll extends ExchangeUtils {
    @Override
    public void run() {
        String domain = getRequired("domain");
        String address = getRequired("address");
        cancelAll(domain, address);
    }
}
