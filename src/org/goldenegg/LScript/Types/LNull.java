package org.goldenegg.LScript.Types;

import org.goldenegg.LScript.LSValue;

public class LNull extends LSValue {

    public LNull() {
    }

    @Override
    public LString getType() {
        return new LString("Null");
    }

    public String toString() {
        return "NULL";
    }
}