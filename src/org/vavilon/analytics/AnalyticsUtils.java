package org.vavilon.analytics;

import com.metabrain.gdb.BigArray;
import com.metabrain.gdb.BigMap;
import org.vavilon.analytics.model.EventLink;
import org.vavilon.servers.model.Endpoint;
import org.vavilon.analytics.model.Candle;
import org.vavilon.analytics.model.Event;
import org.vavilon.wallet.data.model.Field;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AnalyticsUtils extends Endpoint {
    static final Map<String, List<Candle>> allCharts = new ConcurrentHashMap<>();
    static final BigArray<Event> allEvents = new BigArray<>("allEvents", Event.class);

    public Map<String, Double> newCandles = new ConcurrentHashMap<>();
    public List<Event> newEvents = new ArrayList<>();

    public void trackEvent(String name, String value, String userId, String session) {
        Event event = new Event();
        event.time = time();
        event.name = name;
        event.value = value;
        event.user_id = userId;
        event.session = session;
        newEvents.add(event);
    }

    public void commitEvents() {
        for (Event event : newEvents) {
            allEvents.add(event);
        }
        newEvents.clear();
    }

    public Event getEvent(String name, String value, String session) {
        List<Event> events = getEvents(name, value, session, null, 1L);
        return events.isEmpty() ? null : events.get(0);
    }

    public List<Event> getEvents(String name, String value, String session, Long fromTime, Long size) {
        long i = allEvents.size() - 1;
        List<Event> result = new ArrayList<>();
        while (i >= 0 && result.size() < size && (fromTime == null || fromTime > 0)) {
            Event event = allEvents.get(i);
            if (event == null) break;
            if (name != null && !name.equals(event.name)) {
                i--;
                continue;
            }
            if (value != null && !value.equals(event.value)) {
                i--;
                continue;
            }
            if (session != null && !session.equals(event.session)) {
                i--;
                continue;
            }
            if (fromTime != null && event.time < fromTime) {
                break;
            }
            result.add(event);
            i--;
        }
        return result;
    }


    static final Map<String, Long> periopdsSec = new HashMap<>();

    {
        periopdsSec.put("M", 60L);
        periopdsSec.put("H", 60L * 60);
        periopdsSec.put("D", 60L * 60 * 24);
        periopdsSec.put("W", 60L * 60 * 24 * 7);
    }

    public void trackLinear(String key, double value) {
        newCandles.put(key, value);
    }

    public void trackAccumulate(String key) {
        trackLinear(key, 1);
    }

    public void trackAccumulate(String key, double value) {
        trackLinear(key, getCandleLastValue(key) + value);
    }

    void commitCharts() {
        for (String key : newCandles.keySet()) {
            long timestamp = time();
            Double value = newCandles.get(key);
            for (String period_name : periopdsSec.keySet()) {
                long period = periopdsSec.get(period_name);
                Candle last_candle = null;
                List<Candle> chart = allCharts.get(key + period_name);
                if (chart != null) {
                    last_candle = chart.get(chart.size() - 1);
                }
                long period_time = (timestamp / period) * period;
                if (last_candle == null || period_time != last_candle.time) {
                    Candle candle = new Candle();
                    candle.period = period_name;
                    candle.time = period_time;
                    candle.low = value;
                    candle.high = value;
                    candle.open = getCandleLastValue(key);
                    candle.close = value;
                    allCharts.computeIfAbsent(key + period_name, k -> new ArrayList<>()).add(candle);
                } else {
                    last_candle.low = Math.min(last_candle.low, value);
                    last_candle.high = Math.max(last_candle.high, value);
                    last_candle.close = value;
                }
            }
        }
    }

    public List<Candle> getCandles(String key, String periodName, int count) {
        List<Candle> chart = allCharts.get(key + periodName);
        long period = periopdsSec.get(periodName);
        return optimizeCandles(chart, period, count);
    }

    public List<Candle> optimizeCandles(List<Candle> chart, long periodSec, int count) {
        if (chart == null || chart.isEmpty()) return Collections.emptyList();
        Candle firstCandle = chart.get(0);
        Candle lastCandle = chart.get(chart.size() - 1);
        Map<Long, Candle> candlesMap = new HashMap<>();
        for (Candle candle : chart)
            candlesMap.put(candle.time, candle);
        List<Candle> result = new ArrayList<>();
        double lastClose = lastCandle.close;
        long timestamp = time();
        long periodTime = (timestamp / periodSec) * periodSec;
        for (long i = periodTime; i >= firstCandle.time && result.size() < count; i -= periodSec) {
            Candle item = candlesMap.get(i);
            if (item == null) {
                Candle newCandle = new Candle();
                newCandle.time = (int) i;
                newCandle.low = lastClose;
                newCandle.high = lastClose;
                newCandle.open = lastClose;
                newCandle.close = lastClose;
                result.add(newCandle);
            } else {
                lastClose = item.open;
                result.add(item);
            }
        }
        Collections.reverse(result);
        return result;
    }

    public List<Candle> getAccumulate(String key, String periodName, int count) {
        List<Candle> candles = getCandles(key, periodName, count);
        for (Candle candle : candles) {
            candle.open = round(candle.open);
            candle.close = round(candle.close);
            candle.high = round(candle.high);
            candle.low = round(candle.low);
            candle.value = round(candle.close - candle.open);
        }
        return candles;
    }

    public double getCandleLastValue(String key) {
        List<Candle> chart = allCharts.get(key + "M");
        if (chart == null || chart.isEmpty()) return 0;
        return chart.get(chart.size() - 1).close;
    }

    public double getCandleChange24(String key) {
        List<Candle> chart = getCandles(key, "D", 2);
        if (chart == null || chart.size() < 2) return 0;
        return chart.get(0).close - chart.get(1).close;
    }

    public void commit() {
        commitEvents();
        commitCharts();
    }
}