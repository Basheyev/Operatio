package com.axiom.operatio.modelold.production.materials;

import com.axiom.operatio.modelold.production.old.BlockOld;

/**
 * Единица материала используемая в производственном процессе
 */
public class Item {

    public Material material;                 // Информация о материале
    public BlockOld owner;                       // Блок в котором находится материал
    public long processingStart;              // Служебная информация о времени начала обработки
    public float temperature;                 // Температура по Цельсию
    public float quality;                     // Качество материала (0-1)


    public Item(int materialID) {
        material = Material.getMaterial(materialID);
        quality = 1.0f;
        temperature = 0;
    }



}
