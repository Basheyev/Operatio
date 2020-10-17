package com.axiom.operatio.scenes.warehouse;

import android.graphics.Color;
import android.view.MotionEvent;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.GameScene;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.materials.Material;
import com.axiom.operatio.model.production.Production;
import com.axiom.operatio.model.warehouse.Warehouse;
import com.axiom.operatio.scenes.production.view.ItemWidget;
import com.axiom.operatio.scenes.production.view.ProductionSceneUI;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.DKGRAY;
import static android.graphics.Color.WHITE;


// TODO 1. Добавить сцену склад: хранение материалов и машин
// TODO 2. Добавить сцену склад: правила покупки и продажи со склада (симуляция рынка цен)
public class WarehouseScene extends GameScene {

    protected static boolean initialized = false;
    protected static int tickSound;
    protected static Sprite background;
    protected static ItemWidget[] itemWidget;

    @Override
    public String getSceneName() {
        return "Warehouse";
    }

    @Override
    public void startScene() {
        if (!initialized) buildUI();
        Warehouse warehouse = Warehouse.getInstance();
        for (int i=0; i< Material.getMaterialsAmount(); i++) {
            Material material = Material.getMaterial(i);
            int balance = warehouse.getBalance(material);
            itemWidget[i].setText("" + balance);
        }
    }

    @Override
    public void disposeScene() {

    }

    @Override
    public void updateScene(float deltaTime) {

    }

    @Override
    public void preRender(Camera camera) {
        background.zOrder = 0;
        background.draw(camera,camera.getMinX(),camera.getMinY(), Camera.WIDTH,Camera.HEIGHT);
    }

    @Override
    public void postRender(Camera camera) {

    }

    @Override
    public void onMotion(MotionEvent event, float worldX, float worldY) {

    }


    protected void buildUI() {
        background = new Sprite(SceneManager.getResources(), R.drawable.background);
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);

        ClickListener exitListener = new ClickListener() {
            @Override
            public void onClick(Widget w) {
                SoundRenderer.playSound(tickSound);
                SceneManager.getInstance().setActiveScene("Production");
            }
        };

        Widget widget = getSceneWidget();
        Button exitButton = new Button("Production");
        exitButton.setTextColor(1,1,1,1);
        exitButton.setLocalBounds(Camera.WIDTH - 375,960,375,100);
        exitButton.setColor(0.8f, 0.5f, 0.5f, 0.9f);
        exitButton.setClickListener(exitListener);
        widget.addChild(exitButton);


        Panel panel = new Panel();
        panel.setLocalBounds(50,50, Camera.WIDTH - 600, Camera.HEIGHT - 200);
        panel.setColor(0xCC505050);

        Warehouse warehouse = Warehouse.getInstance();
        itemWidget = new ItemWidget[Material.getMaterialsAmount()];
        float x = 30, y = 700;
        for (int i=0; i< Material.getMaterialsAmount(); i++) {
            Material material = Material.getMaterial(i);
            int balance = warehouse.getBalance(material);
            itemWidget[i] = new ItemWidget("" + balance);
            itemWidget[i].setColor(BLACK);
            itemWidget[i].setBackground(material.getImage());
            itemWidget[i].setTextScale(1);
            itemWidget[i].setTextColor(WHITE);
            itemWidget[i].setLocalBounds(x, y, 80, 80);
            panel.addChild(itemWidget[i]);
            x += 96;
            if (x + 96 > panel.getWidth()) {
                x = 30;
                y -= 96;
            }
        }
        widget.addChild(panel);

        initialized = true;

    }
}
