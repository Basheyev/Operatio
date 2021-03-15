package com.axiom.operatio.model.gameplay;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.JSONFile;

import org.json.JSONArray;
import org.json.JSONException;

public class GameManager {

    private static GameMission[] missions = null;


    public static GameMission getMission(int index) {
        if (missions==null) loadMissions();
        if (index < 0 || index >= missions.length) return null;
        return missions[index];
    }


    public static int size() {
        if (missions==null) loadMissions();
        return missions.length;
    }


    private static void loadMissions() {
        JSONArray jsonMissions = JSONFile.loadArray(SceneManager.getResources(), R.raw.missions);
        missions = new GameMission[jsonMissions.length()];
        try {
            for (int i = 0; i < jsonMissions.length(); i++) {
                missions[i] = new GameMission(jsonMissions.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
