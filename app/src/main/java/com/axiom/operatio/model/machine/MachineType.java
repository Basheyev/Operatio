package com.axiom.operatio.model.machine;

import android.content.res.Resources;
import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.CSVTable;
import com.axiom.operatio.model.materials.Material;

import java.util.ArrayList;


/**
 * Описывает машины и доступные операции над материалами
 * (C) Bolat Basheyev 2020
 */
public class MachineType {

    //---------------------------------------------------------------------------------
    protected static ArrayList<MachineType> machineTypes;  // Список всех типов машин
    protected static boolean initialized = false;          // Флаг инициализации
    //---------------------------------------------------------------------------------
    protected int ID;                                      // Код машины
    protected String name;                                 // Название
    protected Operation[] operations;                      // Доступные операции

    //---------------------------------------------------------------------------------

    /**
     * Выдаёт экземпляр описания машины
     * @param ID код машины
     * @return экземпляр описания машины
     */
    public static MachineType getMachineType(int ID) {
        if (!initialized) loadMachinesData(SceneManager.getResources());
        return machineTypes.get(ID);
    }


    /**
     * Загружает данные о машинах и операциях (пока без защиты от некорректных данных)
     * @param resources ресурсы приложения
     */
    protected static void loadMachinesData(Resources resources) {
        CSVTable csv = new CSVTable(resources, R.raw.machines);
        machineTypes = new ArrayList<>(100);
        MachineType machineType;
        int total = csv.getRowCount();
        for (int row=0; row<total; row++) {
            machineType = new MachineType();
            machineType.ID = csv.getIntValue(row,0);
            machineType.name = csv.getValue(row,1).trim();
            machineType.operations = new Operation[csv.getIntValue(row,2)];
            Operation operation;
            try {
                for (int i = 0; i < machineType.operations.length; i++) {
                    row++;
                    int col = 0;
                    operation = new Operation();
                    operation.operationTime = Integer.parseInt(csv.getValue(row, 0));
                    col++;
                    operation.outputMaterials = new Material[csv.getIntValue(row, 1)];
                    col++;
                    operation.inputMaterials = new Material[csv.getIntValue(row, 2)];
                    col++;
                    operation.outputAmount = new int[operation.outputMaterials.length];
                    operation.inputAmount = new int[operation.inputMaterials.length];
                    for (int j = 0; j < operation.outputMaterials.length; j++) {
                        operation.outputMaterials[j] =
                                Material.getMaterial(csv.getIntValue(row, col + j));
                    }
                    col += operation.outputMaterials.length;
                    for (int j = 0; j < operation.inputMaterials.length; j++) {
                        operation.inputMaterials[j] =
                                Material.getMaterial(csv.getIntValue(row, col + j));
                    }
                    col += operation.inputMaterials.length;
                    for (int j = 0; j < operation.outputAmount.length; j++) {
                        operation.outputAmount[j] = csv.getIntValue(row, col + j);
                    }
                    col += operation.outputAmount.length;
                    for (int j = 0; j < operation.inputAmount.length; j++) {
                        operation.inputAmount[j] = csv.getIntValue(row, col + j);
                    }
                    machineType.operations[i] = operation;
                }
                machineTypes.add(machineType);
            } catch (Exception e) {
                Log.e("Machines Loader", " Wrong data format at row " + row);
                e.printStackTrace();
            }
        }
        initialized = true;
    }


    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public Operation[] getOperations() {
        return operations;
    }

    public boolean isOperationAvailable(Operation op) {
        int size = operations.length;
        for (int i=0; i<size; i++) {
            if (operations[i].equals(op)) return true;
        }
        return false;
    }

    //--------------------------------------------------------------------------------

    /**
     * Отладочный метод распечатки списка описания машин
     */
    public static void printDebug() {
        MachineType m;
        Operation op;
        for (int i=0; i<machineTypes.size();i++) {
            m = machineTypes.get(i);
            System.out.println("Machine:" + m.name);
            for (int j=0; j<m.operations.length; j++) {
                op = m.operations[j];
                System.out.print (op.outputMaterials[0].getName() + " = ");
                for (int k=0; k<op.inputMaterials.length; k++) {
                    System.out.print (op.inputMaterials[k].getName() +
                            " x " + op.inputAmount[k] + " ");
                }
                System.out.println();
            }
        }
    }



}
