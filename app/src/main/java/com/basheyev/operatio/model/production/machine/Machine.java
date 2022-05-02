package com.basheyev.operatio.model.production.machine;

import com.basheyev.atom.engine.data.structures.Channel;
import com.basheyev.operatio.model.common.JSONSerializable;
import com.basheyev.operatio.model.ledger.Ledger;
import com.basheyev.operatio.model.production.block.Block;
import com.basheyev.operatio.model.production.Production;
import com.basheyev.operatio.model.production.block.BlockBuilder;
import com.basheyev.operatio.model.production.buffer.Buffer;
import com.basheyev.operatio.model.materials.Item;
import com.basheyev.operatio.model.materials.Material;
import com.basheyev.operatio.model.production.buffer.ExportBuffer;
import com.basheyev.operatio.model.production.conveyor.Conveyor;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Модель экземпляра маштны (оборудования)
 */
public class Machine extends Block implements JSONSerializable {

    public static final String MSG_READY = "Ready";
    public static final String MSG_WRONG_INPUT = "Wrong input";
    public static final String MSG_OUTPUT_BUSY = "Output is busy";
    public static final String MSG_BUSY = "Work in progress...";


    protected MachineType type;
    protected Operation operation;

    private int[] matCounter;
    private int cyclesLeft = 0;

    public Machine(Production production, MachineType type, Operation op, int inDir, int outDir) {
        super(production, inDir, op.totalInputAmount(), outDir, op.totalOutputAmount());
        this.type = type;
        this.price = type.price;
        this.operation = op;
        this.matCounter = new int[4];
        this.renderer = new MachineRenderer(this);
    }

