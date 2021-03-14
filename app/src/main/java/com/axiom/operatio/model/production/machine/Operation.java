package com.axiom.operatio.model.production.machine;

import com.axiom.operatio.model.materials.Material;

// todo add op cost
public class Operation {

    protected MachineType machineType;
    protected int cycles;                // Время операции в циклах производства
    protected Material[] outputs;       // Список кодов входящих материалов
    protected Material[] inputs;        // Список кодов исходящих материалов
    protected int[] outputAmount;               // Список количества входящих материалов
    protected int[] inputAmount;                // Список количества исходящих материалов
    protected long cost;                        // Стоимость операции


    public Operation(MachineType type) {
        machineType = type;
    }


    public boolean isCorrectInput(Material m) {
        for (Material material: inputs) {
            if (material.equals(m)) return true;
        }
        return false;
    }


    /**
     * Общее количество необходимых материалов на вход (всех типов)
     * @return количество необходимых материалов на вход (всех типов)
     */
    public int totalInputAmount() {
        int total = 0;
        for (int i=0; i < inputAmount.length; i++) {
            total += inputAmount[i];
        }
        return total;
    }


    /**
     * Общее количество материалов на выходе (всех типов)
     * @return количество материалов на выходе (всех типов)
     */
    public int totalOutputAmount() {
        int total = 0;
        for (int i=0; i < outputAmount.length; i++) {
            total += outputAmount[i];
        }
        return total;
    }

    /**
     * Возвращает время выполнения операции в циклах производства
     * @return Время выполнения операции в циклах производства
     */
    public int getCycles() {
        return cycles;
    }

    /**
     * Возвращает список типов материалов на выходе
     * @return список материалов на выходе
     */
    public Material[] getOutputs() {
        return outputs;
    }

    /**
     * Возвращает список типов материалов на входе
     * @return список материалов на входе
     */
    public Material[] getInputs() {
        return inputs;
    }

    /**
     * Возвращает списоком количество материалов каждого типа на выходе
     * @return список количество материалов каждого типа на выходе
     */
    public int[] getOutputAmount() {
        return outputAmount;
    }

    /**
     * Возвращает списоком количество материалов каждого типа на входе
     * @return список количество материалов каждого типа на входе
     */
    public int[] getInputAmount() {
        return inputAmount;
    }

    /**
     * Возвращает тип машины которая выполняет эту операцию
     * @return тип машины
     */
    public MachineType getMachineType() {
        return machineType;
    }

}
