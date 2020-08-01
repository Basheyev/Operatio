package com.axiom.operatio.model.production.materials;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.graphics.renderers.Sprite;

/**
 * Класс содержащий информацию о материале (сырье, комплектующее, полуфабрикат, продукт).
 */
public class Material {

    public int materialID;      // Код материала
    public String name;         // Название материала
    public Sprite image;        // Изображение материала

    public static final int MEASURE_UNITS = 0;        // Количество (штук)
    public static final int MEASURE_KILOGRAMS = 1;    // Вес (1 тонна = 1000 кг)
    public static final int MEASURE_LITERS = 2;       // Объем (1 кубический метр = 1000 литров)

    public Material(Resources resources, int ID, String name) {
        this.materialID = ID;
        this.name = name;
        this.image = new Sprite(resources, R.drawable.materials, 8,8);
        image.setActiveFrame(ID);
    }

}
