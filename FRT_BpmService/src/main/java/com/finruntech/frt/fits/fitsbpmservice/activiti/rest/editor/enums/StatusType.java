package com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.enums;

public enum StatusType {
	Sucess("成功","200"),
    Fail("失败","500");

    private String displayName;
    private String code;
    StatusType( String name,String code) {
        this.displayName = name;
        this.code = code;
    }
    public String getCode() {
        return code;
    }
    public String getDisplayName() {
        return displayName;
    }
}
