package com.axiom.operatio.model.gameplay;

import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.machine.MachineType;

import java.util.Arrays;

/**
 * Описывает условия победы и проигрыша
 * TODO Сделать уровни и вывести ключевые показатели
 *
 * Правила:
 *  - Прохождение уровня - это производство нужного количества товаров в день (N x M)
 *  - Ты можешь покупать сначала только 0-7 материалы
 *  - Тебе разрешено покупать другие материалы только если ты их производил (Нужно ли это?)
 *  - Тебе разрешено покупать машины (после прохождения N уровней - освоение и обучение)
 *  (достижение мощности производства и/или требуемого уровня прибыли в месяц)
 *  - Ты проигрываешь когда у тебя заканчиваются деньги или истекает время (конец игры)
 */
public class Level {

    private String description;                         // Описание цели и задач уровня
    private long timeLimit;                             // Ограничение времени на достижение
    private boolean[] accessableMachine;                  // Доступные блоки
    private boolean[] accessableSKU;                    // Доступные в принципе товары на уровне
    private boolean[] buyAllowedSKU;                    // Доступные для покупки товары на уровне
    private int[] targetSKUBalance;                     // Целевые остатки на складе
    private double targetCapitalization;                // Целевая капитализация предприятия
    private double targetCashflow;                      // Целевой денежный поток

    public Level() {

        description = "";
        timeLimit = 0;
        targetCapitalization = 0;
        targetCashflow = 0;

        // По умолчанию доступны все машины
        accessableMachine = new boolean[MachineType.getMachineTypesCount()];
        Arrays.fill(accessableSKU, true);

        // По умолчанию 0 требований к остаткам на складе
        targetSKUBalance = new int[Inventory.SKU_COUNT];
        Arrays.fill(targetSKUBalance, 0);

        // По умолчанию доступны все материалы
        accessableSKU = new boolean[Inventory.SKU_COUNT];
        Arrays.fill(accessableSKU, true);

        // Для покупки доступны только сырье 0-7
        buyAllowedSKU = new boolean[Inventory.SKU_COUNT];
        Arrays.fill(buyAllowedSKU, 8, Inventory.SKU_COUNT-1, false);
        Arrays.fill(buyAllowedSKU,0,7, true);
    }


    public String getLevelDescription() {
        return description;
    }

    public boolean isMachineAvailable(int machineID) {
        return accessableMachine[machineID];
    }

    public boolean isAllowedToBuy(int commodity) {
        return buyAllowedSKU[commodity];
    }

    public boolean checkWin(Production production) {
        return false;
    }

    public boolean checkLose(Production production) {
        return false;
    }


}
