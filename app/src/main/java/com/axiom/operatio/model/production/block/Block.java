package com.axiom.operatio.model.production.block;


import com.axiom.atom.engine.data.Channel;
import com.axiom.operatio.model.common.JSONSerializable;
import com.axiom.operatio.model.inventory.Inventory;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.production.buffer.Buffer;
import com.axiom.operatio.model.production.buffer.ExportBuffer;
import com.axiom.operatio.model.production.buffer.ImportBuffer;
import com.axiom.operatio.model.production.conveyor.Conveyor;
import com.axiom.operatio.model.production.machine.Machine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Базовый блок производства реализующий примитивную механику
 */
public abstract class Block implements JSONSerializable {

    protected Production production;                  // Производство к которомуо относится блок
    protected static int blockCounter = 0;
    protected int ID;                                 // ID блока
    protected double price;                           // Цена блока
    protected int state = IDLE;                       // Текущее состояние блока
    protected long stateChangeTime;                   // Время последнего изменения состояния
    protected int inputDirection, outputDirection;    // Направление ввода и вывода
    protected int inputCapacity, outputCapacity;      // Максимальая вместимость блока в предметах
    protected Channel<Item> input;                    // Буферы ввода предметов
    protected Channel<Item> output;                   // Буферы вывода предметов
    public int column, row;                           // Координаты блока в сетке карты
    protected BlockRenderer renderer;                 // Отрисовщик

    public boolean directionFlip = false;

    /**
     * Конструктор блока производства
     * @param production производство
     * @param inCapacity размер буфера ввода в количестве предметов
     * @param outCapacity размер буфера вывода в количестве предметов
     */
    public Block(Production production, int inDir, int inCapacity, int outDir, int outCapacity) {
        this.ID = blockCounter++;
        this.production = production;
        this.inputDirection = inDir;
        this.inputCapacity = inCapacity;
        this.outputDirection = outDir;
        this.outputCapacity = outCapacity;
        this.input = new Channel<Item>(inCapacity);
        this.output = new Channel<Item>(outCapacity);
    }


    public void setOutputDirection(int outDir) {
        this.outputDirection = outDir;
    }

    public void setInputDirection(int inDir) {
        this.inputDirection = inDir;
    }

    public void setDirections(int inDir, int outDir) {
        setInputDirection(inDir);
        setOutputDirection(outDir);
    }

    /**
     * Добавляет предмет во входную очередь блока
     * @param item предмет
     * @return true - если блок принял предмет, false - если нет
     */
    public boolean push(Item item) {
        if (item==null) return false;
        if (getState()==BUSY) return false;
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
        return output.poll();
    }
    

    /**
     * Забирает один предмет из блока по направлению входа
     * @return true - если получилось забрать, false - если нет
     */
    protected void grabItemsFromInputDirection() {
        Block inputBlock = production.getBlockAt(this, inputDirection);
        if (inputBlock==null) return;         // Если на входящем направление ничего нет

        // Если выход входного блока смотрит на наш вход
        int neighborOutputDirection = inputBlock.outputDirection;
        Block neighborOutput = production.getBlockAt(inputBlock, neighborOutputDirection);
        if (neighborOutputDirection==NONE || neighborOutput==this) {
            Item item = inputBlock.peek();       // Пытаемся взять предмет из блока входа
            if (item == null) return;            // Если ничего нет возвращаем false
            if (!push(item)) return;             // Если не получилось добавить к себе - false
            inputBlock.poll();                   // Если получилось - удаляем из блока входа
        }
    }


    /**
     * Возвращает состояние блока
     * @return состояние блока
     */
    public int getState() {
        return state;
    }


