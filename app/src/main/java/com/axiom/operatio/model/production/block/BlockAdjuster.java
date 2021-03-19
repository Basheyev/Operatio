package com.axiom.operatio.model.production.block;

import static com.axiom.operatio.model.production.block.Block.NONE;
import static com.axiom.operatio.model.production.block.Block.LEFT;
import static com.axiom.operatio.model.production.block.Block.UP;
import static com.axiom.operatio.model.production.block.Block.RIGHT;
import static com.axiom.operatio.model.production.block.Block.DOWN;


/**
 * Наладчик направления потока материалов блока
 */
public class BlockAdjuster {

    /**
     * Устанавливает направления потока материалов с учётом соседних блоков
     */
    public static void adjustFlow(Block block) {
        boolean upper = block.hasInOutFrom(UP);
        boolean down  = block.hasInOutFrom(DOWN);
        boolean left  = block.hasInOutFrom(LEFT);
        boolean right = block.hasInOutFrom(RIGHT);
        int neighborsCount = (upper ? 1:0) + (down ? 1:0) + (left ? 1:0) + (right ? 1:0);
        if (neighborsCount == 1) {
            int neighborSide = (upper ? UP : down ? DOWN : left ? LEFT : RIGHT);
            adjustOneNeighbor(block, neighborSide);
        }
        else if (neighborsCount == 2) {
            if (left && right) adjustTwoNeighbors(block, LEFT, RIGHT);
            if (left && upper) adjustTwoNeighbors(block, LEFT, UP);
            if (left && down) adjustTwoNeighbors(block, LEFT, DOWN);
            if (right && upper) adjustTwoNeighbors(block, RIGHT, UP);
            if (right && down) adjustTwoNeighbors(block, RIGHT, DOWN);
            if (upper && down) adjustTwoNeighbors(block, UP, DOWN);
        }

    }


    /**
     * Устанавливает направление потока материалов если есть один сосед
     * @param side с какой стороны находится соседний блок
     */
    private static void adjustOneNeighbor(Block block, int side) {
        if (block.isSourceAvailable(side)) {
            block.setDirections(side, oppositeDirection(side));
        } else if (block.isDestinationAvailable(side)) {
            block.setDirections(oppositeDirection(side), side);
        }
    }


    /**
     * Устанавливает направление потока материалов если есть два соседа
     * @param side1 сторона первого соседа
     * @param side2 сторона второго соседа
     */
    private static void adjustTwoNeighbors(Block block, int side1, int side2) {
        if (block.hasInOutFrom(side1) && !block.hasInOutFrom(side2)) {
            adjustOneNeighbor(block, side1);
        } else if (!block.hasInOutFrom(side1)) {
            adjustOneNeighbor(block, side2);
        } else if (block.isDestinationAvailable(side1) && block.isSourceAvailable(side2)) {
            block.setDirections(side2, side1);
        } else if (block.isSourceAvailable(side1) && block.isDestinationAvailable(side2)) {
            block.setDirections(side1, side2);
        }
    }


    //--------------------------------------------------------------------------------------
    // Поворот направления потока материалов
    //--------------------------------------------------------------------------------------

    /**
     * Поворачивает направление потока материалов
     */
    public static void rotateFlow(Block block) {
        boolean upper = block.hasInOutFrom(UP);
        boolean down  = block.hasInOutFrom(DOWN);
        boolean left  = block.hasInOutFrom(LEFT);
        boolean right = block.hasInOutFrom(RIGHT);
        int neighborsCount = (upper ? 1:0) + (down ? 1:0) + (left ? 1:0) + (right ? 1:0);

        switch (neighborsCount) {
            case 0:
                rotateClockwise90(block);
                break;
            case 1:
                int neighborSide = (upper ? UP : down ? DOWN : left ? LEFT : RIGHT);
                rotateOneNeighbor(block, neighborSide);
                break;
            case 2:
                if (left && right) rotateTwoNeighbors(block, LEFT, RIGHT);
                if (left && upper) rotateTwoNeighbors(block, LEFT, UP);
                if (left && down) rotateTwoNeighbors(block, LEFT, DOWN);
                if (right && upper) rotateTwoNeighbors(block, RIGHT, UP);
                if (right && down) rotateTwoNeighbors(block, RIGHT, DOWN);
                if (upper && down) rotateTwoNeighbors(block, UP, DOWN);
                break;
            default:
                rotateClockwise(block);
        }
    }


