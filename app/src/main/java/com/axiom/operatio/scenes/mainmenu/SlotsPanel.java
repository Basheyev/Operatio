package com.axiom.operatio.scenes.mainmenu;

import android.graphics.Color;
import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.gameplay.GameSaveLoad;
import com.axiom.operatio.scenes.production.ProductionScene;

import static android.graphics.Color.DKGRAY;
import static android.graphics.Color.WHITE;

/**
 * Панель отображения слотов сохраения/загрузки
 */
public class SlotsPanel extends Panel {

    public static final int MODE_LOAD_GAME = 0;
    public static final int MODE_SAVE_GAME = 1;
    private static final String LOAD_GAME = "LOAD GAME";
    private static final String SAVE_GAME = "SAVE GAME";

    public static final int PANEL_COLOR = 0xCC505050;
    public static final int LOAD_GAME_BACKGROUND = 0xBC2fe682;
    public static final int SAVE_GAME_BACKGROUND = 0xBCe6bb2f;

    protected Caption header;
    protected MainMenuScene mainMenuScene;
    protected MenuPanel menuPanel;
    protected int tickSound;
    protected int mode;
    protected Button[] slotButtons;


    public SlotsPanel(MainMenuScene menuScene) {
        super();
        this.mainMenuScene = menuScene;
        this.menuPanel = mainMenuScene.getMenuPanel();
        setLocalBounds(menuPanel.getX() + menuPanel.getWidth() + 50, 50,600,800);
        setColor(PANEL_COLOR);
        buildButtons();
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
        setMode(MODE_LOAD_GAME);
    }


    private void buildButtons() {
        Button button;

        header = new Caption("");
        header.setLocalBounds(50, 730, 500, 50);
        header.setTextScale(1.5f);
        header.setTextColor(WHITE);
        addChild(header);

        slotButtons = new Button[GameSaveLoad.MAX_SLOTS];

        for (int i =0; i < GameSaveLoad.MAX_SLOTS; i++) {
            button = new Button();
            button.setHorizontalAlignment(Text.ALIGN_LEFT);
            button.setTag("" + i);
            button.setLocalBounds(50, 750 - ((i + 1) * 140), 500, 100);
            button.setColor(Color.GRAY);
            button.setTextColor(1,1,1,1);
            button.setTextScale(1.8f);
            button.setClickListener(listener);
            addChild(button);
            slotButtons[i] = button;
        }

    }

    public void setMode(int mode) {
        this.mode = mode;
        GameSaveLoad gsl = mainMenuScene.getGameSaveLoad();
        String[] savedGamesCaptions = gsl.getGamesCaptions();

        if (mode==MODE_LOAD_GAME) {
            header.setText(LOAD_GAME);
            setColor(PANEL_COLOR);
            for (int i=0; i<savedGamesCaptions.length; i++) {
                if (savedGamesCaptions[i]!=null) {
                    slotButtons[i].visible = true;
                    slotButtons[i].setColor(LOAD_GAME_BACKGROUND);
                    if (i==0) {
                        slotButtons[i].setText("AUTO SAVED");
                    } else slotButtons[i].setText(savedGamesCaptions[i]);
                }
                else slotButtons[i].visible = false;
            }
        } else if (mode==MODE_SAVE_GAME) {
            header.setText(SAVE_GAME);
            setColor(PANEL_COLOR);
            for (int i=0; i<savedGamesCaptions.length; i++) {
                if (savedGamesCaptions[i]==null) {
                    slotButtons[i].setColor(Color.GRAY);
                } else {
                    slotButtons[i].setColor(SAVE_GAME_BACKGROUND);
                }
                slotButtons[i].visible = true;
            }
        }
    }

    public int getMode() {
        return mode;
    }

    protected ClickListener listener = new ClickListener() {
        @Override
        public void onClick(Widget w) {
            int choice = Integer.parseInt(w.getTag());
            GameSaveLoad gsl = mainMenuScene.getGameSaveLoad();
            Log.i("SLOT", "" + choice);
            if (mode==MODE_LOAD_GAME) {
                mainMenuScene.getMenuPanel().setProductionScene(gsl.loadGame(choice));
                SoundRenderer.playSound(tickSound);
                mainMenuScene.getSlotsPanel().visible = false;
            } else if (mode==MODE_SAVE_GAME) {
                ProductionScene gameScene = mainMenuScene.getMenuPanel().getProductionScene();
                if (gameScene==null || choice==0) return;
                gsl.saveGame(choice, gameScene);
                mainMenuScene.getMenuPanel().updateUI();
                SoundRenderer.playSound(tickSound);
                mainMenuScene.getSlotsPanel().visible = false;
            }
        }
    };

}
