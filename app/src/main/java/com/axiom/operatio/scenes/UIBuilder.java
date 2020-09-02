package com.axiom.operatio.scenes;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import com.axiom.atom.R;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.ui.listeners.ClickListener;
import com.axiom.atom.engine.ui.widgets.Button;
import com.axiom.atom.engine.ui.widgets.Panel;
import com.axiom.atom.engine.ui.widgets.Widget;

public class UIBuilder {

    public static void buildUI(Resources resources, Widget widget) {

        ClickListener listener = new ClickListener() {
            @Override
            public void onClick(Widget w) {
                Log.i("INPUT", "BUTTON PRESSED " + w.toString());
            }
        };

        Widget panel = new Panel();
        panel.setLocalBounds(0,0,1920,180);
        // panel.setColor(1,0,0,0.7f);
        panel.setClickListener(listener);
        widget.addChild(panel);

        Widget button = new Button("EXIT BUTTON");
        button.setLocalBounds(1680,40,200,100);
        button.setColor(Color.GREEN);
        button.setClickListener(listener);
        panel.addChild(button);

        Widget button2 = new Button(new Sprite(resources, R.drawable.material));
        button2.setLocalBounds(50,40,200,100);
        button2.setColor(Color.YELLOW);
        button2.setClickListener(listener);
        panel.addChild(button2);
    }

}
