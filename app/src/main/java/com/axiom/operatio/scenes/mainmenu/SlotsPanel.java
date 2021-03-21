package com.axiom.operatio.scenes.mainmenu;

import android.graphics.Color;
import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.gameplay.GameSaveLoad;
import com.axiom.operatio.scenes.production.ProductionScene;

public class SlotsPanel extends Panel {

    public final int panelColor = 0xCC505050;
    protected MainMenuScene mainMenuScene;
    protected MenuPanel menuPanel;
    protected int tickSound;


    public SlotsPanel(MainMenuScene menuScene) {
        super();
        this.mainMenuScene = menuScene;
        this.menuPanel = mainMenuScene.getMenuPanel();
        setLocalBounds(menuPanel.getX() + menuPanel.getWidth() + 50, Camera.HEIGHT/2 - 400,600,800);
        setColor(panelColor);
        buildButtons();
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
    }


    private void buildButtons() {
        Button button;
        String caption;

        for (int i =1; i<6; i++) {
            caption = "SLOT " + (i-1);
            button = new Button(caption);
            button.setTag(""+i);
            button.setLocalBounds(50, 750 - ( i * 140), 500, 100);
            button.setColor(Color.GRAY);
            button.setTextColor(1,1,1,1);
            button.setTextScale(1.8f);
            button.setClickListener(listener);
            this.addChild(button);
        }

    }


    protected ClickListener listener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            int choice = Integer.parseInt(w.getTag());
            SoundRenderer.playSound(tickSound);
            Log.i("SLOT", "" + choice);
        }
    };

}
