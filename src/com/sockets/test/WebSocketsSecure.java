package com.sockets.test;

import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import javax.net.ssl.SSLContext;


public class WebSocketsSecure extends Sockets {

    public WebSocketsSecure(int port) {
        super(port);
        SSLContext context = SSLContextBuilder.from(
                "C:\\wamp\\bin\\apache\\apache2.4.51\\conf\\ssl\\webserver.cert",
                "C:\\wamp\\bin\\apache\\apache2.4.51\\conf\\ssl\\webserver.key"
        );

        if (context != null) {
            System.out.println("WSS context activated");
            setWebSocketFactory(new DefaultSSLWebSocketServerFactory(context));
        }
    }
}
