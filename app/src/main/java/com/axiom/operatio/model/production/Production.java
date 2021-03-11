package com.axiom.operatio.model.production;

import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.data.JSONSerializable;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.gameplay.Ledger;
import com.axiom.operatio.model.gameplay.Level;
import com.axiom.operatio.model.gameplay.LevelFactory;
import com.axiom.operatio.model.market.Market;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.production.conveyor.Conveyor;
import com.axiom.operatio.model.production.machine.Machine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Модель производства
 */
public class Production implements JSONSerializable {

    public static final int START_MONEY = 10000;  // Начальная сумма денег
    public static final int TILE_PRICE = 500;     // Цена одной плитки площади
    public static final int CYCLE_TIME = 300;     // Длительность цикла в миллесекундах

    private Inventory inventory;                  // Объект - склад
    private Market market;                        // Объект - рынок
    private Ledger ledger;                        // Объект - игровая статистика
    private LevelFactory levelFactory;            // Менеджер уровней
    private int level = 0;                        // Текущий уровень
    private int lastCompletedLevel = -1;          // Последний завершенный уровень
    private double cashBalance = START_MONEY;     // Стартовые деньги

    private ArrayList<Block> blocks;              // Список блоков производства
    private Block[][] grid;                       // Блоки привязанные к координатной сетке
    private boolean[][] unlocked;                 // Разлокированные клетки производства
    private int columns, rows;                    // Количество столбцов и строк

    private long lastCycleTime;                   // Время последнего цикла (миллисекунды)
    private long cycleMilliseconds = CYCLE_TIME;  // Длительносить цикла (миллисекунды)
    private long clock = 0;                       // Часы производства (с вычетом пауз игры)
    private long cycle;                           // Счётчик циклов производства

    private boolean isPaused = false;             // Флаг паузы игры
    private long pauseStart = 0;                  // Время начала паузы в системном времени
    private long pausedTime = 0;                  // Сумма времени на паузы

    private boolean blockSelected = false;        // Выбрал ли блок
    private int selectedCol, selectedRow;         // Столбец и строка выбранного блока

    private int levelCompletedSound;


    public Production(int columns, int rows) {

        levelCompletedSound = SoundRenderer.loadSound(R.raw.yes_snd);

        levelFactory = LevelFactory.getInstance();
        this.columns = columns;
        this.rows = rows;
        grid = new Block[rows][columns];
        unlocked = new boolean[rows][columns];
        setAreaUnlocked(0,0, columns, rows, false);
        setAreaUnlocked(0,0,8, 6, true);

        blocks = new ArrayList<Block>(100);
        inventory = new Inventory(this);
        market = new Market(this);
        ledger = new Ledger(this);

    }


