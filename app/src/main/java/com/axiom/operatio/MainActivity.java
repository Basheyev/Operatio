package com.axiom.operatio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.axiom.atom.engine.core.GameView;
import com.axiom.atom.tests.demotest.DemoScene;
import com.axiom.atom.tests.shoottest.ShooterScene;
import com.axiom.atom.tests.spritetest.SpriteScene;
import com.axiom.operatio.scenes.mainmenu.MainMenuScene;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;
    private static MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        gameView = GameView.getInstance(this, new MainMenuScene());
        setContentView(gameView);
    }

    @Override
    protected void onPause() {
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