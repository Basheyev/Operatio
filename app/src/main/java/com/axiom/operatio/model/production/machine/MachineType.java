package com.axiom.operatio.model.production.machine;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.JSONFileLoader;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.operatio.model.materials.Material;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    protected Sprite image;                                // Изображение машины
    protected double price;
    //---------------------------------------------------------------------------------

    /**
     * Выдаёт экземпляр описания машины
     * @param ID код машины
     * @return экземпляр описания машины
     */
    public static MachineType getMachineType(int ID) {
        if (!initialized) initialize();
        return machineTypes.get(ID);
    }


    public static int getMachineTypesCount() {
        if (!initialized) initialize();
        return machineTypes.size();
    }


    private static void initialize() {
        loadMachinesData(SceneManager.getResources());
        loadMachineSprites();
        initialized = true;
    }


    private static void loadMachinesData(Resources resources) {
        JSONFileLoader fileLoader = new JSONFileLoader(resources, R.raw.machines);
        try {
            JSONArray jsonMachines = new JSONArray(fileLoader.getJsonFile());
            int machinesCount = jsonMachines.length();
            machineTypes = new ArrayList<>(machinesCount);
            MachineType machineType;
            for (int i=0; i<machinesCount; i++) {
                JSONObject jsonMachineType = jsonMachines.getJSONObject(i);
                machineType = new MachineType();
                machineType.ID = jsonMachineType.getInt("ID");
                machineType.name = jsonMachineType.getString("name");
                int operationsCount = jsonMachineType.getInt("operationsCount");
                machineType.operations = new Operation[operationsCount];
                machineType.price = jsonMachineType.getInt("price");
                JSONArray jsonOperations = jsonMachineType.getJSONArray("operations");
                for (int j=0; j<operationsCount; j++) {
                    JSONObject jsonOperation = jsonOperations.getJSONObject(j);
                    machineType.operations[j] = loadOperationData(machineType, jsonOperation);
                }
                machineTypes.add(machineType);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private static Operation loadOperationData(MachineType machineType, JSONObject op) throws JSONException {
        Operation operation = new Operation(machineType);
        operation.cycles = op.getInt("cycles");
        operation.outputs = new Material[op.getInt("outputs")];
        operation.inputs = new Material[op.getInt("inputs")];
        operation.outputAmount = new int[operation.outputs.length];
        operation.inputAmount = new int[operation.inputs.length];
        JSONArray jsonOutputIDs = op.getJSONArray("outputIDs");
        for (int j = 0; j < operation.outputs.length; j++)
            operation.outputs[j] = Material.getMaterial(jsonOutputIDs.getInt(j));
        JSONArray jsonInputIDs = op.getJSONArray("inputIDs");
        for (int j = 0; j < operation.inputs.length; j++)
            operation.inputs[j] = Material.getMaterial(jsonInputIDs.getInt(j));
        JSONArray jsonOutputAmount = op.getJSONArray("outputQuantities");
        for (int j = 0; j < operation.outputAmount.length; j++)
            operation.outputAmount[j] = jsonOutputAmount.getInt(j);
        JSONArray jsonInputAmount = op.getJSONArray("inputQuantities");
        for (int j = 0; j < operation.inputAmount.length; j++)
            operation.inputAmount[j] = jsonInputAmount.getInt(j);;
        return operation;
    }


    private static void loadMachineSprites() {
        Sprite allMachines = new Sprite(SceneManager.getResources(), R.drawable.blocks, 8, 11);
        for (int i=0; i < machineTypes.size(); i++) {
            Sprite image = allMachines.getAsSprite(i * 8, i * 8 + 7);
            int animation = image.addAnimation(0, 7, 8, true);
            image.setActiveAnimation(animation);
            machineTypes.get(i).image = image;
        }
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

    public Operation getOperation(int ID) {
        if (ID < 0 || ID >= operations.length) return null;
        return operations[ID];
    }

    public int getOperationID(Operation op) {
        int size = operations.length;
        for (int i=0; i<size; i++) {
            if (operations[i].equals(op)) return i;
        }
        return -1;
    }

    public Sprite getImage() {
        return image;
    }

    public double getPrice() { return price; }

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
                System.out.print (op.outputs[0].getName() + " = ");
                for (int k = 0; k<op.inputs.length; k++) {
                    System.out.print (op.inputs[k].getName() +
                            " x " + op.inputAmount[k] + " ");
                }
                System.out.println();
            }
        }
    }



}
