package com.mfm_wallet.mfm_exchange;

import com.mfm_wallet.Contract;
import com.mfm_wallet.model.Token;

import java.util.*;

import static com.mfm_wallet.mfm_data.DataContract.GAS_DOMAIN;

abstract class ExchangeUtils extends Contract {

    private static final Map<Long, Order> allOrders = new HashMap<>();
    private final Map<Long, Order> orders = new HashMap<>();

    public void place(String domain, String address, long isSell, double price, double amount, double total, String pass) {
        String exchangeAddress = "exchange_" + domain;
        if (botScriptReg(domain, exchangeAddress)) commitAccounts();

        if (price != round(price, 2)) error("price tick is 0.01");
        if (price <= 0) error("price less than 0");
        if (amount != round(amount, 2)) error("amount tick is 0.01");
        if (amount <= 0) error("amount less than 0");

        if (isSell == 1) {
            total = round(price * amount, 2);
            orderFillSell(address, domain, price, amount, total, pass);
        } else {
            amount = round(total / price, 2);
            orderFillBuy(address, domain, price, amount, total, pass);
        }
    }

    public long createOrder(String address, String domain, int isSell, double price, double amount, double total) {
        Order order = new Order();
        order.orderId = random.nextLong();
        order.address = address;
        order.domain = domain;
        order.isSell = isSell;
        order.price = price;
        order.amount = amount;
        order.total = total;
        order.status = 2;
        order.timestamp = time();
        orders.put(order.orderId, order);
        return order.orderId;
    }

    private List<Order> getOrders(String domain, int isSell, double price, boolean isBuy) {
        List<Order> result = new ArrayList<>();
        for (Order order : allOrders.values()) {
            if (order.domain.equals(domain) && order.isSell == isSell && order.status == 0) {
                if (isBuy ? order.price <= price : order.price >= price) {
                    result.add(order);
                }
            }
        }
        result.sort((o1, o2) -> isBuy ? Double.compare(o1.price, o2.price) : Double.compare(o2.price, o1.price));
        return result;
    }

    public void orderFillSell(String address, String domain, double price, double amount, double total, String pass) {
        String exchangeAddress = "exchange_" + domain;
        tokenSend(scriptPath, domain, address, exchangeAddress, amount, pass, null);
        long  orderId = createOrder(address, domain, 1, price, amount, total);
        double tradeVolume = 0;
        double lastTradePrice = 0;

        for (Order order : getOrders(domain, 0, price, false)) {
            Order newOrder = orders.get(orderId);
            double amountNotFilled = round(newOrder.amount - newOrder.amountFilled, 2);
            if (amountNotFilled == 0) break;
            double orderTotalNotFilled = round(order.total - order.totalFilled, 2);
            double orderAmountNotFilled = round(orderTotalNotFilled / order.price, 2);
            double amountToFill = Math.min(orderAmountNotFilled, amountNotFilled);
            double totalToFill = round(amountToFill * order.price, 2);

            updateOrder(order.orderId, order.amountFilled + amountToFill, order.totalFilled + totalToFill, orderAmountNotFilled == amountToFill ? 1 : 0);
            if (orderAmountNotFilled == amountToFill) {
                double amountFilled = allOrders.get(order.orderId).amountFilled;
                tokenSend(scriptPath, domain, exchangeAddress, order.address, amountFilled, tokenPass(domain, exchangeAddress), null);
            }

            updateOrder(orderId, newOrder.amountFilled + amountToFill, newOrder.totalFilled + totalToFill, 0);
            lastTradePrice = order.price;
            tradeVolume += totalToFill;
        }

        Order newOrder = orders.get(orderId);
        if (newOrder.amount == newOrder.amountFilled) {
            tokenSend(scriptPath, GAS_DOMAIN, exchangeAddress, address, round(newOrder.totalFilled, 2), tokenPass(GAS_DOMAIN, exchangeAddress), null);
            updateOrder(orderId, 1);
        } else {
            updateOrder(orderId, 0);
        }

        trackFill(domain, lastTradePrice, tradeVolume);
    }

    public void orderFillBuy(String address, String domain, double price, double amount, double total, String pass) {
        String exchangeAddress = "exchange_" + domain;
        tokenSend(scriptPath, GAS_DOMAIN, address, exchangeAddress, total, pass, null);
        long orderId = createOrder(address, domain, 0, price, amount, total);
        double tradeVolume = 0;
        double lastTradePrice = 0;

        for (Order order : getOrders(domain, 1, price, true)) {
            Order newOrder = orders.get(orderId);
            double totalNotFilled = round(newOrder.total - newOrder.totalFilled, 2);
            if (totalNotFilled == 0) break;
            double orderAmountNotFilled = round(order.amount - order.amountFilled, 2);
            double orderTotalNotFilled = round(orderAmountNotFilled * order.price, 2);
            double totalToFill = Math.min(orderTotalNotFilled, totalNotFilled);
            double amountToFill = round(totalToFill / order.price, 2);

            if (order.amountFilled + amountToFill < 0)
                error("amount filled less than 0 " + order.amountFilled + " " + amountToFill);
            updateOrder(order.orderId, order.amountFilled + amountToFill, order.totalFilled + totalToFill, orderTotalNotFilled == totalToFill ? 1 : 0);
            if (orderTotalNotFilled == totalToFill) {
                double totalFilled = allOrders.get(order.orderId).totalFilled;
                tokenSend(scriptPath, GAS_DOMAIN, exchangeAddress, order.address, totalFilled, tokenPass(GAS_DOMAIN, exchangeAddress), null);
            }

            updateOrder(orderId, newOrder.amountFilled + amountToFill, newOrder.totalFilled + totalToFill, 0);
            lastTradePrice = order.price;
            tradeVolume += totalToFill;
        }

        Order newOrder = orders.get(orderId);
        if (newOrder.total == newOrder.totalFilled) {
            tokenSend(scriptPath, domain, exchangeAddress, address, round(newOrder.amountFilled, 2), tokenPass(domain, exchangeAddress), null);
            updateOrder(orderId, 1);
        } else {
            updateOrder(orderId, 0);
        }

        trackFill(domain, lastTradePrice, tradeVolume);
    }

