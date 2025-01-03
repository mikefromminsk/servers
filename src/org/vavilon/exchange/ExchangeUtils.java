package org.vavilon.exchange;

import org.vavilon.token.TokenRequests;
import org.vavilon.token.model.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.vavilon.exchange.ExchangeServer.broadcast;
import static org.vavilon.utils.Params.map;
import static org.vavilon.data.Contract.GAS_DOMAIN;

public abstract class ExchangeUtils extends TokenRequests {

    public static final String EXCHANGE_PREFIX = "exchange_";

    private static final Map<Long, Order> allOrders = new HashMap<>();
    private final Map<Long, Order> orders = new HashMap<>();

    void placeAndCommit(String domain,
                        String address,
                        boolean isSell,
                        double price,
                        double amount,
                        double total,
                        String pass) {
        try {
            Place place = new Place();
            place.params = map(
                    "domain", domain,
                    "address", address,
                    "is_sell", "" + (isSell ? 1 : 0),
                    "price", "" + price,
                    "amount", "" + amount,
                    "total", "" + total,
                    "pass", pass);
            place.run();
            place.commit();
        } catch (Exception e) {
        }
    }

    public void place(String domain, String address, long isSell, double price, double amount, double total, String pass) {
        String exchangeAddress = EXCHANGE_PREFIX + domain;
        if (botScriptReg(domain, exchangeAddress)) commitAccounts();

        if (price != round(price)) error("price tick is 0.01");
        if (price <= 0) error("price less than 0");
        if (amount != round(amount)) error("amount tick is 0.01");
        if (amount <= 0) error("amount less than 0");

        if (isSell == 1) {
            total = round(price * amount);
            orderFillSell(address, domain, price, amount, total, pass);
        } else {
            amount = round(total / price);
            orderFillBuy(address, domain, price, amount, total, pass);
        }
    }

    public long createOrder(String address, String domain, int isSell, double price, double amount, double total) {
        Order order = new Order();
        order.order_id = random();
        order.address = address;
        order.domain = domain;
        order.is_sell = isSell;
        order.price = price;
        order.amount = amount;
        order.total = total;
        order.status = 2;
        order.timestamp = time();
        orders.put(order.order_id, order);
        return order.order_id;
    }

    private List<Order> getOrders(String domain, int isSell, double price, boolean isBuy) {
        List<Order> result = new ArrayList<>();
        for (Order order : allOrders.values()) {
            if (order.domain.equals(domain) && order.is_sell == isSell && order.status == 0) {
                if (isBuy ? order.price <= price : order.price >= price) {
                    result.add(order);
                }
            }
        }
        result.sort((o1, o2) -> isBuy ? Double.compare(o1.price, o2.price) : Double.compare(o2.price, o1.price));
        return result;
    }

    public void orderFillSell(String address, String domain, double price, double amount, double total, String pass) {
        String exchangeAddress = EXCHANGE_PREFIX + domain;
        tokenSend(scriptPath, domain, address, exchangeAddress, amount, pass, null);
        long orderId = createOrder(address, domain, 1, price, amount, total);
        double tradeVolume = 0;
        double lastTradePrice = 0;

        for (Order order : getOrders(domain, 0, price, false)) {
            Order newOrder = orders.get(orderId);
            double amountNotFilled = round(newOrder.amount - newOrder.amount_filled);
            if (amountNotFilled == 0) break;
            double orderTotalNotFilled = round(order.total - order.total_filled);
            double orderAmountNotFilled = round(orderTotalNotFilled / order.price);
            double amountToFill = Math.min(orderAmountNotFilled, amountNotFilled);
            double totalToFill = round(amountToFill * order.price);

            updateOrder(order.order_id, order.amount_filled + amountToFill, order.total_filled + totalToFill, orderAmountNotFilled == amountToFill ? 1 : 0);
            if (orderAmountNotFilled == amountToFill) {
                double amountFilled = getOrder(order.order_id).amount_filled;
                tokenSend(scriptPath, domain, exchangeAddress, order.address, amountFilled, tokenPass(domain, exchangeAddress), null);
            }

            updateOrder(orderId, newOrder.amount_filled + amountToFill, newOrder.total_filled + totalToFill, 0);
            lastTradePrice = order.price;
            tradeVolume += totalToFill;
        }

        Order newOrder = orders.get(orderId);
        if (newOrder.amount == newOrder.amount_filled) {
            tokenSend(scriptPath, GAS_DOMAIN, exchangeAddress, address, round(newOrder.total_filled), tokenPass(GAS_DOMAIN, exchangeAddress), null);
            updateOrder(orderId, 1);
        } else {
            updateOrder(orderId, 0);
        }

        trackFill(domain, lastTradePrice, tradeVolume);
    }

