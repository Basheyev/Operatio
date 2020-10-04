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




    public int totalInputAmount() {
        int total = 0;
        for (int i=0; i < inputAmount.length; i++) {
            total += inputAmount[i];
        }
        return total;
    }


    public int totalOutputAmount() {
        int total = 0;
        for (int i=0; i < outputAmount.length; i++) {
            total += outputAmount[i];
        }
        return total;
    }

}
