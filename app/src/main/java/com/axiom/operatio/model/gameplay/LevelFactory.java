package com.axiom.operatio.model.gameplay;

import java.util.ArrayList;

/**
 * Построитель уровней
 * todo вынести данные уровней в файл ресурса
 */
public class LevelFactory {

    private static LevelFactory instance = null;
    private ArrayList<Level> levels;

    public static LevelFactory getInstance() {
        if (instance==null) instance = new LevelFactory();
        return instance;
    }

    private LevelFactory() {
        levels = new ArrayList<>();
        buildLevel1();
        buildLevel2();
        buildLevel3();
        buildLevel4();
        buildLevel5();
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
        level.description = "Manufacture 10 steel plates";
        level.reward = 1000;

        level.allowedBlocks.add(0);       // Conveyor
        level.allowedBlocks.add(2);       // Press

        level.allowedBlocks.add(8);       // import buffer
        level.allowedBlocks.add(9);       // export buffer

        level.allowedMaterials.add(0);      // Steel
        level.allowedMaterials.add(8);      // Steel plate

        level.addCondition(LevelCondition.MANUFACTURED_AMOUNT, 8, 10);

        levels.add(level);
    }

    private void buildLevel2() {
        Level level = new Level();
        level.description = "Manufacture 20 steel plates a day";
        level.reward = 2000;

        level.allowedBlocks.add(0);       // Conveyor
        level.allowedBlocks.add(1);       // Buffer
        level.allowedBlocks.add(2);       // Press

        level.allowedBlocks.add(8);       // import buffer
        level.allowedBlocks.add(9);       // export buffer

        level.allowedMaterials.add(0);      // Steel
        level.allowedMaterials.add(8);      // Steel plate

        level.addCondition(LevelCondition.MANUFACTURE_PRODUCTIVITY, 8, 20);

        levels.add(level);
    }

    private void buildLevel3() {
        Level level = new Level();
        level.description = "Manufacture 60 copper plates";
        level.reward = 6000;

        level.allowedBlocks.add(0);       // Conveyor
        level.allowedBlocks.add(1);       // Buffer
        level.allowedBlocks.add(2);       // Press

        level.allowedBlocks.add(8);       // import buffer
        level.allowedBlocks.add(9);       // export buffer

        level.allowedMaterials.add(0);      // Steel
        level.allowedMaterials.add(2);      // Copper
        level.allowedMaterials.add(8);      // Steel plate
        level.allowedMaterials.add(10);     // Copper plate

        level.addCondition(LevelCondition.MANUFACTURED_AMOUNT, 10, 60);

        levels.add(level);
    }

    private void buildLevel4() {
        Level level = new Level();
        level.description = "Reach $400 revenue per day";
        level.reward = 5000;

        level.allowedBlocks.add(0);       // Conveyor
        level.allowedBlocks.add(1);       // Buffer
        level.allowedBlocks.add(2);       // Press

        level.allowedBlocks.add(8);       // import buffer
        level.allowedBlocks.add(9);       // export buffer

        level.allowedMaterials.add(0);      // Steel
        level.allowedMaterials.add(2);      // Copper
        level.allowedMaterials.add(8);      // Steel plate
        level.allowedMaterials.add(10);     // Copper plate

        level.addCondition(LevelCondition.REVENUE_PER_DAY, 0, 400);

        levels.add(level);
    }



    private void buildLevel5() {
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

        for (int i=0;i<64; i++) {
            level.allowedMaterials.add(i);
        }

        levels.add(level);
    }

}
