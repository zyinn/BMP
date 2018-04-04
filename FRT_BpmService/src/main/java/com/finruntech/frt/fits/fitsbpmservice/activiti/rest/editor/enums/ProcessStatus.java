package com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.enums;

public enum ProcessStatus {
	on("未结束","1"),
    off("已结束","0");

    private String displayName;
    private String code;
    ProcessStatus( String name,String code) {
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
