package com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.dto;

public class ResultDto {
	private String processId;
	private String processNextNode;
	private String processStatus;
	public String getProcessId() {
		return processId;
	}
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	public String getProcessStatus() {
		return processStatus;
	}
	public void setProcessStatus(String processStatus) {
		this.processStatus = processStatus;
	}
	public String getProcessNextNode() {
		return processNextNode;
	}
	public void setProcessNextNode(String processNextNode) {
		this.processNextNode = processNextNode;
	}
	public ResultDto() {
		
	}
	public ResultDto(String processId, String processStatus) {
		this.processId = processId;
		this.processStatus = processStatus;
	}
}
