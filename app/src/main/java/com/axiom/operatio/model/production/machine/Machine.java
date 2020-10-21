package com.axiom.operatio.model.production.machine;

import com.axiom.atom.engine.data.Channel;
import com.axiom.operatio.model.production.block.Block;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.buffer.Buffer;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.buffer.BufferKeepingUnit;
import com.axiom.operatio.model.production.buffer.ExportBuffer;
import com.axiom.operatio.model.production.conveyor.Conveyor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// TODO Сериализация
public class Machine extends Block {

    protected MachineType type;
    protected Operation operation;

    private int[] matCounter;
    private int cyclesLeft = 0;

    public Machine(Production production, MachineType type, Operation op, int inDir, int outDir) {
        super(production, inDir, op.totalInputAmount(), outDir, op.totalOutputAmount());
        this.type = type;
        this.operation = op;
        this.matCounter = new int[4];
        this.renderer = new MachineRenderer(this);
    }

    public Machine(Production production, JSONObject jsonObject, int inDir, int inCapaciy, int outDir, int outCapacity) {
        super(production, inDir, inCapaciy, outDir, outCapacity);
        deserializeCommonFields(this, jsonObject);
        try {
            type = MachineType.getMachineType(jsonObject.getInt("machineType"));
            operation = type.getOperation(jsonObject.getInt("operation"));
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
        if (!operation.isCorrectInput(item.getMaterial())) return false;
        if (output.remainingCapacity() < outputCapacity) return false;
        return super.push(item);
    }

    @Override
    public void process() {

        // Если машина работает, уменьшаем счетчик оставшихся циклов работы
        if (getState()==BUSY && cyclesLeft > 0) {
            cyclesLeft--;
            // Если время операции прошло и выход свободен
            if (cyclesLeft==0 && output.remainingCapacity() <= outputCapacity) {
                input.clear();       // Удаляем входящие предметы
                generateOutput();    // Генерируем выходные предметы
                setState(IDLE);      // Устанавливаем состояние IDLE
            }
            return;
        }

        // Если количество предметов во вхоядщий очереди меньше чем необходимо для операции
        // пытаемся самостоятельно взять из направления входа (блок)
        int totalAmount = operation.totalInputAmount();
        if (input.size() < totalAmount) {
            grabItemsFromInputDirection();
            return;
        }

        // Подтверждаем, что есть необходимое количество каждого предмета по Операции
        if (operationInputVerified(matCounter)) {    // Начинаем работу машины
            setState(BUSY);                          // Устанавливаем состояние - BUSY
            cyclesLeft = operation.operationTime;    // Указываем количество циклов работы
        }

    }


    /**
     * Забирает предметы из блока по направлению входа
     */
    @Override
    protected void grabItemsFromInputDirection() {
        Block inputBlock = production.getBlockAt(this, inputDirection);
        if (inputBlock==null) return;     // Если на входящем направление ничего нет

        // Если выход входного блока смотрит на наш вход или он не имеет направления (буфер)
        int neighborOutputDirection = inputBlock.getOutputDirection();
        Block neighborOutput = production.getBlockAt(inputBlock, neighborOutputDirection);
        if (neighborOutputDirection==NONE || neighborOutput==this) {

            // Считаем сколько не хватает еще материалов и какого вида, если хватает уходим
            if (operationInputVerified(matCounter)) return;

            // Пытаемся взять материалы по списку входящих материалов
            for (int i=0; i<operation.inputMaterials.length; i++) {
                Material material = operation.inputMaterials[i];
                for (int j=0; j<matCounter[i]; j++) {       // По количеству недостающих
                    if (inputBlock instanceof Buffer) {
                        Buffer inputBuffer = (Buffer) inputBlock;
                        Item item = inputBuffer.peek(material);      // Пытаемся взять предмет из блока входа
                        if (item == null) continue;          // Если ничего нет возвращаем false
                        if (!push(item)) continue;           // Если не получилось добавить к себе - false
                        inputBuffer.poll(material);          // Если получилось - удаляем из блока входа
                    } else {
                        Item item = inputBlock.peek();       // Пытаемся взять предмет из блока входа
                        if (item == null) continue;          // Если ничего нет возвращаем false
                        if (!push(item)) continue;           // Если не получилось добавить к себе - false
                        inputBlock.poll();                   // Если получилось - удаляем из блока входа
                    }
                }
            }

        }

    }


    /**
     * Проверяет, есть ли все предметы для получения выходного предмета (по операции)
     * @param matCounter записывает сколько и каких материалов не хватает
     * @return true - если есть, false - если нет
     */
    protected boolean operationInputVerified(int[] matCounter) {
        // Копируем количество необходимого входного материала из описания операции в matCounter
        System.arraycopy(operation.inputAmount, 0, matCounter,0, operation.inputAmount.length);
        // Алгоритм построен с учтом того, что все входящие материалы входят в рецепт
        for (int k=0; k<input.size(); k++) {
            Item item = input.get(k);
            if (item==null) continue;
            // Берем код очередного материала из входящей очереди
            int materialID = item.getMaterial().getMaterialID();

            for (int i=0; i<operation.inputMaterials.length; i++) {
                // Если нашли такой же материал, то уменьшаем счетчик необходимых материалов

                if (operation.inputMaterials[i].getMaterialID()==materialID) {
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
        for (int i=0; i<operation.outputAmount.length; i++) {
            Material material = operation.outputMaterials[i];
            for (int j=0; j<operation.outputAmount[i]; j++) {
                item = new Item(material);
                item.setOwner(this);
                output.add(item);

                // Если приёмник буфер или конвейер - затолкать самостоятельно
                Block outputBlock = production.getBlockAt(this,outputDirection);
                if (outputBlock!=null) {
                    if (outputBlock instanceof Buffer
                            || outputBlock instanceof ExportBuffer
                            || outputBlock instanceof Conveyor) {
                        if (outputBlock.push(item)) output.remove(item);
                    }
                }

            }
        }
    }

    public void setState(int state) {
        if (this.state==state) return;
        switch (state) {
            case Block.IDLE:
                ((MachineRenderer) renderer).setIdleAnimation();
                this.state = state;
            case Block.BUSY:
                ((MachineRenderer) renderer).setBusyAnimation();
                this.state = state;

        }
    }


    public MachineType getType() {
        return type;
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
        input.clear();
        output.clear();
        cyclesLeft = 0;
        setState(IDLE);
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


    public JSONObject serialize() {
        JSONObject jsonObject = super.serialize();
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
