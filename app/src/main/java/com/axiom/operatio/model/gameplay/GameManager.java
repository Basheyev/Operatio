package com.axiom.operatio.model.gameplay;

import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.mainmenu.MainMenuScene;
import com.axiom.operatio.scenes.production.ProductionScene;

public class GameManager {


    public GameManager() {

    }

    public ProductionScene newGame() {
        return null;
    }


    public ProductionScene loadGame() {
        return null;
    }


    public boolean saveGame(Production production) {


        return false;
    }


    private void clearGameScenes() {
        SceneManager sceneManager = SceneManager.getInstance();
        GameScene activeScene = sceneManager.getActiveScene();
        if (!activeScene.getSceneName().equals(MainMenuScene.SCENE_NAME)) {
            sceneManager.setActiveScene(MainMenuScene.SCENE_NAME);
        }
        sceneManager.removeGameScene(ProductionScene.SCENE_NAME);
    }




}
