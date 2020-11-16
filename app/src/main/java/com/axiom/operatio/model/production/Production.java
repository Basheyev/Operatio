package com.axiom.operatio.model.production;

import com.axiom.atom.engine.data.JSONSerializable;
import com.axiom.operatio.model.gameplay.Ledger;
import com.axiom.operatio.model.market.Market;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.production.conveyor.Conveyor;
import com.axiom.operatio.model.production.machine.Machine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

// TODO Площадь производства должна быть ограничена
public class Production implements JSONSerializable {


    //protected static Production instance;         // Синглтон объекта - производство
    protected Inventory inventory;                  // Объект - склад
    protected Market market;                        // Объект - рынок
    protected Ledger ledger;                        // Объект - игровая статистика
    protected double cashBalance = 15000;           // Остатки денег

    protected ArrayList<Block> blocks;              // Список блоков производства
    protected Block[][] grid;                       // Блоки привязанные к координатной сетке
    protected int columns, rows;                    // Количество столбцеов и строк

    protected long lastCycleTime;                   // Время последнего цикла (миллисекунды)
    protected long cycleMilliseconds = 300;         // Длительносить цикла (миллисекунды)
    protected long clock = 0;                       // Часы производства (с вычетом пауз игры)
    protected long cycle;                           // Счётчик циклов производства

    protected boolean isPaused = false;             // Флаг паузы игры
    protected long pauseStart = 0;                  // Время начала паузы в системном времени
    protected long pausedTime = 0;                  // Сумма времени на паузы

    protected boolean blockSelected = false;        // Выбрал ли блок
    protected int selectedCol, selectedRow;         // Столбец и строка выбранного блока


    public Production(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        grid = new Block[rows][columns];
        blocks = new ArrayList<Block>(100);
        inventory = new Inventory(this);
        market = new Market(this);
        ledger = new Ledger();
    }


    public Production(JSONObject jsonObject) throws JSONException {

        cashBalance = jsonObject.getLong("cashBalance");
        int columns = jsonObject.getInt("columns");
        int rows = jsonObject.getInt("rows");

        this.columns = columns;
        this.rows = rows;
        grid = new Block[rows][columns];

        lastCycleTime = jsonObject.getLong("lastCycleTime");
        cycleMilliseconds = jsonObject.getLong("cycleMilliseconds");
        clock = jsonObject.getLong("clock");
        cycle = jsonObject.getLong("cycle");
        isPaused = jsonObject.getBoolean("isPaused");
        pauseStart = jsonObject.getLong("pauseStart");
        pausedTime = jsonObject.getLong("pausedTime");
        blockSelected = jsonObject.getBoolean("blockSelected");
        selectedCol = jsonObject.getInt("selectedCol");
        selectedRow = jsonObject.getInt("selectedRow");

        JSONArray jsonArray = jsonObject.getJSONArray("blocks");
        blocks = new ArrayList<Block>(jsonArray.length());
        for (int i=0; i<jsonArray.length(); i++) {
            JSONObject jsonBlock = jsonArray.getJSONObject(i);
            Block block = Block.deserialize(this, jsonBlock);
            setBlock(block, block.column, block.row);
        }

        JSONObject jsonInventory = jsonObject.getJSONObject("inventory");
        inventory = new Inventory(this, jsonInventory);
        market = new Market(this);
        ledger = new Ledger();
    }




    /**
     * Симулирует цикл производства
     */
    public void process() {
        if (!isPaused) {
            long now = clock; //System.currentTimeMillis();
            if (now - lastCycleTime > cycleMilliseconds) {
                Block block;
                boolean energyPayed;
                int size = blocks.size();
                int expenseType = Ledger.EXPENSE_BLOCK_OPERATION;
                for (int i = 0; i < size; i++) {
                    block = blocks.get(i);
                    energyPayed = false;
                    if (block instanceof Machine) {
                        energyPayed = decreaseCashBalance(expenseType,0.1d);  // Берем по $0.1 за цикл машины
                    } else if (block instanceof Conveyor) {
                        energyPayed = decreaseCashBalance(expenseType,0.05d);  // Берем по $0.05 за конвейер
                    } else {
                        energyPayed = decreaseCashBalance(expenseType,0.01d); // Берем по $0.01 за буферы
                    }
                    if (energyPayed) {
                        block.process();  // Если энергия оплачена отрабатываем
                    } else {
                        // TODO иначе показываем что не хватает на операцию (энергии)
                    }
                }
                // Выполнить симуляцию склада
                inventory.process(this);
                // Выполнить симуляцию рынка
                market.process();
                // Выполнить процесс учёта статистики
                ledger.process(this);
                // Увеличиваем счётчик циклов
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
    public long getClockMilliseconds() {
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


    public double getValuation() {
        double sum = 0;
        for (int i=0; i<blocks.size(); i++) {
            sum += blocks.get(i).getPrice();
        }
        return sum;
    }


    public void removeBlock(Block block, boolean returnToInventory) {
        if (block==null) return;
        int col = block.column;
        int row = block.row;
        if (col < 0 || row < 0 || col >= columns || row >= columns) return;
        grid[row][col] = null;
        blocks.remove(block);
        if (returnToInventory) block.returnItemsTo(getInventory());
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

    public Ledger getLedger() { return ledger; }

    public long getCycleTimeMs() {
        return cycleMilliseconds;
    }

    public long getCurrentCycle() {
        return cycle;
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

    public Block getSelectedBlock() {
        if (blockSelected) {
            return getBlockAt(selectedCol, selectedRow);
        } else {
            return null;
        }
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
            jsonObject.put("cashBalance", cashBalance);
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

    public double getCashBalance() {
        return cashBalance;
    }

    public boolean decreaseCashBalance(int type, double value) {
        double result = cashBalance - value;
        if (result < 0) return false;
        ledger.registerExpense(type, value);
        cashBalance = result;
        return true;
    }

    public double increaseCashBalance(int type, double value) {
        cashBalance += value;
        ledger.registerIncome(type, value);
        return cashBalance;
    }

}
