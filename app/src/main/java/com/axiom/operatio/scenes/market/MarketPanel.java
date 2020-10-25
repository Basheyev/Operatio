package com.axiom.operatio.scenes.market;

import android.graphics.Color;

import com.axiom.atom.engine.core.geometry.AABB;
import com.axiom.atom.engine.graphics.GraphicsRender;
import com.axiom.atom.engine.graphics.gles2d.Camera;
import com.axiom.atom.engine.ui.widgets.Caption;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;
import com.axiom.operatio.model.market.Market;
import com.axiom.operatio.scenes.inventory.MaterialsPanel;

import static android.graphics.Color.WHITE;

public class MarketPanel extends Panel {

    private Caption caption;
    private Market market;
    private MaterialsPanel materialsPanel;

    public MarketPanel(MaterialsPanel materialsPanel, Market market) {
        super();
        this.market = market;
        this.materialsPanel = materialsPanel;
        setLocalBounds(900, 430, 1000, 550);
        setColor(0xCC505050);
        caption = new Caption("Market");
        caption.setScale(1.5f);
        caption.setTextColor(WHITE);
        caption.setLocalBounds(30, getHeight() - 100, 300, 100);
        addChild(caption);
    }


    @Override
    public void draw(Camera camera) {
        super.draw(camera);

        AABB wBounds = getWorldBounds();
        AABB scissor = getScissors();

        GraphicsRender.setZOrder(zOrder + 1);
        GraphicsRender.setColor(Color.GREEN);
        float x, y, oldX = 0, oldY = getHeight() / 2;
        for (int i=0; i<getWidth(); i+=20) {
            x = i;
            y = ((float) Math.sin(i / 100.0f)) * 100 + getHeight() / 2;

            if (y>=oldY) GraphicsRender.setColor(Color.GREEN); else GraphicsRender.setColor(Color.RED);

            GraphicsRender.drawLine(
                    wBounds.min.x + x, wBounds.min.y + oldY,
                    wBounds.min.x + x,wBounds.min.y +  y, scissor);
            oldX = x;
            oldY = y;
        }
    }

}
