package com.axiom.operatio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.axiom.atom.engine.core.GameView;
import com.axiom.atom.engine.tests.demotest.DemoScene;
import com.axiom.atom.engine.tests.phystest.PhysScene;
import com.axiom.atom.engine.tests.shoottest.ShooterScene;
import com.axiom.atom.engine.tests.spritetest.SpriteScene;
import com.axiom.operatio.scenes.ProductionScene;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this, new ProductionScene());
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
}