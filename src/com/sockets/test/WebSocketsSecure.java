package com.sockets.test;

import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import javax.net.ssl.SSLContext;


public class WebSocketsSecure extends Sockets {

    public WebSocketsSecure(int port) {
        super(port);
        SSLContext context = SSLContextBuilder.from(
                "C:\\Certbot\\live\\mytoken.space\\cert.pem",
                "C:\\Certbot\\live\\mytoken.space\\privkey.pem"
        );

        if (context != null) {
            System.out.println("WSS context activated");
            setWebSocketFactory(new DefaultSSLWebSocketServerFactory(context));
        }
    }
}