    public void orderFillBuy(String address, String domain, double price, double amount, double total, String pass) {
        String exchangeAddress = EXCHANGE_PREFIX + domain;
        tokenSend(scriptPath, GAS_DOMAIN, address, exchangeAddress, total, pass, null);
        long orderId = createOrder(address, domain, 0, price, amount, total);
        double tradeVolume = 0;
        double lastTradePrice = 0;

        for (Order order : getOrders(domain, 1, price, true)) {
            Order newOrder = orders.get(orderId);
            double totalNotFilled = round(newOrder.total - newOrder.total_filled);
            if (totalNotFilled == 0) break;
            double orderAmountNotFilled = round(order.amount - order.amount_filled);
            double orderTotalNotFilled = round(orderAmountNotFilled * order.price);
            double totalToFill = Math.min(orderTotalNotFilled, totalNotFilled);
            double amountToFill = round(totalToFill / order.price);

            if (order.amount_filled + amountToFill < 0)
                error("amount filled less than 0 " + order.amount_filled + " " + amountToFill);
            updateOrder(order.order_id, order.amount_filled + amountToFill, order.total_filled + totalToFill, orderTotalNotFilled == totalToFill ? 1 : 0);
            if (orderTotalNotFilled == totalToFill) {
                double totalFilled = getOrder(order.order_id).total_filled;
                tokenSend(scriptPath, GAS_DOMAIN, exchangeAddress, order.address, totalFilled, tokenPass(GAS_DOMAIN, exchangeAddress), null);
            }

            updateOrder(orderId, newOrder.amount_filled + amountToFill, newOrder.total_filled + totalToFill, 0);
            lastTradePrice = order.price;
            tradeVolume += totalToFill;
        }

        Order newOrder = orders.get(orderId);
        if (newOrder.total == newOrder.total_filled) {
            tokenSend(scriptPath, domain, exchangeAddress, address, round(newOrder.amount_filled), tokenPass(domain, exchangeAddress), null);
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
                broadcast("price", map(
                        "domain", domain,
                        "price", "" + price
                ));
            }
            Token token = getToken(domain);
            token.price = getCandleLastValue(domain + "_price");
            token.price24 = getCandleChange24(domain + "_price");
            token.volume24 = getCandleChange24(domain + "_volume");
            setToken(token);
        }
    }

    public void cancel(long orderId) {
        scriptPath = null; // temporary fix
        Order order = getOrder(orderId);
        if (order.status != 0) error("order already finished");
        String exchangeAddress = EXCHANGE_PREFIX + order.domain;
        if (order.is_sell == 1) {
            double amountToGet = round(order.amount - order.amount_filled);
            double totalToGet = round(order.total_filled);
            if (amountToGet > 0)
                tokenSend(scriptPath, order.domain, exchangeAddress, order.address, amountToGet, tokenPass(order.domain, exchangeAddress), null);
            if (totalToGet > 0)
                tokenSend(scriptPath, GAS_DOMAIN, exchangeAddress, order.address, totalToGet, tokenPass(GAS_DOMAIN, exchangeAddress), null);
        } else {
            double totalToGet = round(order.total - order.total_filled);
            double amountToGet = round(order.amount_filled);
            if (totalToGet > 0)
                tokenSend(scriptPath, GAS_DOMAIN, exchangeAddress, order.address, totalToGet, tokenPass(GAS_DOMAIN, exchangeAddress), null);
            if (amountToGet > 0)
                tokenSend(scriptPath, order.domain, exchangeAddress, order.address, amountToGet, tokenPass(order.domain, exchangeAddress), null);
        }
        updateOrder(orderId, -1);
    }

    public void cancelAll(String domain, String address) {
        List<Order> orders = ordersActive(domain, address);
        for (Order order : orders) {
            try {
                cancel(order.order_id);
            } catch (Exception e) {
            }
        }
    }

    public void cancelAllAndCommit(String domain, String address) {
        try {
            CancelAll cancelAll = new CancelAll();
            cancelAll.run(null, map(
                    "domain", domain,
                    "address", address
            ));
            cancelAll.commit();
        } catch (Exception e) {
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

    public List<PriceLevel> getPriceLevels(String domain, boolean isSell, int count) {
        Map<Double, PriceLevel> levels = new HashMap<>();
        for (Order order : allOrders.values()) {
            if (order.domain.equals(domain) && order.is_sell == (isSell ? 1 : 0) && order.status == 0) {
                if (levels.containsKey(order.price)) {
                    PriceLevel level = levels.get(order.price);
                    level.amount += order.amount - order.amount_filled;
                } else {
                    levels.put(order.price, new PriceLevel(order.price, order.amount - order.amount_filled));
                }
            }
        }
        List<PriceLevel> levelsList = new ArrayList<>(levels.values());
        levelsList.sort((o1, o2) -> isSell ? Double.compare(o1.price, o2.price) : Double.compare(o2.price, o1.price));
        return levelsList.size() > count ? levelsList.subList(0, count) : levelsList;
    }

    public void commit() {
        super.commit();
        if (orders.size() > 0) {
            allOrders.putAll(orders);
            orders.clear();
        }
    }

    private Order getOrder(long orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            order = allOrders.get(orderId);
        }
        return order;
    }

    private void updateOrder(long orderId, double amountFilled, double totalFilled, int status) {
        Order order = getOrder(orderId);
        if (order != null) {
            order.amount_filled = amountFilled;
            order.total_filled = totalFilled;
            order.status = status;
            orders.put(orderId, order);
        }
    }

    private void updateOrder(long orderId, int status) {
        Order order = getOrder(orderId);
        if (order != null) {
            order.status = status;
            orders.put(orderId, order);
        }
    }

    public class Order {
        public long order_id;
        public String address;
        public String domain;
        public int is_sell;
        public double price;
        public double amount;
        public double total;
        public int status;
        public long timestamp;
        public double amount_filled;
        public double total_filled;

        public Order() {
        }

        public Order clone() {
            Order order = new Order();
            order.order_id = order_id;
            order.address = address;
            order.domain = domain;
            order.is_sell = is_sell;
            order.price = price;
            order.amount = amount;
            order.total = total;
            order.status = status;
            order.timestamp = timestamp;
            order.amount_filled = amount_filled;
            order.total_filled = total_filled;
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