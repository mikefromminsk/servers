package com.mfm_wallet;

import com.mfm_wallet.model.Account;
import com.mfm_wallet.model.Token;
import com.sockets.test.SSLContextBuilder;
import fi.iki.elonen.NanoHTTPD;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class Server extends TokenRequests {
    static String MIME_JSON = "application/json";

    public Server(int port) throws IOException {
        NanoHTTPD server = new NanoHTTPD(port) {
            @Override
            public Response serve(IHTTPSession session) {
                Long start = System.currentTimeMillis();
                String scriptPath = session.getUri().substring(1);
                Map<String, String> params = parseParams(session);
                Map<String, Object> response = new LinkedHashMap<>();
                try {
                    switch (scriptPath) {
                        case "mfm-token/send.php": {
                            String domain = getRequired(params, "domain");
                            String from_address = getRequired(params, "from_address");
                            String to_address = getRequired(params, "to_address");
                            Double amount = getDoubleRequired(params, "amount");
                            String pass = getRequired(params, "pass");
                            String delegate = getString(params, "delegate");
                            response.put("next_hash", tokenSend(scriptPath, domain, from_address, to_address, amount, pass, delegate));
                            commit(response);
                            break;
                        }
                        case "mfm-token/account.php": {
                            String domain = getRequired(params, "domain");
                            String address = getRequired(params, "address");
                            Account account = getAccount(domain, address);
                            if (account == null) error("Account not found");
                            response.put("account", account);
                            commit(response);
                            break;
                        }
                        case "mfm-token/accounts.php": {
                            String address = getRequired(params, "address");
                            response.put("accounts", getAccounts(address));
                            commit(response);
                            break;
                        }
                        case "mfm-token/search.php": {
                            String search_text = getRequired(params, "search_text");
                            List<Token> tokens = new ArrayList<>();
                            tokens.add(getToken(search_text));
                            response.put("tokens", tokens);
                            commit(response);
                            break;
                        }
                        case "mfm-token/tran.php": {
                            String next_hash = getRequired(params, "next_hash");
                            response.put("tran", getTran(next_hash));
                            commit(response);
                            break;
                        }
                        case "mfm-token/trans.php": {
                            String domain = getRequired(params, "domain");
                            String from_address = getRequired(params, "from_address");
                            String to_address = getString(params, "to_address");
                            response.put("trans", tokenTrans(domain, from_address, to_address));
                            commit(response);
                            break;
                        }
                        default:
                            error("Unknown path: " + session.getUri());
                            break;
                    }
                } catch (Exception e) {
                    Map<String, Object> error = new LinkedHashMap<>();
                    if (e.getMessage().charAt(0) == '{') {
                        error.put("message",gson.fromJson(e.getMessage(), Map.class));
                    } else {
                        error.put("message", e.getMessage());
                    }
                    StringWriter writer = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(writer);
                    e.printStackTrace(printWriter);
                    printWriter.flush();
                    LinkedList<String> stack = new LinkedList<>(Arrays.asList(writer.toString().split("\r\n\t")));
                    stack.removeFirst();
                    stack.removeFirst();
                    stack.removeLast();
                    stack.removeLast();
                    stack.removeLast();
                    error.put("stack", stack);
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_JSON, gson.toJson(error));
                }
                Long end = System.currentTimeMillis();
                System.out.println("Request: " + scriptPath + " took " + (end - start) + "ms");
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", gson.toJson(response));
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
