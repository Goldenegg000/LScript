package org.goldenegg.LScript.Types;

import org.goldenegg.LScript.LSValue;

public class LObject extends LSValue {

    public LObject() {
    }

    @Override
    public LString getType() {
        return new LString("Object");
    }

    public String toString() {
        return "Object";
    }
}