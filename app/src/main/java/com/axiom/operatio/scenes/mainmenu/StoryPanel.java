package com.axiom.operatio.scenes.mainmenu;

import android.graphics.Color;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Texture;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.operatio.model.common.FormatUtils;

import static android.graphics.Color.BLACK;

/**
 * Панель отображающая историю
 */
public class StoryPanel extends Panel {

    public static final int PANEL_COLOR = 0xCC505050;

    private Sprite storyFrames, ceo;
    private StringBuffer slideNumber = new StringBuffer();
    private int animationID;
    private int lastSlide = -1;

    public StoryPanel() {
        super();
        Texture storyTexture = Texture.getInstance(SceneManager.getResources(), R.drawable.story, true);
        storyFrames = new Sprite(storyTexture, 4,4);
        animationID = storyFrames.addAnimation(0,11, 0.2f, true);
        storyFrames.setActiveAnimation(animationID);
        Texture ceoTexture = Texture.getInstance(SceneManager.getResources(), R.drawable.ceo, true);
        ceo = new Sprite(ceoTexture, 1,1);
        opaque = false;
    }

    @Override
    public void draw(Camera camera) {
        AABB bounds = getWorldBounds();
        float ceoW = ceo.getWidth();
        float ceoH = ceo.getHeight();
        GraphicsRender.setZOrder(zOrder);
        GraphicsRender.setColor(BLACK);
        GraphicsRender.drawRectangle(bounds, null);
        storyFrames.setZOrder(zOrder + 1);
        storyFrames.drawExact(camera, bounds.minX + 15, bounds.minY + 15, bounds.maxX - 15, bounds.maxY - 15);
        // Если слайд изменился
        int activeFrame = storyFrames.getActiveFrame();
        int totalFrames = storyFrames.getAnimation(animationID).stopFrame;
        if (lastSlide != activeFrame) {
            slideNumber.setLength(0);
            slideNumber.append(activeFrame + 1);
            slideNumber.append('/');
            slideNumber.append(totalFrames + 1);
            lastSlide = activeFrame;
        }
        GraphicsRender.setZOrder(zOrder + 2);
        GraphicsRender.setColor(Color.BLACK);
        GraphicsRender.drawText(slideNumber, bounds.centerX - 20, bounds.minY + 25, 1.5f);

        ceo.setZOrder(zOrder + 100);
        ceo.draw(camera, bounds.maxX - ceoW / 2, bounds.minY - ceoH / 2, ceoW, ceoH);
        super.draw(camera);
    }

}
