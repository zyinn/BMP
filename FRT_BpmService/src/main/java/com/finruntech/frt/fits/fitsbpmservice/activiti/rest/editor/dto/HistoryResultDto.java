package com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.dto;

public class HistoryResultDto {
	private String userId;        //审批人ID
	private String approvalTime;  //审批时间
	private String approvalStep;  //审批步骤
	private String opinion;       //审批意见
	private String approvalResult;//审批结果
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getApprovalTime() {
		return approvalTime;
	}
	public void setApprovalTime(String approvalTime) {
		this.approvalTime = approvalTime;
	}
	public String getOpinion() {
		return opinion;
	}
	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}
	public String getApprovalResult() {
		return approvalResult;
	}
	public void setApprovalResult(String approvalResult) {
		this.approvalResult = approvalResult;
	}
	public String getApprovalStep() {
		return approvalStep;
	}
	public void setApprovalStep(String approvalStep) {
		this.approvalStep = approvalStep;
	}
	public HistoryResultDto(String userId, String approvalTime, String opinion, String approvalResult) {
		super();
		this.userId = userId;
		this.approvalTime = approvalTime;
		this.opinion = opinion;
		this.approvalResult = approvalResult;
	}
	public HistoryResultDto() {
		
	}
	
}