    public void trackFill(String domain, double price, double volume) {
        if (price != 0) {
            trackAccumulate(domain + "_volume", volume);
            double lastPrice = getCandleLastValue(domain + "_price");
            if (price != lastPrice) {
                trackLinear(domain + "_price", price);
                //broadcast("price", new BroadcastMessage(domain, price));
            }
            Token token = allTokens.get(domain);
            token.price24 = getCandleChange24(domain + "_price");
            token.volume24 = getCandleChange24(domain + "_volume");
            setToken(token);
        }
    }

    public void cancel(long orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            order = allOrders.get(orderId);
        }
        if (order.status != 0) error("order already finished");
        String exchangeAddress = "exchange_" + order.domain;
        if (order.isSell == 1) {
            double amountToGet = round(order.amount - order.amountFilled, 2);
            double totalToGet = round(order.totalFilled, 2);
            if (amountToGet > 0) tokenSend(scriptPath, order.domain, exchangeAddress, order.address, amountToGet, tokenPass(order.domain, exchangeAddress), null);
            if (totalToGet > 0) tokenSend(scriptPath, GAS_DOMAIN, exchangeAddress, order.address, totalToGet, tokenPass(GAS_DOMAIN, exchangeAddress), null);
        } else {
            double totalToGet = round(order.total - order.totalFilled, 2);
            double amountToGet = round(order.amountFilled, 2);
            if (totalToGet > 0) tokenSend(scriptPath, GAS_DOMAIN, exchangeAddress, order.address, totalToGet, tokenPass(GAS_DOMAIN, exchangeAddress), null);
            if (amountToGet > 0) tokenSend(scriptPath, order.domain, exchangeAddress, order.address, amountToGet, tokenPass(order.domain, exchangeAddress), null);
        }
        updateOrder(orderId, -1);
    }

    public void cancelAll(String domain, String address) {
        List<Order> orders = ordersActive(domain, address);
        for (Order order : orders) {
            cancel(order.orderId);
        }
    }

    public List<Order> ordersActive(String domain, String address) {
        List<Order> result = new ArrayList<>();
        for (Order order : allOrders.values()) {
            if (order.domain.equals(domain) && order.address.equals(address) && order.status == 0) {
                result.add(order);
            }
        }
        result.sort((o1, o2) -> Long.compare(o2.timestamp, o1.timestamp));
        return result;
    }

    public List<Order> ordersHistory(String domain, String address) {
        List<Order> result = new ArrayList<>();
        for (Order order : allOrders.values()) {
            if (order.domain.equals(domain) && order.address.equals(address) && order.status != 0) {
                result.add(order);
            }
        }
        result.sort((o1, o2) -> Long.compare(o2.timestamp, o1.timestamp));
        return result;
    }

    public List<PriceLevel> getPriceLevels(String domain, int isSell, int count) {
        List<PriceLevel> levels = new ArrayList<>();
        for (Order order : allOrders.values()) {
            if (order.domain.equals(domain) && order.isSell == isSell && order.status == 0) {
                levels.add(new PriceLevel(order.price, order.amount - order.amountFilled));
            }
        }
        levels.sort((o1, o2) -> isSell == 1 ? Double.compare(o1.price, o2.price) : Double.compare(o2.price, o1.price));
        return levels.size() > count ? levels.subList(0, count) : levels;
    }

    public boolean botScriptReg(String domain, String botAddress) {
        String placeScript = "mfm-exchange/place.php";
        tokenRegScript(domain, botAddress, placeScript);
        return tokenRegScript(GAS_DOMAIN, botAddress, placeScript);
    }

    public void commit() {
        super.commit();
        allOrders.putAll(orders);
        orders.clear();
    }

    private void updateOrder(long orderId, double amountFilled, double totalFilled, int status) {
        Order order = orders.get(orderId);
        if (order == null) {
            order = allOrders.get(orderId).clone();
        }
        if (order != null) {
            order.amountFilled = amountFilled;
            order.totalFilled = totalFilled;
            order.status = status;
            orders.put(orderId, order);
        }
    }

    private void updateOrder(long orderId, int status) {
        Order order = orders.get(orderId);
        if (order == null) {
            order = allOrders.get(orderId);
        }
        if (order != null) {
            order.status = status;
            orders.put(orderId, order);
        }
    }

    public class Order {
        public long orderId;
        public String address;
        public String domain;
        public int isSell;
        public double price;
        public double amount;
        public double total;
        public int status;
        public long timestamp;
        public double amountFilled;
        public double totalFilled;

        public Order() {
        }

        public Order clone() {
            Order order = new Order();
            order.orderId = orderId;
            order.address = address;
            order.domain = domain;
            order.isSell = isSell;
            order.price = price;
            order.amount = amount;
            order.total = total;
            order.status = status;
            order.timestamp = timestamp;
            order.amountFilled = amountFilled;
            order.totalFilled = totalFilled;
            return order;
        }
    }

    public class PriceLevel {
        public double price;
        public double amount;
        public double percent;

        public PriceLevel(double price, double amount) {
            this.price = price;
            this.amount = amount;
        }
    }
}