package com.axiom.operatio.model.inventory;

import com.axiom.atom.engine.data.structures.Channel;
import com.axiom.operatio.model.materials.Item;

/**
 * Складская карточка
 */
public class StockKeepingUnit {

    public static final int MAX_SKU_CAPACITY = 999;
    public static final int DEFAULT_QUANTITY = 20;

    public static final int AUTO_NONE = 0;          //  0000
    public static final int AUTO_BUY = 1;           //  0001
    public static final int AUTO_SELL = 2;          //  0010
    public static final int AMOUNT_BIT_SHIFT = 16;  //  Смещение битов где хранится объем контракта

    private int materialID;                   // Код материала
    private Channel<Item> items;              // Хранимые предметы
    private int contractParameters;           // Тип контракта и его параметры

    private int dailyBalance = 0;             // Ежедневный баланс материала
    private int dailyOutOfStock = 0;          // Ежедневный дефицит материала
    private int balanceCounter = 0;           // Счетчик баланса материала
    private int outOfStockCounter = 0;        // Счетчик дефицита материала

    public StockKeepingUnit(int materialID) {
        this.materialID = materialID;
        items = new Channel<Item>(MAX_SKU_CAPACITY);
        contractParameters = AUTO_NONE;
    }

    public void setContractParameters(int contractParameters) {
        this.contractParameters = contractParameters;
    }

    public int getContractParameters() {
        return contractParameters;
    }


    public int setContractQuantity(int quantity) {
        int current = contractParameters & 0x0F;                 // Очистим параметры от количества
        int amount = (quantity & 0xFFFF) << AMOUNT_BIT_SHIFT;    // Запишем количество в верхние биты
        contractParameters = amount | current;                   // Записываем итоговое значение
        return getContractQuantity();
    }


    public int getContractQuantity() {
        return (contractParameters >> AMOUNT_BIT_SHIFT) & 0xFFFF;
    }


    public void signPurchaseContract(boolean signed) {
        if (signed) {
            contractParameters |= AUTO_BUY;
        } else {
            contractParameters &= ~AUTO_BUY;
        }
    }

    public boolean hasPurchaseContract() {
        return (contractParameters & AUTO_BUY) > 0;
    }

    public void signSalesContract(boolean signed) {
        if (signed) {
            contractParameters |= AUTO_SELL;
        } else {
            contractParameters &= ~AUTO_SELL;
        }
    }

    public boolean hasSalesContract() {
        return (contractParameters & AUTO_SELL) > 0;
    }


    public boolean push(Item item) {
        balanceCounter++;
        return items.push(item);
    }

    public Item peek() {
        Item item = items.peek();
        if (item==null) outOfStockCounter++;
        return item;
    }

    public Item poll() {
        balanceCounter--;
        return items.poll();
    }

    public int getBalance() {
        return items.size();
    }


    public int getDailyBalance() {
        return dailyBalance;
    }

    public int getDailyOutOfStock() {
        return dailyOutOfStock;
    }

    public void closeDailyStatistics() {
        dailyBalance = balanceCounter;
        dailyOutOfStock = outOfStockCounter;
        balanceCounter = 0;
        outOfStockCounter = 0;
    }


}