package com.axiom.operatio.model.production;

import com.axiom.operatio.model.market.Market;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.inventory.Inventory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

// TODO Добавить экономику: баланс, цена материала, цена хранения, цена операции (стоимость компании)
public class Production {

    protected static Production instance;           // Синглтон объекта - производство
    protected static Inventory inventory;           // Синглтон объекта - склад
    protected static Market market;                 // Синллтон объекта - рынок

    protected ArrayList<Block> blocks;              // Список блоков производства
    protected Block[][] grid;                       // Блоки привязанные к координатной сетке
    protected int columns, rows;                    // Количество столбцеов и строк

    protected long lastCycleTime;                   // Время последнего цикла (миллисекунды)
    protected static long cycleMilliseconds = 300;  // Длительносить цикла (миллисекунды)
    protected static long clock = 0;                // Часы производства (с вычетом пауз игры)
    protected long cycle;                           // Счётчик циклов производства

    protected boolean isPaused = false;             // Флаг паузы игры
    protected long pauseStart = 0;                  // Время начала паузы в системном времени
    protected long pausedTime = 0;                  // Сумма времени на паузы

    protected boolean blockSelected = false;        // Выбрал ли блок
    protected int selectedCol, selectedRow;         // Столбец и строка выбранного блока


    public static Production getInstance(int columns, int rows) {
        if (instance==null) instance = new Production(columns, rows);
        return instance;
    }


    private Production(int columns, int rows) {

        this.columns = columns;
        this.rows = rows;
        grid = new Block[rows][columns];
        blocks = new ArrayList<Block>(100);

        inventory = Inventory.getInstance();
        market = Market.getInstance();

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

        // Выполнить симуляцию склада
        inventory.process();
        // Выполнить симуляцию рынка
        market.process();
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

    public Inventory getInventory() {
        return inventory;
    }

    public Market getMarket() {
        return market;
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


    public JSONObject serialize() {

        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("columns", columns);
            jsonObject.put("rows", rows);
            jsonObject.put("lastCycleTime", lastCycleTime);
            jsonObject.put("cycleMilliseconds", cycleMilliseconds);
            jsonObject.put("clock", clock);
            jsonObject.put("cycle", cycle);
            jsonObject.put("isPaused", isPaused);
            jsonObject.put("pauseStart", pauseStart);
            jsonObject.put("pausedTime", pausedTime);
            jsonObject.put("blockSelected", blockSelected);
            jsonObject.put("selectedCol", selectedCol);
            jsonObject.put("selectedRow", selectedRow);
            /*
                protected static Market market;                 // Синллтон объекта - рынок
            */

            JSONArray jsonArray = new JSONArray();
            for (int i=0; i<blocks.size(); i++) {
                JSONObject jsonBlock = blocks.get(i).serialize();
                jsonArray.put(jsonBlock);
            }
            jsonObject.put("blocks", jsonArray);
            jsonObject.put("inventory", inventory.serialize());

            return jsonObject;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
