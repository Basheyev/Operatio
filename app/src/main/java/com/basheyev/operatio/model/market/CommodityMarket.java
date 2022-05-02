package com.basheyev.operatio.model.market;

import com.basheyev.operatio.model.ledger.Ledger;

/**
 * Симулятор рынка товара - выдает значение спроса, предложения и цены
 */
public class CommodityMarket {

    public static final int HISTORY_LENGTH = Ledger.HISTORY_LENGTH * 3;

    public static final double LONG_CYCLE_PART  = 0.05;    // +-5% влияние длинный экономический цикл
    public static final double SHORT_CYCLE_PART = 0.03;    // +-3% влияние короткий экономический цикл
    public static final double NOISE_PART       = 0.10;    // +-10% влияние рыночного шума
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


    /**
     * Конструктор симулятора рынка товара
     * @param faceValue номинальная цена товара
     * @param averageDemand средний спрос в количестве штук
     * @param averageSupply среднее предложение в количчестве штук
     */
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


    /**
     * Выполняет один шаг симуляции
     */
    public synchronized void process() {
        evaluateNextValue();
        saveHistory();
    }


    /**
     * Считает следующие значение спроса, предложения и рыночной цены
     */
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


    /**
     * Сохраняет в историю значение цены товара (для графиков)
     */
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


    /**
     * Получить номинальную стоимость товара
     * @return номинальная стоимость товара
     */
    public double getFaceValue() {
        return faceValue;
    }


    /**
     * Получить рыночную стоимость товара
     * @return рыночная стоимость товара
     */
    public double getMarketValue() {
        return marketValue;
    }


    /**
     * Объем спроса товара на рынке
     * @return спрос в количестве штук
     */
    public double getDemand() {
        return demand;
    }


    /**
     * Объем предложения товара на рынке
     * @return предложение в количестве штук
     */
    public double getSupply() {
        return supply;
    }


    /**
     * Максимальная рыночная цена в истории
     * @return максимальная рыночная цена в истории
     */
    public double getHistoryMaxValue() {
        return historyMaxValue;
    }

    /**
     * Длина истории
     * @return количество значений сохраненных в истории
     */
    public int getHistoryLength() {
        return historyLength;
    }

    /**
     * Загрузить в массив данные истории рыночной цены
     * @param destination массив со значениями истории рыночной цены
     */
    public synchronized void getHistoryValues(double[] destination) {
        System.arraycopy(history, 0, destination, 0, historyLength);
    }
}
