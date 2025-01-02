package org.vavilon.wallet;

import org.vavilon.exchange.CancelAll;
import org.vavilon.servers.HttpServer;
import org.vavilon.servers.model.Endpoint;
import org.vavilon.servers.model.Subscription;
import org.vavilon.utils.Request;
import org.vavilon.utils.SSLContextBuilder;
import org.vavilon.wallet.analytics.Candles;
import org.vavilon.wallet.analytics.Events;
import org.vavilon.wallet.analytics.Funnel;
import org.vavilon.wallet.analytics.Track;
import org.vavilon.wallet.data.mining.Info;
import org.vavilon.wallet.data.mining.Miner;
import org.vavilon.wallet.data.mining.Mint;
import org.vavilon.wallet.data.wallet.Main;
import org.vavilon.wallet.token.*;
import org.vavilon.wallet.token.tests.SumTokens;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.vavilon.utils.Params.map;
import static org.vavilon.wallet.Utils.*;
import static org.vavilon.wallet.WssServer.WSS_START_RANGE;
import static org.vavilon.wallet.token.TokenUtils.transHistory;

public class Node extends HttpServer implements WssServer.Callback {
    static final int HTTPS_START_RANGE = 8000;

    static String masterNode;
    static WssServer wssServer;
    static WssClient wssClient;

    public Node(String domain, String masterNode) {
        super(domain, HTTPS_START_RANGE + getPortOffset(domain));
        Node.masterNode = masterNode;
        SSLContext context = SSLContextBuilder.from(domain);
        if (context != null) {
            makeSecure(context.getServerSocketFactory(), null);
        }
        if (masterNode == null) {
            try {
                if (transHistory.size() == 0) {
                    Init init = new Init();
                    init.run(null, map("admin_password", "pass"));
                    init.commit();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
                System.exit(0);
            }
        } else {
            masterPortOffset = getPortOffset(masterNode);
            wssClient = new WssClient(URI.create(
                    (Node.masterNode.equals("localhost") ? "ws" : "wss") +
                            "://" + Node.masterNode + ":" + (WSS_START_RANGE + masterPortOffset)));
            wssClient.connect();
            if (transHistory.size() == 0) {
                syncWithMasterNode();
            }
        }
        wssServer = new WssServer(domain, this);
    }


    class TransactionHistory {
        List<Map<String, String>> trans;
        Long trans_count;
    }

    long page = 0;
    long size = 1000;
    long trans_count = 100000000000L;
    int masterPortOffset = 0;

    void syncWithMasterNode() {
        if (page * size < trans_count) {
            Request.post(masterNode + ":" + (HTTPS_START_RANGE + masterPortOffset) + "/mfm-token/trans_history", map(
                    "page", "" + page,
                    "size", "1000"
            ), response -> {
                TransactionHistory responseObj = gson.fromJson(response, TransactionHistory.class);
                for (Map<String, String> tran : responseObj.trans) {
                    try {
                        Send send = new Send();
                        send.run(null, tran);
                        send.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                page++;
                trans_count = responseObj.trans_count;
                syncWithMasterNode();
            }, error -> {
                System.out.println("Master node is down");
            });
        }
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
            case "mfm-token/send":
                return new Send();
            case "mfm-token/account":
                return new Account();
            case "mfm-token/accounts":
                return new Accounts();
            case "mfm-token/search":
                return new Search();
            case "mfm-token/tran":
                return new Tran();
            case "mfm-token/trans":
                return new Trans();
            case "mfm-token/profile":
                return new Token();
            case "mfm-token/token_info":
                return new TokenAnalytics();
            case "mfm-token/trans_history":
                return new TransHistory();
            case "mfm-analytics/track":
                return new Track();
            case "mfm-analytics/events":
                return new Events();
            case "mfm-analytics/candles":
                return new Candles();
            case "mfm-analytics/funnel":
                return new Funnel();
            case "mfm-mining/mint":
                return new Mint();
            case "mfm-mining/miner":
                return new Miner();
            case "mfm-mining/info":
                return new Info();
            case "mfm-wallet/home/api/main":
                return new Main();
            case "mfm-exchange/sum_tokens":
                return new SumTokens();
            case "mfm-exchange/cancel_all":
                return new CancelAll();
        }
        return null;
    }

    public static void broadcast(String channel, Map<String, String> data) {
        if (wssServer != null) {
            wssServer.broadcast(channel, data);
            if (masterNode != null && channel.equals("transactions")) {
                Request.post(masterNode + ":" + (HTTPS_START_RANGE + getPortOffset(masterNode)) + "/mfm-token/send",
                        data,
                        response -> {
                            System.out.println("Master node is successfully updated");
                        }, error -> {
                            System.out.println(masterNode + ":" + (HTTPS_START_RANGE + getPortOffset(masterNode)) + "/mfm-token/send");
                            System.out.println("Master node is down " + error);
                        });
            }
        }
    }
}
