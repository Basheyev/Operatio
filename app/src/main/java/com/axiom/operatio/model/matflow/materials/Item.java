package com.axiom.operatio.model.matflow.materials;

import com.axiom.operatio.model.matflow.blocks.Block;

/**
 * Единица материала используемая в производственном процессе
 */
public class Item {

    public Material info;               // Информация о материале
    public Block owner;                 // Блок в котором находится материал
    public float temperature;           // Температура по Цельсию
    public float quality;               // Качество материала (0-1)
    public long processingStart;        // Служебная информация о времени начала обработки

    public Item(Material materialType) {
        info = materialType;
        quality = 1.0f;
        temperature = 0;
    }

}
