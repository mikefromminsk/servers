package com.mfm_wallet;

import com.sockets.test.SSLContextBuilder;
import fi.iki.elonen.NanoHTTPD;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static com.mfm_wallet.Utils.*;
import static fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR;
import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

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
            case "mfm-mining/mint.php":
                return new com.mfm_wallet.mfm_mining.Mint();
            case "mfm-mining/miner.php":
                return new com.mfm_wallet.mfm_mining.Miner();
            case "mfm-exchange/place.php":
                return new com.mfm_wallet.mfm_exchange.Place();
            case "mfm-exchange/cancel.php":
                return new com.mfm_wallet.mfm_exchange.Cancel();
            case "mfm-exchange/orderbook.php":
                return new com.mfm_wallet.mfm_exchange.OrderBook();
            case "mfm-exchange/orders.php":
                return new com.mfm_wallet.mfm_exchange.Orders();
            case "mfm-exchange/spred.php":
                return new com.mfm_wallet.mfm_exchange.Spred();
            case "mfm-wallet/home/api/main.php":
                return new com.mfm_wallet.mfm_wallet.Main();
            default:
                error("Unknown path: " + scriptPath);
        }
        return null;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Long start = time();
        String scriptPath = session.getUri().substring(1);
        Contract contract = getContract(scriptPath);
        try {
            contract.params = parseParams(session);
            contract.scriptPath = scriptPath;
            contract.run();
            contract.commit();
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
            System.out.println("error: " + scriptPath);
            return newFixedLengthResponse(INTERNAL_ERROR, MIME_JSON, gson.toJson(error));
        }
        contract.response.put("success", "true");
        System.out.println("success: " + scriptPath + " took " + (time() - start) + "ms");
        Response response = newFixedLengthResponse(OK, MIME_JSON, gson.toJson(contract.response));
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
