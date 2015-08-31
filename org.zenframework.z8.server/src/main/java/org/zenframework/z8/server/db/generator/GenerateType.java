/**
 * Created by Eclipse 3.x
 * User: m.vlasov
 * Date: 09.03.2011
 * Time: 11:52:39
 */

package org.zenframework.z8.server.db.generator;

public class GenerateType {
    static final public int none = 0x0; // Генерация не требуется
    static final public int lite = 0x1; // Добавление новых таблиц - полей
    static final public int full = 0x2; // Требуется перегенерация таблиц
    static final public int olds = 0x4; // Требуется удаление старых таблиц

    private int type = none;

    public GenerateType() {}

    public int getType() {
        return type;
    }

    public void setLite() {
        type |= lite;
    }

    public void setFull() {
        type |= full;
    }

    public void setOlds() {
        type |= olds;
    }

    public boolean isLite() {
        return (type & lite) == lite;
    }

    public boolean isFull() {
        return (type & full) == full;
    }

    public boolean isOlds() {
        return (type & olds) == olds;
    }

}
