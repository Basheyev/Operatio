package com.axiom.operatio.scenes.production.view;

import com.axiom.atom.R;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.sound.SoundRenderer;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.block.Block;
import com.axiom.operatio.model.materials.Material;

import static android.graphics.Color.GRAY;
import static android.graphics.Color.WHITE;

public class OperationPanel extends Panel {

    public final int panelColor = 0xCC505050;
    protected Button leftButton, materialButton, rightButton;
    protected int materialID = 0;
    protected int tickSound;

    public OperationPanel() {
        super();
        setLocalBounds(Camera.WIDTH - 400,200,400, 700);
        setColor(panelColor);
        tickSound = SoundRenderer.loadSound(R.raw.tick_snd);
        buildButtons();
    }


    protected void buildButtons() {
        Material m = Material.getMaterial(materialID);

        ClickListener clickListener = new ClickListener() {
            @Override
            public void onClick(Widget w) {
                SoundRenderer.playSound(tickSound);
                Button button = (Button) w;
                if (button.getTag().equals("<")) {
                    materialID--;
                    if (materialID < 0) materialID = 0;
                } else if (button.getTag().equals(">")) {
                    materialID++;
                    if (materialID >= Material.getMaterialsAmount()) materialID = Material.getMaterialsAmount() - 1;
                }
                Material m = Material.getMaterial(materialID);
                materialButton.setBackground(m.getImage());
            }
        };

        Caption caption = new Caption("Block information");
        caption.setLocalBounds(50,599,300, 100);
        caption.setScale(1.5f);
        caption.setTextColor(WHITE);
        addChild(caption);

        leftButton = new Button("<");
        leftButton.setLocalBounds( 50, 500, 75, 100);
        leftButton.setTag("<");
        leftButton.setColor(GRAY);
        leftButton.setTextColor(WHITE);
        leftButton.setClickListener(clickListener);
        addChild(leftButton);

        materialButton = new Button(m.getImage());
        materialButton.setLocalBounds( 150, 500, 100, 100);
        materialButton.setColor(GRAY);
        materialButton.setTextColor(WHITE);
        addChild(materialButton);

        rightButton = new Button(">");
        rightButton.setTag(">");
        rightButton.setLocalBounds( 275, 500, 75, 100);
        rightButton.setColor(GRAY);
        rightButton.setTextColor(WHITE);
        rightButton.setClickListener(clickListener);
        addChild(rightButton);
    }


    public void showBlockInfo(Block block) {

    }

}