    public Machine(Production production, JSONObject jsonObject, int inDir, int inCapaciy, int outDir, int outCapacity) {
        super(production, inDir, inCapaciy, outDir, outCapacity);
        BlockBuilder.parseCommonFields(this, jsonObject);
        try {
            type = MachineType.getMachineType(jsonObject.getInt("machineType"));
            price = type.price;
            operation = type.getOperation(jsonObject.getInt("operation"));
            if (type==null || operation==null) throw new JSONException("Data corrupt - no such MachineType or Operation");
            matCounter = new int[4];
            cyclesLeft = jsonObject.getInt("cyclesLeft");
            renderer = new MachineRenderer(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean push(Item item) {
        // Проверяем допустимый ли материал для добавления в очередь ввода
        if (!operation.isCorrectInput(item.getMaterial())) {
            setState(FAULT, MSG_WRONG_INPUT);
            return false;
        }
        // Если на выходе машины что-то еще осталось не брать новый материал
        if (output.remainingCapacity() < outputCapacity) {
            setState(FAULT, MSG_OUTPUT_BUSY);
            return false;
        }
        // Если все материалы собраны уходим
        if (operationInputVerified(matCounter)) return false;

        // Если материала Item Уже собрано достаточно уходим
        Material[] inputMaterials = operation.getInputs();
        for (int i=0; i < inputMaterials.length; i++)
        if (inputMaterials[i]==item.getMaterial()) {
            if (matCounter[i] <= 0) {
                return false;
            }
        }

        return super.push(item);
    }


    @Override
    public void process() {

        // Если был FAULT и на выход есть предметы - пробуем вытолкнуть на выход
        if (getState()==FAULT && output.size() > 0) {
            if (pushToOutput()) {
                setState(IDLE, MSG_READY);
            } else {
                setState(FAULT, MSG_BUSY);
            }
        }

        // Если машина работает
        if (getState()==BUSY) {
            // Если время операции прошло и выход свободен
            if (getState()==BUSY && cyclesLeft==0 && output.size()==0) {
                input.clear();              // Удаляем входящие предметы
                generateOutput();           // Генерируем выходные предметы
                pushToOutput();             // Выталкиваем предметы на вывод
                setState(IDLE, MSG_READY);  // Устанавливаем состояние IDLE
            }
            // уменьшаем счетчик оставшихся циклов работы
            if (cyclesLeft > 0) cyclesLeft--;
            return;
        }

        // Если количество предметов во входящей очереди меньше чем необходимо для операции
        // пытаемся самостоятельно взять из направления входа (блок)
        int totalAmount = operation.totalInputAmount();
        if (input.size() < totalAmount) {
            grabItemsFromInput();
            return;
        }

        // Подтверждаем, что есть необходимое количество каждого предмета по Операции
        if (operationInputVerified(matCounter)) {       // Начинаем работу машины
            setState(BUSY, MSG_BUSY);                   // Устанавливаем состояние - BUSY
            cyclesLeft = operation.getCycles();         // Указываем количество циклов работы
        }

    }


    /**
     * Забирает предметы из блока по направлению входа
     */
    @Override
    protected boolean grabItemsFromInput() {
        Block inputBlock = production.getBlockAt(this, inputDirection);
        if (inputBlock==null) return false;     // Если на входящем направление ничего нет

        // Если выход входного блока смотрит на наш вход или он не имеет направления (буфер)
        int neighborOutputDirection = inputBlock.getOutputDirection();
        Block neighborOutput = production.getBlockAt(inputBlock, neighborOutputDirection);
        if (neighborOutputDirection==NONE || neighborOutput==this) {

            // Проверяем количество и тип входящих материалов
            // Считаем сколько не хватает еще материалов и какого вида, если хватает уходим
            if (operationInputVerified(matCounter)) return false;

            // Пытаемся взять материалы по списку входящих материалов
            for (int i = 0; i<operation.getInputs().length; i++) {
                Material material = operation.getInputs()[i];
                for (int j=0; j<matCounter[i]; j++) {            // По количеству недостающих
                    if (inputBlock instanceof Buffer) {
                        Buffer inputBuffer = (Buffer) inputBlock;
                        Item item = inputBuffer.peek(material);  // Пытаемся взять предмет из блока входа
                        if (item == null) {                      // Если ничего нет пробуем взять
                            setState(IDLE, MSG_READY);           // Устанавливаем состояние IDLE
                            continue;
                        }
                        if (!push(item)) continue;           // Если не получилось добавить к себе
                        inputBuffer.poll(material);          // Если получилось - удаляем из блока входа
                    } else {
                        Item item = inputBlock.peek();       // Пытаемся взять предмет из блока входа
                        if (item == null) {                  // Если ничего нет возвращаем
                            setState(IDLE, MSG_READY);                  // Устанавливаем состояние IDLE
                            continue;
                        }
                        if (!push(item)) continue;           // Если не получилось добавить к себе
                        inputBlock.poll();                   // Если получилось - удаляем из блока входа
                    }
                }
            }

        }
        return true;
    }


    /**
     * Проверяет, есть ли все предметы для получения выходного предмета (по операции)
     * @param matCounter записывает сколько и каких материалов не хватает
     * @return true - если есть, false - если нет
     */
    protected boolean operationInputVerified(int[] matCounter) {
        // Копируем количество необходимого входного материала из описания операции в matCounter
        System.arraycopy(operation.getInputAmount(), 0, matCounter,0, operation.getInputAmount().length);
        // Алгоритм построен с учтом того, что все входящие материалы входят в рецепт операции
        for (int k=0; k<input.size(); k++) {
            //
            Item item = input.get(k);
            if (item==null) continue;
            // Берем код очередного материала из входящей очереди
            int materialID = item.getMaterial().getID();
            // Проверяем есть ли такой материал в списке входных материалов операции
            for (int i = 0; i<operation.getInputs().length; i++) {
                // Если нашли такой же материал, то уменьшаем счетчик необходимых материалов
                if (operation.getInputs()[i].getID()==materialID) {
                    matCounter[i]--;
                    break;
                }
            }
        }
        // Если все необходимые материалы есть, то каждый элемент matCounter будет меньше/равен 0
        for (int i=0; i<matCounter.length; i++) {
            if (matCounter[i] > 0) return false; // Если больше 0, предметов не хватает
        }
        // Все необходимые материалы есть
        return true;
    }


    // Добавляем на выход исходящие предметы
    protected void generateOutput() {
        Item item;
        Ledger ledger = production.getLedger();
        for (int i=0; i<operation.getOutputAmount().length; i++) {
            Material material = operation.getOutputs()[i];

            // Регистрируем факт производства материала
            ledger.materialManufactured(material.getID(), operation.getOutputAmount()[i]);

            for (int j=0; j<operation.getOutputAmount()[i]; j++) {
                item = new Item(material);
                item.setOwner(production,this);
                output.push(item);
            }
        }
    }


    private boolean pushToOutput() {
        Block outputBlock = production.getBlockAt(this,outputDirection);
        if (outputBlock==null) return false;
        if (!(outputBlock instanceof Buffer
           || outputBlock instanceof ExportBuffer
           || outputBlock instanceof Conveyor)) return false;

        if (outputBlock instanceof Conveyor) {
            if (production.getBlockAt(outputBlock, outputBlock.getInputDirection()) != this) {
                return false;
            }
        }

        Item item;
        while (output.size() > 0) {
            item = output.peek();
            if (outputBlock.push(item)) output.poll(); else return false;
        }

        return true;
    }


    @Override
    public void clear() {
        input.clear();
        output.clear();
        cyclesLeft = 0;
        setState(IDLE, MSG_READY);
    }

    @Override
    public void setState(int newState, String description) {
        if (getState()==newState) return;
        MachineRenderer machineRenderer = (MachineRenderer) renderer;
        switch (newState) {
            case Block.IDLE: machineRenderer.setIdleAnimation(); break;
            case Block.BUSY: machineRenderer.setBusyAnimation(); break;
            case Block.FAULT: machineRenderer.setIdleAnimation(); break;
        }
        super.setState(newState, description);
    }


    @Override
    public String getDescription() {
        return type.getDescription();
    }

    public MachineType getType() {
        return type;
    }

    @Override
    public double getCycleCost() {
        return type.getCycleCost();
    }

    public Operation getOperation() { return operation; }

    public int getOperationID() {
        return type.getOperationID(operation);
    }

    /**
     * Переналадка машины на новую операцию
     * @param ID номер операции для данного типа машины
     */
    public void setOperation(int ID) {
        operation = type.getOperation(ID);
        inputCapacity = operation.totalInputAmount();
        outputCapacity = operation.totalOutputAmount();
        input = new Channel<Item>(inputCapacity);
        output = new Channel<Item>(outputCapacity);
        // обнулить машину
        clear();
        setState(IDLE, MSG_READY);
    }


    @Override
    public void setOutputDirection(int outDir) {
        super.setOutputDirection(outDir);
        ((MachineRenderer)renderer).arrangeAnimation(inputDirection, outputDirection);
    }

    @Override
    public void setInputDirection(int inDir) {
        super.setInputDirection(inDir);
        ((MachineRenderer)renderer).arrangeAnimation(inputDirection, outputDirection);
    }

    @Override
    public void setDirections(int inDir, int outDir) {
        super.setDirections(inDir, outDir);
        ((MachineRenderer)renderer).arrangeAnimation(inputDirection, outputDirection);
    }


    public JSONObject toJSON() {
        JSONObject jsonObject = super.toJSON();
        try {
            jsonObject.put("class", "Machine");
            jsonObject.put("machineType", type.getID());
            jsonObject.put("operation", getOperationID());
            jsonObject.put("cyclesLeft", cyclesLeft);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }

}
