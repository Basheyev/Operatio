package com.axiom.operatio.model.gameplay;

import com.axiom.operatio.model.production.Production;

import java.util.ArrayList;

/**
 * Описывает разрешения и условия победы
 * Only reason you out of business when you out of money
 */
public class Level {

    protected String description;                        // Описание цели и задач уровня
    protected double reward;                             // Вознаграждение за прохождение
    protected ArrayList<Integer> allowedBlocks;          // Разрешенные на уровне машины
    protected ArrayList<Integer> allowedMaterials;       // Разрешенные на уровне материалы
    protected ArrayList<LevelCondition> winConditions;   // Условия победы

    public Level() {
        allowedBlocks = new ArrayList<>();
        allowedMaterials = new ArrayList<>();
        winConditions = new ArrayList<>();
    }

    //-------------------------------------------------------------------------------------------
    // Level description
    //-------------------------------------------------------------------------------------------

    public String getDescription() {
        return description;
    }

    public double getReward() {
        return reward;
    }

    public void addCondition(int KPI, int objectID, double value) {
        winConditions.add(new LevelCondition(KPI, objectID, value));
    }

    //-------------------------------------------------------------------------------------------
    // Check completion
    //-------------------------------------------------------------------------------------------

    public boolean checkWinConditions(Production production) {
        // Если условий победы нет - выигрыша нет
        if (winConditions.size()==0) return false;
        // Если не выполнилось хоть одно условия - выигрыша нет
        for (int i=0; i<winConditions.size(); i++) {
            if (!winConditions.get(i).check(production.getLedger())) return false;
        }
        // Иначе выигрыш
        return true;
    }

    //-------------------------------------------------------------------------------------------
    // Level permissions
    //-------------------------------------------------------------------------------------------

    public boolean isBlockAvailable(int blockID) {
        return allowedBlocks.contains(blockID);
    }

    public boolean isMaterialAvailable(int commodity) {
        return allowedMaterials.contains(commodity);
    }

}
