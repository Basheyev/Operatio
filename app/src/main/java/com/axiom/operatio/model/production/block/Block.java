package com.axiom.operatio.model.production.block;


import com.axiom.atom.engine.data.structures.Channel;
import com.axiom.operatio.model.common.JSONSerializable;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.production.buffer.Buffer;
import com.axiom.operatio.model.production.buffer.ExportBuffer;
import com.axiom.operatio.model.production.buffer.ImportBuffer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Базовый блок производства реализующий примитивную механику
 */
public abstract class Block implements JSONSerializable {

    //---------------------------------------------------------------------------------------
    // Состояние блока
    //---------------------------------------------------------------------------------------
    public static final int IDLE = 0;
    public static final int BUSY = 1;
    public static final int FAULT = -1;

    //---------------------------------------------------------------------------------------
    // Ориентация блока (по часовой стрелке)
    //---------------------------------------------------------------------------------------
    public static final int NONE = 0;
    public static final int LEFT = 1;
    public static final int UP = 2;
    public static final int RIGHT = 3;
    public static final int DOWN = 4;


    protected Production production;                  // Производство к которомуо относится блок
    protected long ID;                                // ID блока
    protected double price;                           // Цена блока
    protected int state = IDLE;                       // Текущее состояние блока
    protected String stateDescription = "";           // Описание состояния блока
    protected long stateChangeTime;                   // Время последнего изменения состояния
    protected int inputDirection, outputDirection;    // Направление ввода и вывода
    protected int inputCapacity, outputCapacity;      // Максимальая вместимость блока в предметах
    protected Channel<Item> input;                    // Буферы ввода предметов
    protected Channel<Item> output;                   // Буферы вывода предметов
    public int column, row;                           // Координаты блока в сетке карты
    protected BlockRenderer renderer;                 // Отрисовщик
    protected boolean directionFlip = false;          // Флаг отражения направления потока

    protected long lastPollTime = 0;                  // Последнее время вытягивания предмета

    /**
     * Конструктор блока производства
     * @param production производство
     * @param inCapacity размер буфера ввода в количестве предметов
     * @param outCapacity размер буфера вывода в количестве предметов
     */
    public Block(Production production, int inDir, int inCapacity, int outDir, int outCapacity) {
        this.ID = System.currentTimeMillis();
        this.production = production;
        this.inputDirection = inDir;
        this.inputCapacity = inCapacity;
        this.outputDirection = outDir;
        this.outputCapacity = outCapacity;
        this.input = new Channel<Item>(inCapacity);
        this.output = new Channel<Item>(outCapacity);
    }


    /**
     * Обрабатывает входной поток предметов в выходной поток предметов
     */
    public abstract void process();


    /**
     * Возвращает стоимость одного цикла работы блока
     * @return стоимость
     */
    public abstract double getCycleCost();


    /**
     * Добавляет предмет во входную очередь блока
     * @param item предмет
     * @return true - если блок принял предмет, false - если нет
     */
    public boolean push(Item item) {
        if (item == null) return false;
        if (getState() == BUSY) return false;
        if (input.size() >= inputCapacity) return false;
        item.setOwner(production, this);
        input.add(item);
        return true;
    }


    /**
     * Возвращает обработанный предмет из блока, но не удаляет из выходной очереди
     * @return первый в очереди готовый предмет или null если такого нет
     */
    public Item peek() {
        return output.peek();
    }


    /**
     * Возвращает обработанный предмет из блока и удаляет из выходной очереди
     * @return первый в очереди готовый предмет или null если такого нет
     */
    public Item poll() {
        Item item = output.peek();
        if (item==null) return null;
        lastPollTime = production.getClock();
        return output.poll();
    }
    

