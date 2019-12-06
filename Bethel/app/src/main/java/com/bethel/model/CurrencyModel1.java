package com.bethel.model;

/**
 * Created by sidharath on 4/10/16.
 */
public class CurrencyModel1 {

        String name,code;
    boolean isChecked;
    boolean isDisabled;

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    public boolean isChecked() {
        return isChecked;
    }


    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }
}
