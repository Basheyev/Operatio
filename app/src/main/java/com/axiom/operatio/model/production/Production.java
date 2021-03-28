package com.axiom.operatio.model.production;

import com.axiom.atom.R;
import com.axiom.operatio.model.common.JSONSerializable;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.gameplay.MissionManager;
import com.axiom.operatio.model.gameplay.GameMission;
import com.axiom.operatio.model.gameplay.GamePermissions;
import com.axiom.operatio.model.ledger.Ledger;
import com.axiom.operatio.model.market.Market;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.production.block.BlockBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Модель производства
 */
public class Production implements JSONSerializable {

    public static final int MAP_WIDTH = 32;
    public static final int MAP_HEIGHT = 24;
    public static final int UNLOCKED_WIDTH = 8;
    public static final int UNLOCKED_HEIGHT = 6;
    public static final int TILE_PRICE = 500;     // Цена одной плитки площади

    public static final int CYCLE_TIME = 300;     // Длительность цикла в миллесекундах

    private Inventory inventory;                  // Объект - склад
    private Market market;                        // Объект - рынок
    private Ledger ledger;                        // Объект - игровая статистика
    private GamePermissions permissions;          // Разрешения в игре
    private int level = 0;                        // Текущий уровень
    private int lastCompletedLevel = 0;           // Последний завершенный уровень

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

    private ProductionRenderer renderer = null;
    private int levelCompletedSound;


    public Production() {

        levelCompletedSound = SoundRenderer.loadSound(R.raw.yes_snd);

        this.columns = MAP_WIDTH;
        this.rows = MAP_HEIGHT;
        grid = new Block[rows][columns];
        unlocked = new boolean[rows][columns];
        setAreaUnlocked(0,0, columns, rows, false);
        int centerCol = (columns / 2) - (UNLOCKED_WIDTH / 2);
        int centerRow = (rows / 2) - (UNLOCKED_HEIGHT / 2);
        setAreaUnlocked(centerCol,centerRow,UNLOCKED_WIDTH, UNLOCKED_HEIGHT, true);

        blocks = new ArrayList<Block>(100);
        inventory = new Inventory(this);
        market = new Market(this);
        ledger = new Ledger(this);
        renderer = new ProductionRenderer(this);

        float cameraX = (columns / 2) * renderer.getCellWidth();
        float cameraY = (rows / 2) * renderer.getCellHeight();
        Camera.getInstance().lookAt(cameraX, cameraY);

        permissions = new GamePermissions();
        GameMission stub = MissionManager.getMission(0);
        if (stub!=null) {
            stub.earnReward(this);
            level = 1;
        }
    }


    public Production(JSONObject jsonObject) throws JSONException {

        levelCompletedSound = SoundRenderer.loadSound(R.raw.yes_snd);

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
        renderer = new ProductionRenderer(this);

        try {
            float cameraX = (float) jsonObject.getDouble("cameraX");
            float cameraY = (float) jsonObject.getDouble("cameraY");
            float cellWidth = (float) jsonObject.getDouble("cellWidth");
            float cellHeight = (float) jsonObject.getDouble("cellHeight");
            Camera.getInstance().lookAt(cameraX, cameraY);
            renderer.setCellSize(cellWidth, cellHeight);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = jsonObject.getJSONArray("blocks");
        blocks = new ArrayList<Block>(jsonArray.length());
        for (int i=0; i<jsonArray.length(); i++) {
            JSONObject jsonBlock = jsonArray.getJSONObject(i);
            Block block = BlockBuilder.deserialize(this, jsonBlock);
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

        try {
            JSONObject jsonLedger = jsonObject.getJSONObject("ledger");
            ledger = new Ledger(this, jsonLedger);
        } catch (JSONException e) {
            e.printStackTrace();
            ledger = new Ledger(this);
        }


        try {
            JSONObject jsonPermissions = jsonObject.getJSONObject("permissions");
            permissions = new GamePermissions(jsonPermissions);
        } catch (JSONException e) {
            e.printStackTrace();
            permissions = new GamePermissions();
            GameMission stub = MissionManager.getMission(0);
            if (stub!=null) {
                stub.earnReward(this);
                level = 1;
            }
        }
    }




    /**
     * Симулирует цикл производства
     */
    public void process() {
        if (!isPaused) {
            updateClock();
            long now = getClock();
            if (now - lastCycleTime > cycleMilliseconds) {
                Block block;
                boolean energyPayed;
                int size = blocks.size();
                int expenseType = Ledger.EXPENSE_BLOCK_OPERATION;
                for (int i = 0; i < size; i++) {
                    block = blocks.get(i);
                    energyPayed = ledger.creditCashBalance(expenseType, block.getCycleCost());
                    if (energyPayed) block.process();  // Если энергия оплачена отрабатываем
                    else {
                        block.setState(Block.FAULT);
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
        }
    }


    private synchronized void updateClock() {
        // Учитываем время производства в миллисекундах с учетом пауз игры
        clock = System.currentTimeMillis() - pausedTime;
    }

    /**
     * Возвращает время производства в миллисекундах с учетом пауз игры
     * @return время в миллисекундах
     */
    public synchronized long getClock() {
        return clock;
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



    public void setAreaUnlocked(int col, int row, int w, int h, boolean state) {
        if (col < 0) col = 0;
        if (row < 0) row = 0;
        if (col >= columns) col = columns - 1;
        if (row >= rows) row = rows - 1;
        if (col + w >= columns) w = (columns - col);
        if (row + h >= rows) h = (rows - row);
        for (int y=row; y<row+h; y++) {
            for (int x=col; x<col+w; x++) {
                unlocked[y][x] = state;
            }
        }
    }

    private void checkLevelConditions() {
        // Проверка условий уровня
        GameMission mission = MissionManager.getMission(level);
        if (mission.checkWinConditions(this) && lastCompletedLevel != level) {
            SoundRenderer.playSound(levelCompletedSound);
            mission.earnReward(this);
            lastCompletedLevel = level;
            if (level + 1 <= MissionManager.size() - 1) level++;
        }
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


    public GamePermissions getPermissions() {
        return permissions;
    }

    public int getLevel() {
        return level;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public ProductionRenderer getRenderer() {
        return renderer;
    }

    public JSONObject toJSON() {

        try {
            JSONObject jsonObject = new JSONObject();
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

            // Camera & Zoom
            jsonObject.put("cameraX", Camera.getInstance().getX());
            jsonObject.put("cameraY", Camera.getInstance().getY());
            jsonObject.put("cellWidth", renderer.getCellWidth());
            jsonObject.put("cellHeight", renderer.getCellHeight());

            JSONArray jsonArray = new JSONArray();
            for (int i=0; i<blocks.size(); i++) {
                JSONObject jsonBlock = blocks.get(i).toJSON();
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
            jsonObject.put("inventory", inventory.toJSON());
            jsonObject.put("ledger", ledger.toJSON());
            jsonObject.put("permissions", permissions.toJSON());

            return jsonObject;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }



}
