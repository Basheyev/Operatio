package com.axiom.operatio.model.gameplay;

import android.content.Context;
import android.content.SharedPreferences;

import com.axiom.atom.engine.core.SceneManager;
import com.axiom.operatio.MainActivity;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.production.ProductionScene;

import org.json.JSONException;
import org.json.JSONObject;

public class GameSaveLoad {

    public static final int MAX_SLOTS = 5;



    public synchronized void continueGame() {
        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.setActiveScene(ProductionScene.SCENE_NAME);
    }

    public synchronized ProductionScene newGame() {
        ProductionScene scene = new ProductionScene();
        startScene(scene);
        return scene;
    }


    public synchronized ProductionScene loadGame(int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) return null;
        String slotName = "SLOT" + slot;
        SharedPreferences sharedPref = getPreferences();
        String loadedGame = sharedPref.getString(slotName, null);
        if (loadedGame==null) return null;
        try {
            ProductionScene scene = new ProductionScene(new JSONObject(loadedGame));
            startScene(scene);
            return scene;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public synchronized boolean saveGame(int slot, ProductionScene productionScene) {
        if (slot < 0 || slot >= MAX_SLOTS || productionScene==null) return false;
        String slotName = "SLOT" + slot;
        SharedPreferences sharedPref = getPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        Production production = productionScene.getProduction();
        if (!production.isPaused()) production.setPaused(true);
        String savedGame = production.toJSON().toString();
        editor.remove(slotName);
        editor.putString(slotName, savedGame);
        return editor.commit();
    }


    private void startScene(ProductionScene scene) {
        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.removeGameScene(ProductionScene.SCENE_NAME);
        sceneManager.addGameScene(scene);
        sceneManager.setActiveScene(ProductionScene.SCENE_NAME);
    }


    private SharedPreferences getPreferences() {
        MainActivity mainActivity = MainActivity.getActivity();
        return mainActivity.getPreferences(Context.MODE_PRIVATE);
    }


}
