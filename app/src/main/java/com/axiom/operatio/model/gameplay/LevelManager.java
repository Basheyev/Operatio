package com.axiom.operatio.model.gameplay;

import java.util.ArrayList;

// TODO Сделать уровни в игре с поэтапным открытием возможностей в класс Production

public class LevelManager {

    private static LevelManager instance = null;
    private ArrayList<Level> levels;


    public static LevelManager getInstance() {
        if (instance==null) instance = new LevelManager();
        return instance;
    }

    private LevelManager() {
        levels = new ArrayList<>();
        buildLevel1();
        buildLevel2();
    }


    public Level getLevel(int index) {
        return levels.get(index);
    }

    public int size() {
        return levels.size();
    }


    // Level Factory

    private void buildLevel1() {
        Level level = new Level();
        level.description = "Manufacture 100 steel plates";
        level.reward = 3000;
        level.allowedMachines.add(0);       // Press
        level.allowedMaterials.add(0);      // Steel
        level.allowedMaterials.add(8);      // Steel plate
        level.addCondition(LevelCondition.MANUFACTURED_AMOUNT, 8, 100);
        levels.add(level);
    }

    private void buildLevel2() {
        Level level = new Level();
        level.description = "Manufacture 150 copper plates and sell it";
        level.reward = 5000;
        level.allowedMachines.add(0);       // Press
        level.allowedMaterials.add(0);      // Steel
        level.allowedMaterials.add(2);      // Copper
        level.allowedMaterials.add(8);      // Steel plate
        level.allowedMaterials.add(10);     // Copper plate
        level.addCondition(LevelCondition.MANUFACTURED_AMOUNT, 10, 150);
        level.addCondition(LevelCondition.SOLD_AMOUNT, 10, 150);
        levels.add(level);
    }

}
