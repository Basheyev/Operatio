package com.axiom.operatio.model.production;

import com.axiom.operatio.model.production.block.Block;

import java.util.ArrayList;

// TODO 1. Добавить экономику: баланс, цена материала, цена хранения, цена операции (стоимость компании)
// TODO 2. Добавить зона погрузки и выгрузки со склада
// TODO 3. Добавить машину контроля качества (сортировки)
// TODO 4. Добавить сохранение уровня (сериализацию JSon)
public class Production {

    protected static Production instance;           // Синглтон объекта производства

    protected ArrayList<Block> blocks;              // Список блоков производства
    protected Block[][] grid;                       // Блоки привязанные к координатной сетке
    protected int columns, rows;                    // Количество столбцеов и строк

    private long lastCycleTime;                     // Время последнего цикла (миллисекунды)
    private static long cycleMilliseconds = 300;    // Длительносить цикла (миллисекунды)
    protected static long clock = 0;                // Часы производства (с вычетом пауз игры)
    protected long cycle;                           // Счётчик циклов производства

    private boolean isPaused = false;               // Флаг паузы игры
    private long pauseStart = 0;                    // Время начала паузы в системном времени
    private long pausedTime = 0;                    // Сумма времени на паузы

    private boolean blockSelected = false;          // Выбрал ли блок
    private int selectedCol, selectedRow;           // Столбец и строка выбранного блока


    public static Production getInstance(int columns, int rows) {
        if (instance==null) instance = new Production(columns, rows);
        return instance;
    }


    private Production(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        grid = new Block[rows][columns];

        blocks = new ArrayList<Block>(100);
    }


    /**
     * Симулирует цикл производства
     */
    public void process() {
        if (!isPaused) {
            long now = System.currentTimeMillis();
            if (now - lastCycleTime > cycleMilliseconds) {
                Block block;
                int size = blocks.size();
                for (int i = 0; i < size; i++) {
                    block = blocks.get(i);
                    block.process();
                }
                cycle++;
                lastCycleTime = now;
            }
            // Учитываем время производства в миллисекундах с учетом пауз игры
            clock = System.currentTimeMillis() - pausedTime;
        }
    }


    /**
     * Возвращает время производства в миллисекундах с учетом пауз игры
     * @return время в миллисекундах
     */
    public static long getClockMilliseconds() {
        return clock;
    }


    public boolean setBlock(Block block, int col, int row) {
        if (block == null) return false;
        if (col < 0 || col >= columns) return false;
        if (row < 0 || row >= rows) return false;
        block.column = col;
        block.row = row;
        blocks.add(block);
        grid[row][col] = block;
        return true;
    }


    public Block getBlockAt(int col, int row) {
        if (col < 0 || col >= columns) return null;
        if (row < 0 || row >= rows) return null;
        return grid[row][col];
    }


    public Block getBlockAt(Block relativeTo, int direction) {
        switch (direction) {
            case Block.LEFT:
                return getBlockAt(relativeTo.column - 1, relativeTo.row);
            case Block.RIGHT:
                return getBlockAt(relativeTo.column + 1, relativeTo.row);
            case Block.UP:
                return getBlockAt(relativeTo.column, relativeTo.row + 1);
            case Block.DOWN:
                return getBlockAt(relativeTo.column, relativeTo.row - 1);
            default:
                return null;
        }
    }


    public void removeBlock(Block block) {
        if (block==null) return;
        int col = block.column;
        int row = block.row;
        if (col < 0 || row < 0 || col >= columns || row >= columns) return;
        grid[row][col] = null;
        blocks.remove(block);
    }


    public void clearBlocks() {
        for (int row=0; row < rows;row++) {
            for (int col=0; col < columns; col++) {
                grid[row][col] = null;
            }
        }
        blocks.clear();
    }


    public int getTotalItems() {
        Block block;
        int size = blocks.size();
        int total = 0;
        for (int i=0; i<size; i++) {
            block = blocks.get(i);
            total += block.getItemsAmount();
        }
        return total;
    }


    public static long getCycleTimeMs() {
        return cycleMilliseconds;
    }

    public static long getCurrentCycle() {
        if (instance==null) return 0;
        return instance.cycle;
    }


    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }


    public boolean isBlockSelected() {
        return blockSelected;
    }

    public void selectBlock(int col, int row) {
        if (col < 0 || row < 0 || col >= columns || row >= columns) return;
        selectedCol = col;
        selectedRow = row;
        blockSelected = true;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public void unselectBlock() {
        blockSelected = false;
    }

    public void setPaused(boolean pause) {
        if (pause) {
            if (isPaused) {
                long now = System.currentTimeMillis();
                pausedTime += (now - pauseStart);
            }
            pauseStart = System.currentTimeMillis();
            isPaused = true;
        } else {
            long now = System.currentTimeMillis();
            pausedTime += (now - pauseStart);
            isPaused = false;
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

}
