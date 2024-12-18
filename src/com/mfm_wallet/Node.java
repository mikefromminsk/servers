package com.mfm_wallet;

import com.mfm_wallet.mfm_analytics.Candles;
import com.mfm_wallet.mfm_analytics.Events;
import com.mfm_wallet.mfm_analytics.Funnel;
import com.mfm_wallet.mfm_analytics.Track;
import com.mfm_wallet.mfm_exchange.*;
import com.mfm_wallet.mfm_mining.Miner;
import com.mfm_wallet.mfm_mining.Mint;
import com.mfm_wallet.mfm_token.*;
import com.mfm_wallet.mfm_wallet.Main;
import com.mfm_wallet.model.Transaction;
import com.sockets.test.WssServer;
import com.sockets.test.model.Message;
import com.sockets.test.utils.Request;
import com.sockets.test.utils.SSLContextBuilder;
import fi.iki.elonen.NanoHTTPD;
import org.java_websocket.WebSocket;

import javax.net.ssl.SSLContext;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;

import static com.mfm_wallet.Utils.*;
import static com.sockets.test.utils.Params.map;
import static fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR;
import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

public class Node extends NanoHTTPD {
    static final String MIME_JSON = "application/json";
    static final int HTTPS_START_RANGE = 8000;
    static final int WSS_START_RANGE = 8800;

    static String masterNode;
    static WssServer wssServer;

    class TransactionHistory {
        List<Transaction> trans;
    }

    public Node(String domain, String masterNode) {
        super(HTTPS_START_RANGE + getPortOffset(domain));
        Node.masterNode = masterNode;
        SSLContext context = SSLContextBuilder.from(domain);
        if (context != null) {
            makeSecure(context.getServerSocketFactory(), null);
        }
        if (masterNode == null) {
            try {
                String transStr = readFile("trans.json");
                TransactionHistory responseObj = gson.fromJson(transStr, TransactionHistory.class);
                Send send = new Send();
                for (Transaction tran : responseObj.trans) {
                    send.tokenSend(null, tran.domain, tran.from, tran.to, tran.amount, tran.key + ":" + tran.next_hash, tran.delegate);
                }
                send.commit();
                System.out.println("Node loaded");
            } catch (Exception e) {
            }
        } else {
            int masterPortOffset = getPortOffset(masterNode);
            Request.post(masterNode + ":" + (HTTPS_START_RANGE + masterPortOffset) + "/mfm-token/trans_history.php", map(
                    "current", "w"
            ), response -> {
                TransactionHistory responseObj = gson.fromJson(response, TransactionHistory.class);
                Send send = new Send();
                for (Transaction tran : responseObj.trans) {
                    send.tokenSend(null, tran.domain, tran.from, tran.to, tran.amount, tran.key + ":" + tran.next_hash, tran.delegate);
                }
                send.commit();
                System.out.println("Node synced with " + masterNode);
            });
        }
        wssServer = new WssServer(WSS_START_RANGE + getPortOffset(domain), domain);
    }

    public void start() {
        try {
            super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            wssServer.start();
            System.out.println("Node started on " + getListeningPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Contract getContract(String scriptPath) {
        switch (scriptPath) {
            case "mfm-token/send.php":
                return new Send();
            case "mfm-token/account.php":
                return new Account();
            case "mfm-token/accounts.php":
                return new Accounts();
            case "mfm-token/search.php":
                return new Search();
            case "mfm-token/tran.php":
                return new Tran();
            case "mfm-token/trans.php":
                return new Trans();
            case "mfm-token/profile.php":
                return new Token();
            case "mfm-token/token_info.php":
                return new TokenAnalytics();
            case "mfm-token/trans_history.php":
                return new TransHistory();
            case "mfm-analytics/track.php":
                return new Track();
            case "mfm-analytics/events.php":
                return new Events();
            case "mfm-analytics/candles.php":
                return new Candles();
            case "mfm-analytics/funnel.php":
                return new Funnel();
            case "mfm-mining/mint.php":
                return new Mint();
            case "mfm-mining/miner.php":
                return new Miner();
            case "mfm-exchange/place.php":
                return new Place();
            case "mfm-exchange/cancel.php":
                return new Cancel();
            case "mfm-exchange/orderbook.php":
                return new OrderBook();
            case "mfm-exchange/orders.php":
                return new Orders();
            case "mfm-exchange/spred.php":
                return new Spred();
            case "mfm-wallet/home/api/main.php":
                return new Main();
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
            contract.run(scriptPath, parseParams(session));
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

    public static void broadcast(String channel, Object data) {
        if (wssServer.channels.containsKey(channel)) {
            Iterator<WebSocket> iterator = wssServer.channels.get(channel).iterator();
            Set<WebSocket> activeSubscribers = new HashSet<>();
            while (iterator.hasNext()) {
                WebSocket conn = iterator.next();
                if (conn.isOpen()) {
                    activeSubscribers.add(conn);
                } else {
                    iterator.remove();
                }
            }
            Message message = new Message();
            message.channel = channel;
            message.data = data;
            wssServer.broadcast(gson.toJson(message), activeSubscribers);
        }
        if (masterNode != null && channel.equals("transactions")) {
            Request.post("https://" + masterNode + (HTTPS_START_RANGE + getPortOffset(masterNode))
                    + "/mfm-token/send.php", data);
        }
    }
}
