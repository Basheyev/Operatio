package com.basheyev.operatio.model.production.machine;

import com.basheyev.operatio.model.materials.Material;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Операция машины (рецепт)
 */
public class Operation {

    protected static ArrayList<Operation> allOperations = null;  // Список всех типов машин

    private MachineType machineType;          // Тип машины на которой выполняется
    private int ID;                           // Код операции
    private int cycles;                       // Время операции в циклах производства
    private Material[] outputs;               // Список кодов входящих материалов
    private Material[] inputs;                // Список кодов исходящих материалов
    private int[] outputAmount;               // Список количества входящих материалов
    private int[] inputAmount;                // Список количества исходящих материалов
    private double operationCost;             // Стоимость операции
    private double recipeCost;                // Стоимость рецепта (технологии)

    public Operation(MachineType type, JSONObject op) {
        if (allOperations ==null) allOperations = new ArrayList<>();
        try {
            machineType = type;
            ID = op.getInt("operationID");
            cycles = op.getInt("cycles");
            recipeCost = op.getInt("recipeCost");
            outputs = new Material[op.getInt("outputs")];
            inputs = new Material[op.getInt("inputs")];
            outputAmount = new int[outputs.length];
            inputAmount = new int[inputs.length];
            JSONArray jsonOutputIDs = op.getJSONArray("outputIDs");
            for (int j = 0; j < outputs.length; j++) outputs[j] = Material.getMaterial(jsonOutputIDs.getInt(j));
            JSONArray jsonInputIDs = op.getJSONArray("inputIDs");
            for (int j = 0; j < inputs.length; j++) inputs[j] = Material.getMaterial(jsonInputIDs.getInt(j));
            JSONArray jsonOutputAmount = op.getJSONArray("outputQuantities");
            for (int j = 0; j < outputAmount.length; j++) outputAmount[j] = jsonOutputAmount.getInt(j);
            JSONArray jsonInputAmount = op.getJSONArray("inputQuantities");
            for (int j = 0; j < inputAmount.length; j++)  inputAmount[j] = jsonInputAmount.getInt(j);
            operationCost = cycles * machineType.getCycleCost();
            allOperations.add(this);
        } catch (JSONException e) {
            e.printStackTrace();
            System.err.println(ID);
        }
    }


    public static Operation getOperation(int ID) {
        if (allOperations ==null) return null;
        for (int i = 0; i< allOperations.size(); i++) {
            Operation operation = allOperations.get(i);
            if (operation.getID()==ID) return operation;
        }
        return null;
    }

    public int getID() {
        return ID;
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

    /**
     * Возвращает стоимость выполнения операции
     * @return стоимость операции
     */
    public double getOperationCost() {
        return operationCost;
    }


    public double getRecipeCost() {
        return recipeCost;
    }

}
