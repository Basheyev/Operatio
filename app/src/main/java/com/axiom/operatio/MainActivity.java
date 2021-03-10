package com.axiom.operatio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.GameView;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.scenes.inventory.InventoryScene;
import com.axiom.operatio.scenes.mainmenu.MainMenuScene;
import com.axiom.operatio.scenes.production.ProductionScene;
import com.axiom.operatio.scenes.report.ReportScene;
import com.axiom.operatio.scenes.technology.TechnologyScene;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;
    private GameScene gameScene;
    private static MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        gameScene = new MainMenuScene();
        gameView = GameView.getInstance(this, gameScene);
        setContentView(gameView);
    }

    @Override
    public void onBackPressed() {
        SceneManager sceneManager = SceneManager.getInstance();
        GameScene scene = sceneManager.getActiveScene();
        String sceneName = scene.getSceneName();

        switch (sceneName) {
            case ProductionScene.SCENE_NAME:
                ((ProductionScene) scene).getProduction().setPaused(true);
                sceneManager.setActiveScene(MainMenuScene.SCENE_NAME);
                break;
            case InventoryScene.SCENE_NAME:
                sceneManager.setActiveScene(ProductionScene.SCENE_NAME);
                break;
            case TechnologyScene.SCENE_NAME:
                sceneManager.setActiveScene(ProductionScene.SCENE_NAME);
                break;
            case ReportScene.SCENE_NAME:
                sceneManager.setActiveScene(ProductionScene.SCENE_NAME);
                break;
            case MainMenuScene.SCENE_NAME:
                ((MainMenuScene) scene).getMenuPanel().exitGame();
                break;
        }
    }


    @Override
    protected void onPause() {
        SceneManager sceneManager = SceneManager.getInstance();
        GameScene scene = sceneManager.getScene(ProductionScene.SCENE_NAME);
        if (scene != null) {
            ProductionScene ps = ((ProductionScene) scene);
            ps.getInputHandler().invalidateAllActions();
            ps.getProduction().setPaused(true);
        }
        super.onPause();
        gameView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static MainActivity getActivity() {
        return activity;
    }
}