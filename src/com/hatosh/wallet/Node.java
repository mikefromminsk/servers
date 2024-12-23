package com.hatosh.wallet;

import com.hatosh.servers.HttpServer;
import com.hatosh.servers.WssServer;
import com.hatosh.servers.model.Endpoint;
import com.hatosh.servers.model.Message;
import com.hatosh.servers.model.Subscription;
import com.hatosh.utils.Request;
import com.hatosh.utils.SSLContextBuilder;
import com.hatosh.wallet.analytics.Candles;
import com.hatosh.wallet.analytics.Events;
import com.hatosh.wallet.analytics.Funnel;
import com.hatosh.wallet.analytics.Track;
import com.hatosh.wallet.data.mining.Info;
import com.hatosh.wallet.data.mining.Miner;
import com.hatosh.wallet.data.mining.Mint;
import com.hatosh.wallet.data.wallet.Main;
import com.hatosh.wallet.token.*;
import com.hatosh.wallet.token.model.Transaction;
import org.java_websocket.WebSocket;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.*;

import static com.hatosh.utils.Params.map;
import static com.hatosh.wallet.Utils.*;

public class Node extends HttpServer implements WssServer.Callback {
    static final int HTTPS_START_RANGE = 8000;

    static String masterNode;
    static WssServer wssServer;

    class TransactionHistory {
        List<Transaction> trans;
    }

    public Node(String domain, String masterNode) {
        super(domain, HTTPS_START_RANGE + getPortOffset(domain));
        Node.masterNode = masterNode;
        SSLContext context = SSLContextBuilder.from(domain);
        if (context != null) {
            makeSecure(context.getServerSocketFactory(), null);
        }
        if (masterNode == null) {
            try {
                if (TokenUtils.transHistory.fileData.sumFilesSize == 0) {
                    Init init = new Init();
                    init.run(null, map("admin_password", "pass"));
                    init.commit();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
                System.exit(0);
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
        wssServer = new WssServer(domain, this);
    }

    @Override
    public void onMessage(Subscription sub) {
    }

    @Override
    public void start() throws IOException {
        super.start();
        wssServer.start();
    }

    @Override
    public Endpoint getEndpoint(String uri) {
        switch (uri) {
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
            case "mfm-mining/info.php":
                return new Info();
            case "mfm-wallet/home/api/main.php":
                return new Main();
        }
        return null;
    }

    public static void broadcast(String channel, Map<String, String> data) {
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
            data.put("from_address", data.get("from"));
            data.put("to_address", data.get("to"));
            data.put("pass", (data.get("key") == null ? "" : data.get("key")) + ":" + data.get("next_hash"));
            Request.post(masterNode + ":" + (HTTPS_START_RANGE + getPortOffset(masterNode))
                    + "/mfm-token/send.php", data, response -> {
                System.out.println("Master node is successfully updated");
            }, error -> {
                System.out.println("Master node is down");
            });
        }
    }
}
