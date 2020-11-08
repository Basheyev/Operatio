package com.axiom.operatio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.GameView;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.operatio.scenes.mainmenu.MainMenuScene;
import com.axiom.operatio.scenes.production.ProductionScene;

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
            case "Inventory":
                sceneManager.setActiveScene("Production");
                break;
            case "Production":
                sceneManager.setActiveScene("Menu");
                break;
            case "Menu":
                ((MainMenuScene) scene).getMenuPanel().exitGame();
                break;
        }
    }

    @Override
    protected void onPause() {
        SceneManager sceneManager = SceneManager.getInstance();
        GameScene scene = sceneManager.getActiveScene();
        String sceneName = scene.getSceneName();
        if (sceneName.equals("Production")) {
            ((ProductionScene) scene).pause();
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