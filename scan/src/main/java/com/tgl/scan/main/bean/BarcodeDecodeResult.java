package com.tgl.scan.main.bean;

import java.util.ArrayList;
import java.util.List;

public class BarcodeDecodeResult {

	private String docType;
	private List<String> docNoList;

	public BarcodeDecodeResult() {
		this(null, new ArrayList<String>());
	}

	public BarcodeDecodeResult(String docType, List<String> docNoList) {
		super();
		this.docType = docType;
		this.docNoList = docNoList;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public List<String> getDocNoList() {
		return docNoList;
	}

	public void setDocNoList(List<String> docNoList) {
		this.docNoList = docNoList;
	}

	public void addDocNo(String docNo) {
		this.docNoList.add(docNo);
	}

}
