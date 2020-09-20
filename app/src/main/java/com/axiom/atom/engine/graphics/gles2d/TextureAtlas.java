package com.axiom.atom.engine.graphics.gles2d;



// TODO Перенести сюда все вопросы связанные с расчётом текстурных координат в спрайте
// Strip - линия спрайтов одинакового размера (автоматически генерируется деля на количество)
// Tileset - сетка спрайтов одинакового размера (автоматически генерируется для на столбцы и строки)
// Atlas - регионы с разными размерами и координатами (загружается JSON с регионами)
// Сначала надо вынести из Sprite вычисление текстурных координат по Strip/Tileset
// Затем реализовать Atlas

import java.util.ArrayList;

public class TextureAtlas {

    public class Region {
        public String name;
        public int x, y;
        public int width, height;
        public float[] textureCoordinates;
    }

    protected Texture texture;
    protected ArrayList<Region> regions;

    public TextureAtlas(Texture texture) {
        this.texture = texture;
        regions = new ArrayList<Region>();
    }

    public Region addRegion(String name, int x, int y, int width, int height) {
        // Уходим если не валидные параметры
        if (name==null || (x < 0) || (y < 0) || (width<1) || (height<1)) return null;
        // Сохраняем информацию о регионе
        Region region = new Region();
        region.name = name;
        region.x = x;
        region.y = y;
        region.width = width;
        region.height = height;
        region.textureCoordinates = new float[12];
        // Переворачиваем Y координату и нормируем на единицу (0.0-1.0)
        float x1 = x / texture.width;
        float y1 = (y + height) / texture.height;
        float x2 = (x + width) / texture.width;
        float y2 = y / texture.height;
        // ===== Треугольник 1
        region.textureCoordinates[0] = x1;  // левый верхний угол
        region.textureCoordinates[1] = y2;
        region.textureCoordinates[2] = x1;  // левый нижний угол
        region.textureCoordinates[3] = y1;
        region.textureCoordinates[4] = x2;  // правый верхний угол
        region.textureCoordinates[5] = y2;
        // ===== Треугольник 2
        region.textureCoordinates[6] = x1;  // левый нижний угол
        region.textureCoordinates[7] = y1;
        region.textureCoordinates[8] = x2;  // правый верхний угол
        region.textureCoordinates[9] = y2;
        region.textureCoordinates[10] = x2; // правый нижний угол
        region.textureCoordinates[11] = y1;
        // Добавляем в список
        regions.add(region);
        return region;
    }

    public Region getRegion(String name) {
        Region region;
        for (int i=0; i<regions.size(); i++) {
            region = regions.get(i);
            if (region.name.equals(name)) return region;
        }
        return null;
    }

    public boolean removeRegion(Region region) {
        return regions.remove(region);
    }

}

