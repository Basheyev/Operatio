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
        buildLevel3();
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

        level.allowedBlocks.add(0);       // Conveyor
        level.allowedBlocks.add(2);       // Press
        level.allowedBlocks.add(8);       // import
        level.allowedBlocks.add(9);       // export

        level.allowedMaterials.add(0);      // Steel
        level.allowedMaterials.add(8);      // Steel plate

        // fixme Считает количество проданного а не произведенного (а может нет, надо проверить)
        level.addCondition(LevelCondition.MANUFACTURED_AMOUNT, 8, 100);

        levels.add(level);
    }

    private void buildLevel2() {
        Level level = new Level();
        level.description = "Manufacture 150 copper plates and sell it";
        level.reward = 5000;

        level.allowedBlocks.add(0);       // Conveyor
        level.allowedBlocks.add(1);       //
        level.allowedBlocks.add(2);       // Press
        level.allowedBlocks.add(8);       // import
        level.allowedBlocks.add(9);       // export

        level.allowedMaterials.add(0);      // Steel
        level.allowedMaterials.add(2);      // Copper
        level.allowedMaterials.add(8);      // Steel plate
        level.allowedMaterials.add(10);     // Copper plate
        level.addCondition(LevelCondition.MANUFACTURED_AMOUNT, 10, 150);
        level.addCondition(LevelCondition.SOLD_AMOUNT, 10, 150);
        levels.add(level);
    }


    private void buildLevel3() {
        Level level = new Level();
        level.description = "Free play";
        level.reward = 10000;

        level.allowedBlocks.add(0);       // Conveyor
        level.allowedBlocks.add(1);       //
        level.allowedBlocks.add(2);       // Press
        level.allowedBlocks.add(3);       //
        level.allowedBlocks.add(4);       //
        level.allowedBlocks.add(5);       //
        level.allowedBlocks.add(6);       //
        level.allowedBlocks.add(7);       //
        level.allowedBlocks.add(8);       // import
        level.allowedBlocks.add(9);       // export

        level.allowedMaterials.add(0);      // Steel
        level.allowedMaterials.add(2);      // Copper
        level.allowedMaterials.add(8);      // Steel plate
        level.allowedMaterials.add(10);     // Copper plate

//        level.addCondition(LevelCondition.MANUFACTURED_AMOUNT, 10, 150);
//        level.addCondition(LevelCondition.SOLD_AMOUNT, 10, 150);

        levels.add(level);
    }

}
