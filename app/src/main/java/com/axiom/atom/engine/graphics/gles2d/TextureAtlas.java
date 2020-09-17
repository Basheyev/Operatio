package com.axiom.atom.engine.graphics.gles2d;



// TODO Перенести сюда все вопросы связанные с расчётом текстурных координат в спрайте
// Strip - линия спрайтов одинакового размера (автоматически генерируется деля на количество)
// Tileset - сетка спрайтов одинакового размера (автоматически генерируется для на столбцы и строки)
// Atlas - регионы с разными размерами и координатами (загружается JSON с регионами)
// Сначала надо вынести из Sprite вычисление текстурных координат по Strip/Tileset
// Затем реализовать Atlas

import java.util.ArrayList;

public class TextureAtlas {

    protected Texture texture;
    protected ArrayList<Region> regions;

    public class Region {
        float[] textureCoordinates = new float[12];
    }

    public TextureAtlas(Texture texture) {
        this.texture = texture;
        regions = new ArrayList<Region>();
    }

    public void addRegion(int x, int y, int width, int height) {

    }

}
