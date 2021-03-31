package com.axiom.operatio.model.gameplay;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.data.JSONFile;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.operatio.model.production.Production;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Менеджер миссий
 */
public class MissionManager {

    private static GameMission[] missions = null;
    private static int levelCompletedSound = -1;

    public static GameMission getMission(int index) {
        if (missions==null) loadMissions();
        if (index < 0 || index >= missions.length) return null;
        return missions[index];
    }


    public static void process(Production production) {
        int currentMissionID = production.getCurrentMissionID();
        GameMission mission = MissionManager.getMission(currentMissionID);
        if (mission==null) return;
        if (mission.checkWinConditions(production)) {
            SoundRenderer.playSound(levelCompletedSound);
            mission.earnReward(production);
            if (currentMissionID + 1 <= MissionManager.size() - 1) {
                currentMissionID++;
                production.setCurrentMissionID(currentMissionID);
            }
        }
    }

    public static int size() {
        if (missions==null) loadMissions();
        return missions.length;
    }


    private static void loadMissions() {

        levelCompletedSound = SoundRenderer.loadSound(R.raw.yes_snd);

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
