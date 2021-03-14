package com.axiom.operatio.model.gameplay;

import com.axiom.atom.engine.data.Channel;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.machine.Machine;
import com.axiom.operatio.model.production.machine.MachineType;
import com.axiom.operatio.model.production.machine.Operation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Права доступные игроку
 */
public class GamePermissions {

    private ArrayList<Material> allowedMaterials;
    private ArrayList<MachineType> allowedMachines;
    private ArrayList<Operation> allowedOperations;

    //------------------------------------------------------------------------------------

    public GamePermissions() {
        allowedMachines = new ArrayList<>();
        allowedMaterials = new ArrayList<>();
        allowedOperations = new ArrayList<>();
    }


    public GamePermissions(JSONObject permissions) {
        try {
            JSONArray jsonMaterials = permissions.getJSONArray("allowedMaterials");
            JSONArray jsonMachines = permissions.getJSONArray("allowedMachines");
            JSONArray jsonOperation = permissions.getJSONArray("allowedOperations");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //------------------------------------------------------------------------------------

    public void clear() {
        allowedMaterials.clear();
        allowedMachines.clear();
        allowedOperations.clear();
    }

    //------------------------------------------------------------------------------------

    public boolean addMaterialPermission(Material material) {
        if (material==null) return false;
        if (allowedMaterials.contains(material)) return true;
        return allowedMaterials.add(material);
    }

    public boolean addMachinePermission(MachineType machine) {
        if (machine==null) return false;
        if (allowedMachines.contains(machine)) return true;
        return allowedMachines.add(machine);
    }


    public boolean addOperationPermission(Operation operation) {
        if (operation==null) return false;
        if (allowedOperations.contains(operation)) return true;
        return allowedOperations.add(operation);
    }

    //------------------------------------------------------------------------------------

    public boolean isAvailable(Material material) {
        if (material==null) return false;
        return allowedMaterials.contains(material);
    }

    public boolean isAvailable(MachineType machine) {
        if (machine==null) return false;
        return allowedMachines.contains(machine);
    }

    public boolean isAvailable(Operation operation) {
        if (operation==null) return false;
        return allowedOperations.contains(operation);
    }



}
