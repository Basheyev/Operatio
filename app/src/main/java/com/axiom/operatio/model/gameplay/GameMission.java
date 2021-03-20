package com.axiom.operatio.model.gameplay;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.JSONFile;
import com.axiom.operatio.model.production.Production;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class GameMission {

    private int ID;
    private String name;
    private String description;
    private int[] prerequisiteIDs;
    private GameCondition[] winConditions;
    private GamePermissions permissionsReward;
    private double moneyReward;
    protected boolean completed;


    protected GameMission(JSONObject mission) {
        try {
            ID = mission.getInt("ID");
            name = mission.getString("name");
            description = mission.getString("description");
            prerequisiteIDs = parseIntArray(mission.getJSONArray("prerequisiteIDs"));
            winConditions = parseConditionsArray(mission.getJSONArray("winConditions"));
            permissionsReward = new GamePermissions(mission.getJSONObject("permissionsReward"));
            moneyReward = mission.getDouble("moneyReward");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private int[] parseIntArray(JSONArray jsonIntArray) throws JSONException {
        int[] array = new int[jsonIntArray.length()];
        for (int i=0; i<jsonIntArray.length(); i++) {
            array[i] = jsonIntArray.getInt(i);
        }
        return array;
    }


    private GameCondition[] parseConditionsArray(JSONArray jsonConditions) throws JSONException {
        GameCondition[] array = new GameCondition[jsonConditions.length()];
        for (int i=0; i<jsonConditions.length(); i++) {
            array[i] = new GameCondition(jsonConditions.getJSONObject(i));
        }
        return array;
    }


    public boolean checkWinConditions(Production production) {
        // Если условий победы нет - выигрыша нет
        if (winConditions==null) return false;
        // Если не выполнилось хоть одно условия - выигрыша нет
        for (GameCondition winCondition : winConditions) {
            if (!winCondition.check(production.getLedger())) return false;
        }
        // Иначе выигрыш
        return true;
    }


    public void earnReward(Production production) {
        production.getPermissions().addPermissions(permissionsReward);
        production.increaseCashBalance(0, moneyReward);
    }


    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int[] getPrerequisiteIDs() {
        return prerequisiteIDs;
    }

    public GameCondition[] getWinConditions() {
        return winConditions;
    }

    public GamePermissions getPermissionsReward() {
        return permissionsReward;
    }

    public double getMoneyReward() {
        return moneyReward;
    }

    public boolean isCompleted() {
        return completed;
    }
}
