package com.axiom.operatio.model.market;

import com.axiom.operatio.model.ledger.Ledger;

public class CommodityMarket {

    public static final int HISTORY_LENGTH = Ledger.HISTORY_LENGTH * 3;

    public static final double LONG_CYCLE_PART  = 0.05;    // +-5% длинный экономический цикл
    public static final double SHORT_CYCLE_PART = 0.03;    // +-2% короткий экономический цикл
    public static final double NOISE_PART       = 0.10;    // +-10% рыночный шум
    public static final double SHORT_STEP       = 0.02;    // шаг на рынке по короткому циклу
    public static final double LONG_STEP        = 0.0005;  // шаг на рынке по длинному циклу

    private final double faceValue;
    private final double averageDemand;
    private final double averageSupply;
    private final double marketBias;
    private double marketCycle;
    private double largeCycle;

    private double demand;
    private double supply;
    private double marketValue;

    private final double[] history;
    private double historyMaxValue;
    private int historyLength;


    public CommodityMarket(double faceValue, double averageDemand, double averageSupply) {
        this.faceValue = faceValue;
        this.averageDemand = averageDemand;
        this.averageSupply = averageSupply;
        this.demand = averageDemand;
        this.supply = averageSupply;
        this.largeCycle = Math.random() * 2 * Math.PI;
        this.marketBias = (System.nanoTime() / 1_000_000.0) % Math.PI;
        this.history = new double[HISTORY_LENGTH];
        this.historyLength = 0;
        process();
    }


    public synchronized void process() {
        evaluateNextValue();
        saveHistory();
    }


    private void evaluateNextValue() {
        // Посчитать спрос
        double nextDemand = (averageDemand + demand) / 2.0;
        nextDemand += (nextDemand * LONG_CYCLE_PART) * Math.cos(largeCycle + marketBias);
        nextDemand += (nextDemand * SHORT_CYCLE_PART) * Math.sin(marketCycle);
        nextDemand += (nextDemand * NOISE_PART) * (Math.random() - 0.5);
        demand = nextDemand;
        // Посчитать предложение
        double nextSupply = (averageSupply + supply) / 2.0;
        nextSupply += (nextSupply * LONG_CYCLE_PART) * Math.cos(largeCycle + marketBias);
        nextSupply += (nextSupply * SHORT_CYCLE_PART) * Math.sin(marketCycle);
        nextSupply += (nextSupply * NOISE_PART) * (Math.random() - 0.5);
        supply = nextSupply;
        // Посчитать цену
        marketValue = faceValue * (demand / supply);
        // Сделать шаг по рыночному циклу
        marketCycle += SHORT_STEP * Math.random();
        largeCycle += LONG_STEP * Math.random();

    }


    private void saveHistory() {
        history[historyLength] = marketValue;
        historyLength++;
        if (historyLength >= HISTORY_LENGTH) {
            System.arraycopy(history, 1, history, 0,historyLength - 1);
            historyLength--;
        }
        historyMaxValue = 0;
        for (int i=0; i<historyLength; i++) {
            if (history[i] > historyMaxValue) historyMaxValue = history[i];
        }
    }

    public double getFaceValue() {
        return faceValue;
    }

    public double getMarketValue() {
        return marketValue;
    }

    public double getDemand() {
        return demand;
    }

    public double getSupply() {
        return supply;
    }

    public double getHistoryMaxValue() {
        return historyMaxValue;
    }

    public int getHistoryLength() {
        return historyLength;
    }

    public synchronized void getHistoryValues(double[] destination) {
        System.arraycopy(history, 0, destination, 0, historyLength);
    }
}