    /**
     * Поворачивает направление потока материалов при одном соседе
     */
    private static void rotateOneNeighbor(Block block, int side) {
        boolean isSource = block.isSourceAvailable(side);
        boolean isDestination = block.isDestinationAvailable(side);
        if (!block.directionFlip) {
            block.flipDirection();
            block.directionFlip = true;
            return;
        }
        if (isSource) {
            block.flipDirection();
            int newOutDir = nextClockwiseDirection(block.outputDirection);
            if (newOutDir == block.inputDirection) newOutDir = nextClockwiseDirection(newOutDir);
            block.setDirections(side, newOutDir);
            block.directionFlip = false;
        } else if (isDestination) {
            block.flipDirection();
            int newInpDir = nextClockwiseDirection(block.inputDirection);
            if (newInpDir == block.outputDirection) newInpDir = nextClockwiseDirection(newInpDir);
            block.setDirections(newInpDir, side);
            block.directionFlip = false;
        } else {
            rotateClockwise(block);
        }
    }



    /**
     * Поворачивает направление потока материалов при двух соседях
     */
    private static void rotateTwoNeighbors(Block block, int side1, int side2) {
        boolean side1Available = block.hasInOutFrom(side1);
        boolean side2Available = block.hasInOutFrom(side2);

        if (!side1Available && !side2Available) rotateClockwise(block);
        else if (side1Available && !side2Available) rotateOneNeighbor(block,side1);
        else if (!side1Available) rotateOneNeighbor(block,side2);
        else {
            if (block.isSourceAvailable(side1)==block.isSourceAvailable(side2)) {
                rotateOneNeighbor(block, side1);
            }
            else if (block.isSourceAvailable(side1)) rotateOneNeighbor(block, side1);
            else if (block.isSourceAvailable(side2)) rotateOneNeighbor(block, side2);
        }
    }



    //----------------------------------------------------------------------------------------------
    // Вспомогательные методы
    //----------------------------------------------------------------------------------------------


    /**
     * Поворачивает направление потока материалов по часовой стрелке с отражением через шаг
     */
    private static void rotateClockwise(Block block) {
        if (block.directionFlip) {
            int currentInpDir = block.getInputDirection();
            int currentOutDir = block.getOutputDirection();
            int newInpDir = nextClockwiseDirection(currentInpDir);
            int newOutDir = currentOutDir;
            if (newInpDir == currentOutDir) {
                newInpDir = currentInpDir;
                newOutDir = nextClockwiseDirection(currentOutDir);
            }
            block.setDirections(newInpDir, newOutDir);
            block.directionFlip = false;
        } else {
            block.flipDirection();
            block.directionFlip = true;
        }
    }


    /**
     * Поворачивает направление потока материалов на 90 градусов
     */
    private static void rotateClockwise90(Block block) {
        int currentInpDir = block.getInputDirection();
        int currentOutDir = block.getOutputDirection();
        int newInpDir = nextClockwiseDirection(currentInpDir);
        int newOutDir = nextClockwiseDirection(currentOutDir);
        block.setDirections(newInpDir, newOutDir);
    }


    /**
     * Следующее направление по часовой стрелке
     * @param direction текущее направление
     * @return следующее направление по часовой стрелке
     */
    public static int nextClockwiseDirection(int direction) {
        if (direction== NONE || direction < 0) return NONE;
        direction++;
        if (direction > DOWN) direction = LEFT;
        return direction;
    }


    /**
     * Противоположное направление
     * @param direction направление
     * @return противоположное направление
     */
    public static int oppositeDirection(int direction) {
        if (direction==LEFT) return RIGHT;
        if (direction==RIGHT) return LEFT;
        if (direction==UP) return DOWN;
        if (direction==DOWN) return UP;
        return NONE;
    }



}
