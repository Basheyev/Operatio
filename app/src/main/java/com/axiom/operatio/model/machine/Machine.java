package com.axiom.operatio.model.machine;

import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.model.Production;
import com.axiom.operatio.model.buffer.Buffer;
import com.axiom.operatio.model.conveyor.ConveyorRenderer;
import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.conveyor.Conveyor;

// TODO Добавить для Сборщика возможность забирать нужные материалы из буфера
// TODO Добавить сортировщик (уточнить нужен ли он)
public class Machine extends Block {

    protected MachineType type;
    protected Operation operation;

    private int[] matCounter;
    private int cyclesLeft = 0;

    public Machine(Production production, MachineType type, Operation op, int inDir, int outDir) {
        super(production, inDir, op.totalInputAmount(), outDir, op.totalOutputAmount());
        this.type = type;
        this.operation = op;
        this.matCounter = new int[operation.inputMaterials.length];
        this.renderer = new MachineRenderer(this);
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
            getItemFromInputDirection();
            return;
        }

        // Подтверждаем, что есть необходимое количество каждого предмета по Операции
        if (operationInputVerified()) {              // Начинаем работу машины
            setState(BUSY);                          // Устанавливаем состояние - BUSY
            cyclesLeft = operation.operationTime;    // Указываем количество циклов работы
        }

    }




    /**
     * Проверяет, есть ли все предметы для получения выходного предмета (по операции)
     * @return true - если есть, false - если нет
     */
    protected boolean operationInputVerified() {
        // Копируем количество необходимого входного материала из описания операции в matCounter
        System.arraycopy(operation.inputAmount, 0, matCounter,0, matCounter.length);
        // Алгоритм построен с учтом того, что все входящие материалы входят в рецепт
        //for (Item item:input) {
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
                    if (outputBlock instanceof Buffer || outputBlock instanceof Conveyor) {
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
}