    public Production(JSONObject jsonObject) throws JSONException {

        levelCompletedSound = SoundRenderer.loadSound(R.raw.yes_snd);

        levelFactory = LevelFactory.getInstance();
        cashBalance = jsonObject.getLong("cashBalance");
        int columns = jsonObject.getInt("columns");
        int rows = jsonObject.getInt("rows");

        this.columns = columns;
        this.rows = rows;
        grid = new Block[rows][columns];
        unlocked = new boolean[rows][columns];
        level = jsonObject.getInt("level");
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
        try {
            float cameraX = (float) jsonObject.getDouble("cameraX");
            float cameraY = (float) jsonObject.getDouble("cameraY");
            Camera.getInstance().lookAt(cameraX, cameraY);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = jsonObject.getJSONArray("blocks");
        blocks = new ArrayList<Block>(jsonArray.length());
        for (int i=0; i<jsonArray.length(); i++) {
            JSONObject jsonBlock = jsonArray.getJSONObject(i);
            Block block = Block.deserialize(this, jsonBlock);
            setBlock(block, block.column, block.row);
        }

        try {
            JSONArray jsonUnlocked = jsonObject.getJSONArray("unlocked");
            for (int i=rows-1; i>=0; i--) {
                JSONArray jsonUnlockedRow = jsonUnlocked.getJSONArray(i);
                for (int j=columns-1; j>=0; j--) {
                    unlocked[i][j] = jsonUnlockedRow.getBoolean(j);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            setAreaUnlocked(0,0, columns, rows, true);
        }

        JSONObject jsonInventory = jsonObject.getJSONObject("inventory");
        inventory = new Inventory(this, jsonInventory);
        market = new Market(this);

        JSONObject jsonLedger;
        try {
            jsonLedger = jsonObject.getJSONObject("ledger");
            ledger = new Ledger(this, jsonLedger);
        } catch (JSONException e) {
            e.printStackTrace();
            ledger = new Ledger(this);
        }

    }




    /**
     * Симулирует цикл производства
     */
    public void process() {
        if (!isPaused) {
            long now = clock;
            if (now - lastCycleTime > cycleMilliseconds) {
                Block block;
                boolean energyPayed;
                int size = blocks.size();
                int expenseType = Ledger.EXPENSE_BLOCK_OPERATION;
                for (int i = 0; i < size; i++) {
                    block = blocks.get(i);
                    energyPayed = false;
                    // TODO Разные операционные косты надо брать у блока, а не у тут
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
                inventory.process();
                // Выполнить симуляцию рынка
                market.process();
                // Выполнить процесс учёта статистики
                ledger.process();
                // Проверить условия завершения уровня
                checkLevelConditions();

                // Увеличиваем счётчик циклов
                cycle++;
                lastCycleTime = now;
            }
            // Учитываем время производства в миллисекундах с учетом пауз игры
            clock = System.currentTimeMillis() - pausedTime;
        }


    }

    public void setAreaUnlocked(int col, int row, int w, int h, boolean state) {
        if (col < 0) col = 0;
        if (row < 0) row = 0;
        if (col >= columns) col = columns - 1;
        if (row >= rows) row = rows - 1;
        if (col + w >= columns) w = (columns - col) - 1;
        if (row + h >= rows) h = (rows - row) - 1;
        for (int y=row; y<row+h; y++) {
            for (int x=col; x<col+w; x++) {
                unlocked[y][x] = state;
            }
        }
    }

    private void checkLevelConditions() {
        // Проверка условий уровня
        Level theLevel = levelFactory.getLevel(level);
        if (theLevel.checkWinConditions(this) && lastCompletedLevel != level) {
            // todo выдать сообщение о прохождении уровня
            Log.i("PRODUCTION", "LEVEL " + level + " COMPLETED!!!!");
            // Выдать звук о прохождении уровня
            SoundRenderer.playSound(levelCompletedSound);
            // Забрать награду
            cashBalance += theLevel.getReward();
            // Перейти на следующий уровень если он есть
            lastCompletedLevel = level;
            if (level + 1 <= levelFactory.size() - 1) level++;
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

    public boolean isUnlocked(int col, int row) {
        if (col < 0 || col >= columns) return false;
        if (row < 0 || row >= rows) return false;
        return unlocked[row][col];
    }


    public double getAssetsValuation() {
        double sum = 0;
        Block block;
        for (int i=0; i<blocks.size(); i++) {
            block = blocks.get(i);
            sum += block.getPrice();
        }
        return sum;
    }

    public double getWorkInProgressValuation() {
        double sum = 0;
        Block block;
        for (int i=0; i<blocks.size(); i++) {
            block = blocks.get(i);
            sum += block.getItemsPrice();
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

    public int getLevel() {
        return level;
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
            jsonObject.put("level", level);
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
            jsonObject.put("cameraX", Camera.getInstance().getX());
            jsonObject.put("cameraY", Camera.getInstance().getY());

            JSONArray jsonArray = new JSONArray();
            for (int i=0; i<blocks.size(); i++) {
                JSONObject jsonBlock = blocks.get(i).serialize();
                jsonArray.put(jsonBlock);
            }

            JSONArray jsonUnlocked = new JSONArray();
            for (int i=0; i<rows; i++) {
                JSONArray jsonUnlockedRow = new JSONArray();
                for (int j=0; j<columns; j++) {
                    jsonUnlockedRow.put(unlocked[i][j]);
                }
                jsonUnlocked.put(jsonUnlockedRow);
            }

            jsonObject.put("blocks", jsonArray);
            jsonObject.put("unlocked", jsonUnlocked);
            jsonObject.put("inventory", inventory.serialize());
            jsonObject.put("ledger", ledger.serialize());

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
        ledger.registerRevenue(type, value);
        return cashBalance;
    }

}
