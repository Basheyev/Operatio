package com.axiom.operatio.scenes.mainmenu;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.graphics.gles2d.Texture;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.ui.widgets.Panel;

import static android.graphics.Color.BLACK;

/**
 * Панель отображающая историю
 */
public class StoryPanel extends Panel {

    public static final int PANEL_COLOR = 0xCC505050;

    private Sprite storyFrames, ceo;

    public StoryPanel() {
        super();
        Texture storyTexture = Texture.getInstance(SceneManager.getResources(), R.drawable.story, true);
        storyFrames = new Sprite(storyTexture, 4,4);
        storyFrames.setActiveAnimation(storyFrames.addAnimation(0,11, 0.2f, true));
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
        ceo.setZOrder(zOrder + 100);
        ceo.draw(camera, bounds.maxX - ceoW / 2, bounds.minY - ceoH / 2, ceoW, ceoH);
        super.draw(camera);
    }

}
