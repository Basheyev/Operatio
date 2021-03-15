package com.axiom.operatio.model.production.machine;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.JSONFile;
import com.axiom.atom.engine.graphics.renderers.Sprite;

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
    protected double price;                                // Стоимость машины
    protected double cycleCost;                            // Стоимость цикла работы
    //---------------------------------------------------------------------------------

    private MachineType(JSONObject jsonMachineType) {
        try {
            ID = jsonMachineType.getInt("ID");
            name = jsonMachineType.getString("name");
            int operationsCount = jsonMachineType.getInt("operationsCount");
            operations = new Operation[operationsCount];
            price = jsonMachineType.getInt("price");
            cycleCost = jsonMachineType.getDouble("cycleCost");
            JSONArray jsonOperations = jsonMachineType.getJSONArray("operations");
            for (int j = 0; j < operationsCount; j++) {
                JSONObject jsonOperation = jsonOperations.getJSONObject(j);
                operations[j] = new Operation(this, jsonOperation);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


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
        try {
            JSONArray jsonMachines = JSONFile.loadArray(resources, R.raw.machines);
            int machinesCount = jsonMachines.length();
            machineTypes = new ArrayList<>(machinesCount);
            MachineType machineType;
            for (int i=0; i<machinesCount; i++) {
                JSONObject jsonMachineType = jsonMachines.getJSONObject(i);
                machineType = new MachineType(jsonMachineType);
                machineTypes.add(machineType);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

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


    public double getPrice() {
        return price;
    }


    public double getCycleCost() {
        return cycleCost;
    }
}
