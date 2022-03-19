package com.tgl.scan.main.bean;

public class UploadProcessSummary {

	private Integer cntSuccess;
	private Integer cntUpload;
	private Integer cntFailed;

	private String dialogTitle;
	private String dialogMessage;

	public UploadProcessSummary(Integer cntSuccess, Integer cntUpload, Integer cntFailed, String dialogTitle, String dialogMessage) {
		super();
		this.cntSuccess = cntSuccess;
		this.cntUpload = cntUpload;
		this.cntFailed = cntFailed;
		this.dialogTitle = dialogTitle;
		this.dialogMessage = dialogMessage;
	}

	public Integer getCntSuccess() {
		return cntSuccess;
	}

	public void setCntSuccess(Integer cntSuccess) {
		this.cntSuccess = cntSuccess;
	}

	public Integer getCntUpload() {
		return cntUpload;
	}

	public void setCntUpload(Integer cntUpload) {
		this.cntUpload = cntUpload;
	}

	public Integer getCntFailed() {
		return cntFailed;
	}

	public void setCntFailed(Integer cntFailed) {
		this.cntFailed = cntFailed;
	}

	public String getDialogTitle() {
		return dialogTitle;
	}

	public void setDialogTitle(String dialogTitle) {
		this.dialogTitle = dialogTitle;
	}

	public String getDialogMessage() {
		return dialogMessage;
	}

	public void setDialogMessage(String dialogMessage) {
		this.dialogMessage = dialogMessage;
	}

	@Override
	public String toString() {
		return "UploadProcessSummary [cntSuccess=" + cntSuccess + ", cntUpload=" + cntUpload + ", cntFailed="
				+ cntFailed + ", dialogTitle=" + dialogTitle + ", dialogMessage=" + dialogMessage + "]";
	}

}
