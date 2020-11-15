package com.axiom.operatio.model.gameplay;

import com.axiom.operatio.model.production.Production;

/**
 * Описывает условия победы и проигрыша
 * TODO Сделать уровни и вывести ключевые показатели
 *
 * Правила:
 *  - Прохождение уровня - это производство нужного количества товаров в день (N x M)
 *  - Ты можешь покупать сначала только 0-7 материалы
 *  - Тебе разрешено покупать другие материалы только если ты их производил (Нужно ли это?)
 *  - Тебе разрешено покупать машины (после прохождения N уровней - освоение и обучение)
 *  (достижение мощности производства и/или требуемого уровня прибыли в месяц)
 *  - Ты проигрываешь когда у тебя заканчиваются деньги или истекает время (конец игры)
 */
public class Level {

    public String getLevelDescription() {
        return "Build product";
    }

    public boolean isMachineAvailable(int machineID) {
        return false;
    }

    public boolean isAllowedToBuy(int commodity) {
        return false;
    }

    public boolean isAllowedToSell(int commodity) {
        return false;
    }


    public boolean checkWin(Production production) {
        return false;
    }


    public boolean checkLose(Production production) {
        return false;
    }


}
