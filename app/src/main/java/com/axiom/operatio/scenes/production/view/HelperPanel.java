package com.axiom.operatio.scenes.production.view;

import android.graphics.Color;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Texture;
import com.axiom.atom.engine.graphics.gles2d.TextureAtlas;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.graphics.renderers.Text;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.operatio.scenes.production.ProductionScene;

public class HelperPanel extends Panel {

    public final int panelColor = 0xCC505050;
    public final StringBuffer text = new StringBuffer(256);
    public Caption caption;
    private Sprite ceo;

    public HelperPanel(ProductionScene scene) {
        super();
        setLocalBounds(1320,0,600,140);
        setColor(panelColor);
        ceo = buildCeoHead();
        caption = new Caption(text);
        caption.setLocalBounds(20, 20, 400, 100);
        caption.setHorizontalAlignment(Text.ALIGN_LEFT);
        caption.setVerticalAlignment(Text.ALIGN_TOP);
        caption.setTextColor(Color.WHITE);
        addChild(caption);
        text.append("Вот тут будет текст миссии");
    }


    private Sprite buildCeoHead() {
        Texture texture = Texture.getInstance(SceneManager.getResources(), R.drawable.ceo);
        TextureAtlas atlas = new TextureAtlas(texture);
        atlas.addRegion("Head", 0, 0, (int) texture.getWidth(), 160);
        return new Sprite(texture, atlas);
    }


    public void setText(CharSequence s) {
        synchronized (text) {
            text.setLength(0);
            text.append(s);
        }
    }


    @Override
    public void draw(Camera camera) {
        synchronized (text) {
            super.draw(camera);
        }
        ceo.setZOrder(zOrder + 100);
        ceo.draw(camera, worldBounds.maxX - 100, worldBounds.minY + 80, 1);
    }

}
