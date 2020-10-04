package com.axiom.operatio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.axiom.atom.engine.core.GameView;
import com.axiom.operatio.scenes.mainmenu.MainMenuScene;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = GameView.getInstance(this, new MainMenuScene());
        setContentView(gameView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.onPause();
        // TODO Нужно ли останавливать игровой цикл?
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
}