    /**
     * Забирает один предмет из блока по направлению входа
     */
    protected boolean grabItemsFromInput() {
        Block inputBlock = production.getBlockAt(this, inputDirection);
        if (inputBlock==null) return false;      // Если на входящем направление ничего нет

        // Если выход входного блока смотрит на наш вход
        int neighborOutputDirection = inputBlock.outputDirection;
        Block neighborOutput = production.getBlockAt(inputBlock, neighborOutputDirection);
        if (neighborOutputDirection==NONE || neighborOutput==this) {
            Item item = inputBlock.peek();       // Пытаемся взять предмет из блока входа
            if (item == null) return false;      // Если ничего нет возвращаем false
            if (!push(item)) return false;       // Если не получилось добавить к себе - false
            inputBlock.poll();                   // Если получилось - удаляем из блока входа
            return true;
        }
        return false;
    }

    public void clear() {
        input.clear();
        output.clear();
    }

    /**
     * Возвращает состояние блока
     * @return состояние блока
     */
    public int getState() {
        return state;
    }

    /**
     * Возвращает описание состояние блока
     * @return описание состояния блока
     */
    public String getStateDescription() {
        return stateDescription;
    }

    /**
     * Выставляет код состояния блока
     * @param newState код нового состояния
     */
    public void setState(int newState, String description) {
        state = newState;
        stateChangeTime = production.getClock();
        stateDescription = description;
    }

    /**
     * Время последнего изменения состояния блока
     * @return время изменения в миллесекундах
     */
    public long getStateChangeTime() {
        return stateChangeTime;
    }

    /**
     * Возвращает общее количество предметов в блоке
     * @return общее количество предметов в блоке
     */
    public int getItemsAmount() {
        return input.size() + output.size();
    }

    /**
     * Возвращает общую вместимость блока по материалам
     * @return вместимость в количестве материалов
     */
    public int getCapacity() {
        return inputCapacity + outputCapacity;
    }


    /**
     * Указать направление выхода материалов
     * @param outDir направление
     */
    public void setOutputDirection(int outDir) {
        this.outputDirection = outDir;
    }


    /**
     * Указать направление входа материалов
     * @param inDir направление
     */
    public void setInputDirection(int inDir) {
        this.inputDirection = inDir;
    }


    /**
     * Установить направления потока материалов
     * @param inDir направление входа
     * @param outDir направление выхода
     */
    public void setDirections(int inDir, int outDir) {
        setInputDirection(inDir);
        setOutputDirection(outDir);
    }


    /**
     * Возвращает направление входа материалов
     * @return направление
     */
    public int getInputDirection() {
        return inputDirection;
    }


    /**
     * Возвращает направление выхода материалов
     * @return направление
     */
    public int getOutputDirection() {
        return outputDirection;
    }


    public Channel<Item> getInputQueue() {
        return input;
    }

    public Channel<Item> getOutputQueue() {
        return output;
    }

    public int getTotalCapacity() {
        return inputCapacity + outputCapacity;
    }

    public long getLastPollTime() {
        return lastPollTime;
    }

    /**
     * Возвращает блок по указнному направлению относительно текущего
     * @param direction направление
     * @return блок сосед
     */
    public Block getNeighbour(int direction) {
        return production.getBlockAt(this, direction);
    }


    /**
     * Возвращает рендер блока для отрисовки
     * @return рендер блока
     */
    public BlockRenderer getRenderer() {
        return renderer;
    }

    /**
     * Возвращает производство к которому привязан блок
     * @return производство
     */
    public Production getProduction() {
        return production;
    }

    /**
     * Возвращает ID блока
     * @return ID блока
     */
    public long getID() {
        return ID;
    }

    /**
     * Возвращает описание блока
     * @return описание блока
     */
    public abstract String getDescription();

    /**
     * Возвращает стоимость блока
     * @return стоимость блока
     */
    public double getPrice() {
        return price;
    }

    /**
     * Считает стоимость всех материалов в блоке
     * @return общая стоимость всех материалов
     */
    public double getItemsPrice() {
        double sum = 0;
        Item item;
        for (int i=0; i<input.size(); i++) {
            item = input.get(i);
            sum += item.getMaterial().getPrice();
        }
        for (int i=0; i<output.size(); i++) {
            item = output.get(i);
            sum += item.getMaterial().getPrice();
        }
        return sum;
    }