    /**
     * Выставляет код состояния блока
     * @param newState код нового состояния
     */
    public void setState(int newState) {
        state = newState;
        stateChangeTime = production.getClock();
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

    public int getCapacity() {
        return inputCapacity + outputCapacity;
    }

    public int getInputDirection() {
        return inputDirection;
    }

    public int getOutputDirection() {
        return outputDirection;
    }

    public BlockRenderer getRenderer() {
        return renderer;
    }

    public Production getProduction() {
        return production;
    }

    public int getID() {
        return ID;
    }

    public double getPrice() {
        return price;
    }


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


    public abstract double getCycleCost();

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


    public static Block deserialize(Production production, JSONObject jsonObject) {
        Block block = null;
        try {
            String type = jsonObject.getString("class");
            int inputDirection = jsonObject.getInt("inputDirection");
            int outputDirection = jsonObject.getInt("outputDirection");
            int inputCapacity = jsonObject.getInt("inputCapacity");
            int outputCapacity = jsonObject.getInt("outputCapacity");

            switch (type) {
                case "Buffer":
                    block = new Buffer(production, jsonObject, inputCapacity);
                    break;
                case "Conveyor":
                    block = new Conveyor(production, jsonObject, inputDirection, outputDirection);
                    break;
                case "Machine":
                    block = new Machine(production, jsonObject, inputDirection, inputCapacity, outputDirection, outputCapacity);
                    break;
                case "ImportBuffer":
                    block = new ImportBuffer(production, jsonObject);
                    break;
                case "ExportBuffer":
                    block = new ExportBuffer(production, jsonObject);
                    break;
                default:
                    throw new JSONException("Unknown block");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return block;
    }


    protected static void deserializeCommonFields(Block block, JSONObject jsonObject) {
        try {
            block.ID = jsonObject.getInt("ID");
            block.state = jsonObject.getInt("state");
            block.inputDirection = jsonObject.getInt("inputDirection");
            block.outputDirection = jsonObject.getInt("outputDirection");
            block.inputCapacity = jsonObject.getInt("inputCapacity");
            block.outputCapacity = jsonObject.getInt("outputCapacity");
            block.column = jsonObject.getInt("column");
            block.row = jsonObject.getInt("row");

            JSONArray jsonInputArray = jsonObject.getJSONArray("input");
            for (int i = 0; i < jsonInputArray.length(); i++) {
                JSONObject jsonItem = (JSONObject) jsonInputArray.get(i);
                Item item = Item.deserialize(jsonItem, block);
                block.input.add(item);
            }

            JSONArray jsonOutputArray = jsonObject.getJSONArray("output");
            for (int i = 0; i < jsonOutputArray.length(); i++) {
                JSONObject jsonItem = (JSONObject) jsonOutputArray.get(i);
                Item item = Item.deserialize(jsonItem, block);
                block.output.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /**
     * Обрабатывает входной поток предметов в выходной поток предметов
     */
    public abstract void process();


    //---------------------------------------------------------------------------------------
    // Состояние блока
    //---------------------------------------------------------------------------------------
    public static final int IDLE = 0;
    public static final int BUSY = 1;
    public static final int FAULT = -1;

    //---------------------------------------------------------------------------------------
    // Ориентация (по часовой стрелке)
    //---------------------------------------------------------------------------------------
    public static final int NONE = 0;
    public static final int LEFT = 1;
    public static final int UP = 2;
    public static final int RIGHT = 3;
    public static final int DOWN = 4;

    /**
     * Следующее направление по часовой стрелке
     * @param direction текущее направление
     * @return следующее направление по часовой стрелке
     */
    public static int nextClockwiseDirection(int direction) {
        if (direction==NONE || direction < 0) return NONE;
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


    //--------------------------------------------------------------------------------------
    // Установка направления потока материалов
    //--------------------------------------------------------------------------------------

    /**
     * Устанавливает направления потока материалов с учётом соседних блоков
     */
    public void adjustDirection() {
        boolean upper = hasInOutFrom(UP);
        boolean down  = hasInOutFrom(DOWN);
        boolean left  = hasInOutFrom(LEFT);
        boolean right = hasInOutFrom(RIGHT);
        int neighborsCount = (upper ? 1:0) + (down ? 1:0) + (left ? 1:0) + (right ? 1:0);
        if (neighborsCount == 1) {
            int neighborSide = (upper ? UP : down ? DOWN : left ? LEFT : RIGHT);
            adjustOneNeighbor(neighborSide);
        }
        else if (neighborsCount == 2) {
            if (left && right) adjustTwoNeighbors(LEFT, RIGHT);
            if (left && upper) adjustTwoNeighbors(LEFT, UP);
            if (left && down) adjustTwoNeighbors(LEFT, DOWN);
            if (right && upper) adjustTwoNeighbors(RIGHT, UP);
            if (right && down) adjustTwoNeighbors(RIGHT, DOWN);
            if (upper && down) adjustTwoNeighbors(UP, DOWN);
        }

    }


    /**
     * Устанавливает направление потока материалов если есть один сосед
     * @param side с какой стороны находится соседний блок
     */
    private void adjustOneNeighbor(int side) {
        if (isSourceAvailable(side)) {
            setDirections(side, oppositeDirection(side));
        } else if (isDestinationAvailable(side)) {
            setDirections(oppositeDirection(side), side);
        }
    }


    /**
     * Устанавливает направление потока материалов если есть два соседа
     * @param side1 сторона первого соседа
     * @param side2 сторона второго соседа
     */
    private void adjustTwoNeighbors(int side1, int side2) {
        if (hasInOutFrom(side1) && !hasInOutFrom(side2)) {
            adjustOneNeighbor(side1);
        } else if (!hasInOutFrom(side1)) {
            adjustOneNeighbor(side2);
        } else if (isDestinationAvailable(side1) && isSourceAvailable(side2)) {
            setDirections(side2, side1);
        } else if (isSourceAvailable(side1) && isDestinationAvailable(side2)) {
            setDirections(side1, side2);
        }
    }


    //--------------------------------------------------------------------------------------
    // Поворот направления потока материалов
    //--------------------------------------------------------------------------------------

    /**
     * Поворачивает направление потока материалов
     */
    public void rotateDirection() {
        boolean upper = hasInOutFrom(UP);
        boolean down  = hasInOutFrom(DOWN);
        boolean left  = hasInOutFrom(LEFT);
        boolean right = hasInOutFrom(RIGHT);
        int neighborsCount = (upper ? 1:0) + (down ? 1:0) + (left ? 1:0) + (right ? 1:0);

        if (neighborsCount==0) {
            rotateClockwise();
        } else {
            if (neighborsCount==1) {
                int neighborSide = (upper ? UP : down ? DOWN : left ? LEFT : RIGHT);
                rotateOneNeighbor(neighborSide);
            } else if (neighborsCount==2) {
                if (left && right) rotateTwoNeighbors(LEFT, RIGHT);
                if (left && upper) rotateTwoNeighbors(LEFT, UP);
                if (left && down) rotateTwoNeighbors(LEFT, DOWN);
                if (right && upper) rotateTwoNeighbors(RIGHT, UP);
                if (right && down) rotateTwoNeighbors(RIGHT, DOWN);
                if (upper && down) rotateTwoNeighbors(UP, DOWN);
            } else {
                // todo более умный поворот при 3-4 соседа
                rotateClockwise();
            }
        }

    }


    /**
     * Поворачивает направление потока материалов на 90 градусов
     */
    private void rotateClockwise() {
        int currentInpDir = getInputDirection();
        int currentOutDir = getOutputDirection();
        int newInpDir = nextClockwiseDirection(currentInpDir);
        int newOutDir = nextClockwiseDirection(currentOutDir);
        setDirections(newInpDir, newOutDir);
    }


    /**
     * Поворачивает направление потока материалов при одном соседе
     */
    private void rotateOneNeighbor(int side) {
        if (isSourceAvailable(side)) {
            int newOutDir = nextClockwiseDirection(outputDirection);
            if (newOutDir == inputDirection) newOutDir = nextClockwiseDirection(newOutDir);
            setDirections(side, newOutDir);
        } else if (isDestinationAvailable(side)) {
            int newInpDir = nextClockwiseDirection(inputDirection);
            if (newInpDir == outputDirection) newInpDir = nextClockwiseDirection(newInpDir);
            setDirections(newInpDir, side);
        }
    }



    /**
     * Поворачивает направление потока материалов при двух соседях
     */
    private void rotateTwoNeighbors(int side1, int side2) {
        boolean side1Available = hasInOutFrom(side1);
        boolean side2Available = hasInOutFrom(side2);
        if (!side1Available && !side2Available) rotateClockwise();
        else if (side1Available && !side2Available) rotateOneNeighbor(side1);
        else if (!side1Available && side2Available) rotateOneNeighbor(side2);
        else {
            if (isSourceAvailable(side1) && isSourceAvailable(side2)) flipDirection(); else
            if (isSourceAvailable(side1)) rotateOneNeighbor(side1); else
            if (isSourceAvailable(side2)) rotateOneNeighbor(side2);
        }
    }


    //----------------------------------------------------------------------------------------------
    // Вспомогательные методы
    //----------------------------------------------------------------------------------------------

    private Block getNeighbour(int direction) {
        return production.getBlockAt(this, direction);
    }


    private boolean isSourceAvailable(int direction) {
        Block neighbor = getNeighbour(direction);
        if (neighbor==null) return false;
        Block neighborOutput = production.getBlockAt(neighbor, neighbor.getOutputDirection());
        return neighborOutput==this || neighbor instanceof Buffer || neighbor instanceof ImportBuffer;
    }


    private boolean isDestinationAvailable(int direction) {
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
    private boolean hasInOutFrom(int direction) {
        Block block = getNeighbour(direction);
        if (block==null) return false;
        boolean hasInput = (production.getBlockAt(block, block.getInputDirection())==this);
        boolean hasOutput = (production.getBlockAt(block, block.getOutputDirection())==this);
        boolean itsBuffer = block instanceof Buffer;
        boolean itsImportBuffer = block instanceof ImportBuffer;
        boolean itsExportBuffer = block instanceof ExportBuffer;
        return hasInput || hasOutput || itsBuffer || itsImportBuffer || itsExportBuffer;
    }


    private void flipDirection() {
        setDirections(outputDirection, inputDirection);
    }
}
