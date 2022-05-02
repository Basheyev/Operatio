package com.basheyev.operatio.model.common;

import org.json.JSONObject;

/**
 * Интерфейс для сериализуемых игровых объектов
 */
public interface JSONSerializable {

    JSONObject toJSON();

}
