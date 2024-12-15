package com.mfm_wallet;

import com.sockets.test.SSLContextBuilder;
import fi.iki.elonen.NanoHTTPD;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Map;

import static com.sockets.test.utils.Params.map;

public class Server extends TokenRequests {
    static String MIME_JSON = "application/json";

    public Server(int port) throws IOException {
        NanoHTTPD server = new NanoHTTPD(port) {
            @Override
            public Response serve(IHTTPSession session) {
                try {
                    String scriptPath = session.getUri().substring(1);
                    switch (scriptPath) {
                        case "mfm-token/send": {
                            Map<String, String> params = parseParams(session);
                            Map<String, String> response = map();

                            String domain = getRequired(params, "domain");
                            String from_address = getRequired(params, "from_address");
                            String to_address = getRequired(params, "to_address");
                            Double amount = getDoubleRequired(params, "amount");
                            String pass = getRequired(params, "pass");
                            String delegate = getString(params, "delegate");

                            response.put("next_hash", "" + tokenSend(scriptPath, domain, from_address, to_address, amount, pass, delegate));

                            return commit(response);
                        }
                        default:
                            error("Unknown path: " + session.getUri());
                    }
                } catch (Exception e) {
                    Map<String, String> error = map("message", e.getMessage());
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_JSON, gson.toJson(error));
                }
                return null;
            }
        };
        SSLContext context = SSLContextBuilder.from("mytoken.space");
        if (context != null) {
            server.makeSecure(context.getServerSocketFactory(), null);
        }
        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Server started, hit Enter to stop.\n");
    }
}
