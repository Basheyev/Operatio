package com.axiom.operatio.model.market;

public class MarketModel {

    private static MarketModel market;

    private MarketModel() {

    }


    public static MarketModel getInstance() {
        if (market==null) market = new MarketModel();
        return market;
    }

    public float demandFunction(float time) {

        // Имитируем колебания производительности труда в диапазоне от 0.15-0.35
        float productivity = 0.25f + (float) Math.sin(0.5f + time / 20.0f) * 0.1f;

        // Имитируем длинный долговой цикл 0.5-1.0 (растягиваем во времени в 4 раза)
        float longCycleDebt = 0.75f + (float) Math.sin(time / 4.0f) * 0.25f;

        // Имитируем короткий долговой цикл 0.5-1.0
        float shortCycleDebt = 0.75f + (float) Math.cos(time) * 0.25f;

        // Добавляем случайные колебания 0.0-0.1
        float fluctuations = (float) Math.random() * 0.1f;

        // Имитируем рыночный спрос нормированный на единицу (0.0-1.0)
        float value = (productivity + longCycleDebt + shortCycleDebt + fluctuations) / 2.45f;

        if (value > 1.0f) {
            value *=10;
        }

        return value;
    }


}
