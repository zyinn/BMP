package com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.dto;

import com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.util.JsonUtil;

public class ReturnJsonDto {
	private String status;
	private String describe;
	private ResultDto result;
	
	public String getDescribe() {
		return describe;
	}
	public void setDescribe(String describe) {
		this.describe = describe;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public ResultDto getResult() {
		return result;
	}
	public void setResult(ResultDto result) {
		this.result = result;
	}
	public ReturnJsonDto() {
	}
	
	public ReturnJsonDto(String status, ResultDto result,String describe) {
		this.status = status;
		this.result = result;
		this.describe=describe;
	}
	public String getJson4ReturnJsonDto(ReturnJsonDto rjDto){
		return JsonUtil.toJson(rjDto);
	}
}
