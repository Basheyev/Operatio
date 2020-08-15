package com.axiom.operatio.modelold.production.materials;

import android.content.res.Resources;

import com.axiom.atom.R;
import com.axiom.atom.engine.core.SceneManager;
import com.axiom.atom.engine.graphics.renderers.Sprite;
import com.axiom.atom.engine.data.CSVFile;

/**
 * Система материалов использует данные из (materials.png, materials.csv)
 */
public class Material {

    private static boolean initialized;       // Флаг инициализации системы материалов
    private static Sprite image;              // Изображения всех материалов
    private static Material[] materials;      // Перечень всех материалов

    public int materialID;                    // Код материала
    public String name;                       // Наименование материала

    /**
     * Конструктор материала
     * @param ID номер материала
     * @param name название материала
     */
    private Material(int ID, String name) {
        materialID = ID;
        this.name = name;
    }


    public static Material getMaterial(int ID) {
        if (!initialized) loadMaterialsData(SceneManager.getResources());
        if (ID < 0 || ID >= materials.length) return null;
        return materials[ID];
    }

    /**
     * Возвращает изображение материала
     * @return спрайт с выставленным кадром материала
     */
    public Sprite getImage() {
        image.setActiveFrame(materialID);
        return image;
    }

    public String getName() {
        return name;
    }

    /**
     * Загружает изображение и описание 64 материалов (8x8)
     * @param resources ресурсы приложения
     */
    protected static void loadMaterialsData(Resources resources) {
        // Загружаем спрайт с изображегиями всех материалов
        image = new Sprite(resources, R.drawable.materials, 8,8);
        // Создаём массив материалов
        materials = new Material[64];
        CSVFile csv = new CSVFile(resources, R.raw.materials);
        int ID, rows = csv.getRowCount();
        for (int i=0; i<rows; i++) {
            ID = Integer.parseInt(csv.getValue(i,0));
            if (ID>=0 && ID < materials.length) {
                materials[ID] = new Material(ID, csv.getValue(i,1).trim());
            }
        }
        initialized = true;
    }

}
