package com.axiom.operatio.production.machines;

import com.axiom.operatio.production.blocks.Block;
import com.axiom.operatio.production.Production;
import com.axiom.operatio.production.materials.Item;
import com.axiom.operatio.production.materials.Material;

public class Machine extends Block {

    protected MachineType type;
    protected Operation operation;
    private int[] matCounter;
    private int cyclesLeft;

    public Machine(Production production, MachineType type, Operation op, int inDir, int outDir) {
        super(production, inDir, op.totalInputAmount(), outDir, op.totalOutputAmount());
        this.type = type;
        this.operation = op;
        this.matCounter = new int[operation.inputMaterials.length];
    }

    @Override
    public boolean push(Item item) {
        // Проверяем допустимый ли материал для добавления в очередь ввода
        if (!operation.isCorrectInput(item.getMaterial())) return false;
        return super.push(item);
    }

    @Override
    public boolean process() {

        // Если машина работает, уменьшаем счетчик оставшегося времени
        if (state==BUSY && cyclesLeft > 0) {
            cyclesLeft--;
            if (cyclesLeft==0) {     // Если время операции прошло
                input.clear();       // Удаляем входящие предметы
                generateOutput();    // Генерируем выходные предметы
                state = IDLE;        // Устанавливаем состояние IDLE
            }
            return true;
        }

        // Если количество предметов во вхоядщий очереди меньше чем необходимо для операции
        // пытаемся самостоятельно взять из направления входа (блок)
        int totalAmount = operation.totalInputAmount();
        if (input.size() < totalAmount) return getItemFromInputDirection();


        // Подтверждаем, что есть необходимое количество каждого предмета по Операции
        if (operationInputVerified()) {              // Начинаем работу машины
            state = BUSY;                            // Устанавливаем состояние - BUSY
            cyclesLeft = operation.operationTime;    // Указываем количество циклов работы
            return true;
        }

        return false;
    }


    /**
     * Забирает один предмет из блока по направлению входа
     * @return true - если получилось забрать, false - если нет
     */
    protected boolean getItemFromInputDirection() {
        Block inputBlock = production.getBlockAt(this, inputDirection);
        if (inputBlock==null) return false;  // Если на входящем направление ничего нет
        Item item = inputBlock.peek();       // Пытаемся взять предмет из блока входа
        if (item==null) return false;        // Если ничего нет возвращаем false
        if (!push(item)) return false;       // Если не получилось добавить к себе - false
        inputBlock.poll();                   // Если получилось - удаляем из блока входа
        return true;
    }


    /**
     * Проверяет, есть ли все предметы для получения выходного предмета (по операции)
     * @return true - если есть, false - если нет
     */
    protected boolean operationInputVerified() {
        // Копируем количество необходимого входного материала из описания операции в matCounter
        System.arraycopy(operation.inputAmount, 0, matCounter,0, matCounter.length);
        // Алгоритм построен с учтом того, что все входящие материалы входят в рецепт
        for (Item item:input) {
            // Берем код очередного материала из входящей очереди
            int materialID = item.getMaterial().materialID;
            for (int i=0; i<operation.inputMaterials.length; i++) {
                // Если нашли такой же материал, то уменьшаем счетчик необходимых материалов
                if (operation.inputMaterials[i].materialID==materialID) {
                    matCounter[i]--;
                    break;
                }
            }
        }
        // Если все необходимые материалы есть, то каждый элемент matCounter будет равен 0
        for (int i=0; i<matCounter.length; i++) {
            if (matCounter[i] != 0) return false;
        }
        // Все необходимые материалы есть
        return true;
    }


    // Добавляем на выход исходящие предметы
    protected boolean generateOutput() {
        Item item;
        for (int i=0; i<operation.outputAmount.length; i++) {
            Material material = operation.outputMaterials[i];
            for (int j=0; j<operation.outputAmount[i]; j++) {
                item = new Item(material);
                item.setOwner(this, Production.getCurrentCycle());
                output.add(item);
            }
        }
        return true;
    }



}