    /**
     * Возвращает все материалы в блоке на указнный склад
     * @param inventory склад
     */
    public void returnItemsTo(Inventory inventory) {
        int inputItemsCount = input.size();
        for (int i=0; i<inputItemsCount; i++) {
            inventory.push(input.poll());
        }
        int outputItemsCount = output.size();
        for (int i=0; i<outputItemsCount; i++) {
            inventory.push(output.poll());
        }
    }



    /**
     * По указанному направлению есть блок источник материалов
     * @param direction направление
     * @return true - да, false - нет
     */
    public boolean isSourceAvailable(int direction) {
        Block neighbor = getNeighbour(direction);
        if (neighbor==null) return false;
        Block neighborOutput = production.getBlockAt(neighbor, neighbor.getOutputDirection());
        return neighborOutput==this || neighbor instanceof Buffer || neighbor instanceof ImportBuffer;
    }


    /**
     * По указанному направлению есть блок приемник материалов
     * @param direction направление
     * @return true - да, false - нет
     */
    public boolean isDestinationAvailable(int direction) {
        Block neighbor = getNeighbour(direction);
        if (neighbor==null) return false;
        Block neighborInput = production.getBlockAt(neighbor, neighbor.getInputDirection());
        return neighborInput==this || neighbor instanceof Buffer || neighbor instanceof ExportBuffer;
    }


    /**
     * Проверяет смотрит ли выход или вход указанного соседа на этот блок
     * @param direction указанный блок
     * @return true - да, false - нет
     */
    public boolean hasInOutFrom(int direction) {
        Block block = getNeighbour(direction);
        if (block==null) return false;
        boolean hasInput = (production.getBlockAt(block, block.getInputDirection())==this);
        boolean hasOutput = (production.getBlockAt(block, block.getOutputDirection())==this);
        boolean itsBuffer = block instanceof Buffer;
        boolean itsImportBuffer = block instanceof ImportBuffer;
        boolean itsExportBuffer = block instanceof ExportBuffer;
        return hasInput || hasOutput || itsBuffer || itsImportBuffer || itsExportBuffer;
    }


    /**
     * Зеркально отражает направление потока материалов
     */
    public void flipDirection() {
        setDirections(outputDirection, inputDirection);
    }

    /**
     * Является ли поток материалов прямым (вход напротив выхода)
     * @return true если да, false если нет
     */
    public boolean isStraight() {
        return BlockAdjuster.oppositeDirection(inputDirection) == outputDirection;
    }

    /**
     * Возвращает левое направление по отношению к выходу
     * @return левое направление по отношению к выходу
     */
    public int getLeftDirection() {
        int leftDir = outputDirection - 1;
        return leftDir < LEFT ? DOWN : leftDir;
    }

    /**
     * Возвращает правое направление по отношению к выходу
     * @return правое направление по отношению к выходу
     */
    public int getRightDirection() {
        int rightDir = outputDirection + 1;
        return  rightDir > DOWN ? LEFT : rightDir;
    }

    /**
     * Сериализует блок в JSONObject
     * @return JSONObject
     */
    public JSONObject toJSON() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ID", ID);
            jsonObject.put("state", state);
            jsonObject.put("inputDirection", inputDirection);
            jsonObject.put("outputDirection", outputDirection);
            jsonObject.put("inputCapacity", inputCapacity);
            jsonObject.put("outputCapacity", outputCapacity);
            jsonObject.put("column", column);
            jsonObject.put("row", row);

            // Сохраняем очередь входящих в обратном порядке
            JSONArray jsonInputArray = new JSONArray();
            for (int i=0; i<input.size(); i++) {
                JSONObject item = input.get(i).toJSON();
                jsonInputArray.put(item);
            }
            jsonObject.put("input", jsonInputArray);

            // Сохраняем очередь исходящих в обратном порядке
            JSONArray jsonOutputArray = new JSONArray();
            for (int i=0; i<output.size(); i++) {
                JSONObject item = output.get(i).toJSON();
                jsonOutputArray.put(item);
            }
            jsonObject.put("output", jsonOutputArray);

            return jsonObject;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


}
