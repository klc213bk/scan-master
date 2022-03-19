package com.tgl.scan.main.bean;

/**
 * ImageRecordSet.xml(掃描文件紀錄檔)內每筆文件metadata屬性名稱
 * <Field name="fileName" value="2020100512220156201.tiff" text="2020100512220156201.tiff"/>
 * text影像內容
 * @author ASDTEMP23
 *
 */
public class RecordFieldText {
	 
	public String Remark = "";//影像備註
	public String fileName = "";//檔案名稱
	public String fromQueryPage = "";//是否從查詢網頁帶起此控件;1:是:true;2:否:false(server端帶入)
	public String MainFileType = "";//影像主類型;ex:UNB;UNB-新契約
	public String BatchDate = "";//日期
	public String ActionReplace = "";//此文件是為取代文件
	public String imageSaveDir = "";//影像掃瞄文件儲資料夾位置(server端帶入)
	public String BatchDepType = "";//批次號碼-部門別;ex:NB:NB
	public String bizDept = "";//商業部門;ex: NB:NB(server端帶入)
	public String OrgName = "";//組織編碼 
	public String ScanTime = "";//掃瞄日期時間
	public String DeptName = "";//部室名稱
	public String isGID = "";//(server端帶入)
	public String MaxPage = "";//文件類最大頁數
	public String BoxNo = "";//箱號
	public String PersonalCode = "";//團險-個人碼
	public String FileType = "";//影像子類型；ex：UNBA040:UNBA040-傳統型保險要保書
	public String Signature = "";//是否有簽名檔
	public String RocDate = "";//紀錄日期(server端帶入)
	public String FileCode = "";//文件編號
	public String orgCode = "";//組織代碼ex:101(server端帶入)
	public String actionType = "";//備註
	public String ScanOrder = "";//掃描順序(序號)
	public String ActionInsert = "";//此文件是為插入文件
	public String updateRole = "";//登入使用者是否有異動權限(server端帶入)
	public String batchDepTypeValue = "";//批次號碼-部門別;ex:NB:NB(server端帶入)
	public String recordStatus = "";//文件紀錄狀態;1:儲存;0:未儲存(server端帶入)
	public String empId = "";//員工編號(server端帶入)
	public String step = "";//(server端帶入)
	public String FilePage = "";//此文件類別中的第幾頁數
	public String ImageFormat = "";//(server端帶入)
	public String BatchDocType = "";//文件別
	public String deptId = "";//(server端帶入)
	public String SigSeqNumber = "";
	public String CompanyCode = "";//公司碼(團險保單碼碼)
	public String BatchArea = "";//分區
	
	public String getRemark() {
		return Remark;
	}
	public void setRemark(String remark) {
		Remark = remark;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFromQueryPage() {
		return fromQueryPage;
	}
	public void setFromQueryPage(String fromQueryPage) {
		this.fromQueryPage = fromQueryPage;
	}
	public String getMainFileType() {
		return MainFileType;
	}
	public void setMainFileType(String mainFileType) {
		MainFileType = mainFileType;
	}
	public String getBatchDate() {
		return BatchDate;
	}
	public void setBatchDate(String batchDate) {
		BatchDate = batchDate;
	}
	public String getActionReplace() {
		return ActionReplace;
	}
	public void setActionReplace(String actionReplace) {
		ActionReplace = actionReplace;
	}
	public String getImageSaveDir() {
		return imageSaveDir;
	}
	public void setImageSaveDir(String imageSaveDir) {
		this.imageSaveDir = imageSaveDir;
	}
	public String getBatchDepType() {
		return BatchDepType;
	}
	public void setBatchDepType(String batchDepType) {
		BatchDepType = batchDepType;
	}
	public String getBizDept() {
		return bizDept;
	}
	public void setBizDept(String bizDept) {
		this.bizDept = bizDept;
	}
	public String getOrgName() {
		return OrgName;
	}
	public void setOrgName(String orgName) {
		OrgName = orgName;
	}
	public String getScanTime() {
		return ScanTime;
	}
	public void setScanTime(String scanTime) {
		ScanTime = scanTime;
	}
	public String getDeptName() {
		return DeptName;
	}
	public void setDeptName(String deptName) {
		DeptName = deptName;
	}
	public String getIsGID() {
		return isGID;
	}
	public void setIsGID(String isGID) {
		this.isGID = isGID;
	}
	public String getMaxPage() {
		return MaxPage;
	}
	public void setMaxPage(String maxPage) {
		MaxPage = maxPage;
	}
	public String getBoxNo() {
		return BoxNo;
	}
	public void setBoxNo(String boxNo) {
		BoxNo = boxNo;
	}
	public String getPersonalCode() {
		return PersonalCode;
	}
	public void setPersonalCode(String personalCode) {
		PersonalCode = personalCode;
	}
	public String getFileType() {
		return FileType;
	}
	public void setFileType(String fileType) {
		FileType = fileType;
	}
	public String getSignature() {
		return Signature;
	}
	public void setSignature(String signature) {
		Signature = signature;
	}
	public String getRocDate() {
		return RocDate;
	}
	public void setRocDate(String rocDate) {
		RocDate = rocDate;
	}
	public String getFileCode() {
		return FileCode;
	}
	public void setFileCode(String fileCode) {
		FileCode = fileCode;
	}
	public String getOrgCode() {
		return orgCode;
	}
	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}
	public String getActionType() {
		return actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	public String getScanOrder() {
		return ScanOrder;
	}
	public void setScanOrder(String scanOrder) {
		ScanOrder = scanOrder;
	}
	public String getActionInsert() {
		return ActionInsert;
	}
	public void setActionInsert(String actionInsert) {
		ActionInsert = actionInsert;
	}
	public String getUpdateRole() {
		return updateRole;
	}
	public void setUpdateRole(String updateRole) {
		this.updateRole = updateRole;
	}
	public String getBatchDepTypeValue() {
		return batchDepTypeValue;
	}
	public void setBatchDepTypeValue(String batchDepTypeValue) {
		this.batchDepTypeValue = batchDepTypeValue;
	}
	public String getRecordStatus() {
		return recordStatus;
	}
	public void setRecordStatus(String recordStatus) {
		this.recordStatus = recordStatus;
	}
	public String getEmpId() {
		return empId;
	}
	public void setEmpId(String empId) {
		this.empId = empId;
	}
	public String getStep() {
		return step;
	}
	public void setStep(String step) {
		this.step = step;
	}
	public String getFilePage() {
		return FilePage;
	}
	public void setFilePage(String filePage) {
		FilePage = filePage;
	}
	public String getImageFormat() {
		return ImageFormat;
	}
	public void setImageFormat(String imageFormat) {
		ImageFormat = imageFormat;
	}
	public String getBatchDocType() {
		return BatchDocType;
	}
	public void setBatchDocType(String batchDocType) {
		BatchDocType = batchDocType;
	}
	public String getDeptId() {
		return deptId;
	}
	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}
	public String getSigSeqNumber() {
		return SigSeqNumber;
	}
	public void setSigSeqNumber(String sigSeqNumber) {
		SigSeqNumber = sigSeqNumber;
	}
	public String getCompanyCode() {
		return CompanyCode;
	}
	public void setCompanyCode(String companyCode) {
		CompanyCode = companyCode;
	}
	public String getBatchArea() {
		return BatchArea;
	}
	public void setBatchArea(String batchArea) {
		BatchArea = batchArea;
	}
	
}
