package com.axiom.operatio.model.machine;

import com.axiom.operatio.model.materials.Material;

public class Operation {

    protected int operationTime;                // Время операции в циклах производства
    protected Material[] outputMaterials;       // Список кодов входящих материалов
    protected Material[] inputMaterials;        // Список кодов исходящих материалов
    protected int[] outputAmount;               // Список количества входящих материалов
    protected int[] inputAmount;                // Список количества исходящих материалов


    public boolean isCorrectInput(Material m) {
        for (Material material: inputMaterials) {
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
    public int getOperationTime() {
        return operationTime;
    }

    /**
     * Возвращает список типов материалов на выходе
     * @return список материалов на выходе
     */
    public Material[] getOutputMaterials() {
        return outputMaterials;
    }

    /**
     * Возвращает список типов материалов на входе
     * @return список материалов на входе
     */
    public Material[] getInputMaterials() {
        return inputMaterials;
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

}
