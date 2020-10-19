package com.axiom.operatio.model.production;


import com.axiom.operatio.model.materials.Item;
import com.axiom.operatio.model.production.block.Block;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// TODO Сделать сериализацию
// TODO Сделать десериализацию
public class ProductionSaveLoad {


    public String serialize(Production production) {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("columns", production.columns);
            jsonObject.put("rows", production.rows);
            jsonObject.put("lastCycleTime", production.lastCycleTime);
            jsonObject.put("cycleMilliseconds", Production.cycleMilliseconds);
            jsonObject.put("clock", Production.clock);
            jsonObject.put("cycle", production.cycle);
            jsonObject.put("isPaused", production.isPaused);
            jsonObject.put("pauseStart", production.pauseStart);
            jsonObject.put("pausedTime", production.pausedTime);
            jsonObject.put("blockSelected", production.blockSelected);
            jsonObject.put("selectedCol", production.selectedCol);
            jsonObject.put("selectedRow", production.selectedRow);

            JSONArray jsonArray = new JSONArray();
            for (int i=0; i<production.blocks.size(); i++) {
                JSONObject jsonBlock = serializeBlock(production.blocks.get(i));
                jsonArray.put(jsonBlock);
            }
            jsonObject.put("blocks", jsonArray);

            /*
                protected static Inventory inventory;           // Синглтон объекта - склад
                protected static Market market;                 // Синллтон объекта - рынок
            */
            return jsonObject.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }



        return "";
    }


    protected JSONObject serializeBlock(Block block) {
        /*
            protected int state = IDLE;                       // Текущее состояние блока
            protected int inputDirection, outputDirection;    // Направление ввода и вывода
            protected int inputCapacity, outputCapacity;      // Максимальая вместимость блока в предметах
            protected Channel<Item> input;                    // Буферы ввода предметов
            protected Channel<Item> output;                   // Буферы вывода предметов
            public int column, row;                           // Координаты блока в сетке карты
         */

        return null;
    }


    protected JSONObject serializeItem(Item item) {
        return null;
    }

    //------------------------------------------------------------------------------------------
    //
    //------------------------------------------------------------------------------------------


    public Production deserialize(String str) {

        return null;
    }

}
