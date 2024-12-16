package com.mfm_wallet;

import com.mfm_wallet.mfm_token.Send;
import com.mfm_wallet.model.Account;
import com.mfm_wallet.model.Token;
import com.sockets.test.SSLContextBuilder;
import fi.iki.elonen.NanoHTTPD;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static com.mfm_wallet.BaseUtils.*;

public class Server extends NanoHTTPD {
    static String MIME_JSON = "application/json";

    Contract getContract(String scriptPath) {
        switch (scriptPath) {
            case "mfm-token/send.php":
                return new com.mfm_wallet.mfm_token.Send();
            case "mfm-token/account.php":
                return new com.mfm_wallet.mfm_token.Account();
            case "mfm-token/accounts.php":
                return new com.mfm_wallet.mfm_token.Accounts();
            case "mfm-token/search.php":
                return new com.mfm_wallet.mfm_token.Search();
            case "mfm-token/tran.php":
                return new com.mfm_wallet.mfm_token.Tran();
            case "mfm-token/trans.php":
                return new com.mfm_wallet.mfm_token.Trans();
            case "mfm-token/profile.php":
                return new com.mfm_wallet.mfm_token.Token();
            case "mfm-token/token_info.php":
                return new com.mfm_wallet.mfm_token.TokenAnalytics();
            case "mfm-analytics/track.php":
                return new com.mfm_wallet.mfm_analytics.Track();
            case "mfm-analytics/events.php":
                return new com.mfm_wallet.mfm_analytics.Events();
            case "mfm-analytics/candles.php":
                return new com.mfm_wallet.mfm_analytics.Candles();
            case "mfm-analytics/funnel.php":
                return new com.mfm_wallet.mfm_analytics.Funnel();
            default:
                error("Unknown path: " + scriptPath);
        }
        return null;
    }


    void commit(Map<String, Object> response) {
        response.put("success", "true");
    }

    @Override
    public Response serve(IHTTPSession session) {
        Long start = System.currentTimeMillis();
        Map<String, String> params = parseParams(session);
        Contract contract = getContract(params.get("script_path"));
        try {
            contract.run(params);
            commit(contract.response);
        } catch (Exception e) {
            Map<String, Object> error = new LinkedHashMap<>();
            if (e.getMessage().charAt(0) == '{') {
                error.put("message", gson.fromJson(e.getMessage(), Map.class));
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
            System.out.println("error: " + params.get("script_path"));
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_JSON, gson.toJson(error));
        }
        contract.commit();
        System.out.println("success: " + params.get("script_path") + " took " + (System.currentTimeMillis() - start) + "ms");
        Response response = newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "application/json",
                gson.toJson(contract.response));
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    public Server(int port) throws IOException {
        super(port);
        SSLContext context = SSLContextBuilder.from("mytoken.space");
        if (context != null) {
            makeSecure(context.getServerSocketFactory(), null);
        }
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Server started, hit Enter to stop.\n");
    }
}
