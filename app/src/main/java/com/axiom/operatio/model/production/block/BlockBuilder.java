package com.axiom.operatio.model.production.block;

import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.production.buffer.Buffer;
import com.axiom.operatio.model.production.buffer.ExportBuffer;
import com.axiom.operatio.model.production.buffer.ImportBuffer;
import com.axiom.operatio.model.production.conveyor.Conveyor;
import com.axiom.operatio.model.production.machine.Machine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BlockBuilder {

    /**
     * Десериализует блок из JSON в объект
     * @param production производство
     * @param jsonObject JSON объект
     * @return блок
     */
    public static Block deserialize(Production production, JSONObject jsonObject) {
        Block block = null;
        try {
            String type = jsonObject.getString("class");
            int inpDir = jsonObject.getInt("inputDirection");
            int outDir = jsonObject.getInt("outputDirection");
            int inpCap = jsonObject.getInt("inputCapacity");
            int outCap = jsonObject.getInt("outputCapacity");
            switch (type) {
                case "Buffer": block = new Buffer(production, jsonObject, inpCap); break;
                case "Conveyor": block = new Conveyor(production, jsonObject, inpDir, outDir); break;
                case "Machine": block = new Machine(production, jsonObject, inpDir, inpCap, outDir, outCap); break;
                case "ImportBuffer": block = new ImportBuffer(production, jsonObject); break;
                case "ExportBuffer": block = new ExportBuffer(production, jsonObject); break;
                default: throw new JSONException("Unknown block");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return block;
    }


    /**
     * Десериализует общие поля блока из JSON в указанный блок
     * @param block целевой объект
     * @param jsonObject JSON объект
     */
    public static void parseCommonFields(Block block, JSONObject jsonObject) {
        try {
            block.ID = jsonObject.getLong("ID");
            block.state = jsonObject.getInt("state");
            block.inputDirection = jsonObject.getInt("inputDirection");
            block.outputDirection = jsonObject.getInt("outputDirection");
            block.inputCapacity = jsonObject.getInt("inputCapacity");
            block.outputCapacity = jsonObject.getInt("outputCapacity");
            block.column = jsonObject.getInt("column");
            block.row = jsonObject.getInt("row");

            JSONArray jsonInputArray = jsonObject.getJSONArray("input");
            for (int i = 0; i < jsonInputArray.length(); i++) {
                JSONObject jsonItem = (JSONObject) jsonInputArray.get(i);
                Item item = Item.deserialize(jsonItem, block);
                block.input.add(item);
            }

            JSONArray jsonOutputArray = jsonObject.getJSONArray("output");
            for (int i = 0; i < jsonOutputArray.length(); i++) {
                JSONObject jsonItem = (JSONObject) jsonOutputArray.get(i);
                Item item = Item.deserialize(jsonItem, block);
                block.output.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
