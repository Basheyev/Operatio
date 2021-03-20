package com.axiom.operatio.model.ledger;

public class MaterialRecord {

    protected int productivity = 0;                   // Производительность за период
    protected int manufacturedByPeriod = 0;           // Объем произведенных материалов за период
    protected int manufacturedTotal = 0;              // Объем произведенных материалов всего
    protected double soldCommoditiesSum = 0;          // Объем проданных материалов
    protected int soldCommoditiesAmount = 0;          // Количество проданных материалов
    protected int soldCommoditiesByPeriod = 0;        // Сумма проданных материалов
    protected int soldCommoditiesCounter = 0;         // Счётчик проданных материалов
    protected double boughtCommoditiesSum = 0;        // Объем приобретенных материалов
    protected int boughtCommoditiesAmount = 0;        // Количество приобретенных материалов
    protected int boughtCommoditiesByPeriod = 0;      // Закуплено материалов за период
    protected int boughtCommoditiesCounter = 0;       // Счётчик закупленных материалов

}
