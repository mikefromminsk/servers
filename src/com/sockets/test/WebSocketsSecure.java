package com.sockets.test;

import com.sockets.test.utils.MD5;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import javax.net.ssl.SSLContext;
import java.math.BigInteger;


public class WebSocketsSecure extends Sockets {

    public WebSocketsSecure(int port, String site_domain) {
        super(port);
        SSLContext context = SSLContextBuilder.from(site_domain);
        if (context != null) {
            System.out.println("WSS context activated for " + site_domain + " on port " + port);
            setWebSocketFactory(new DefaultSSLWebSocketServerFactory(context));
        }
    }

    public static WebSocketsSecure create(String domain) {
        String hash = MD5.hash(domain);
        BigInteger big = new BigInteger(hash, 16);
        return new WebSocketsSecure(8800 + big.mod(BigInteger.valueOf(16)).intValue(), domain);
    }
}
