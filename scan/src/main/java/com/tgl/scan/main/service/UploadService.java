package com.tgl.scan.main.service;

import com.tgl.scan.main.bean.ScannedImage;
import com.tgl.scan.main.bean.UploadStatus;
import com.tgl.scan.main.http.EBaoException;
import com.tgl.scan.main.http.EbaoClient;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class UploadService extends Service<UploadStatus> {

	private final StringProperty uploadUrl = new SimpleStringProperty();
	private final ObjectProperty scannedImage = new SimpleObjectProperty();

	public final String getUploadUrl() {
		return uploadUrl.get();
	}

	public final void setUploadUrl(String uploadUrl) {
		this.uploadUrl.set(uploadUrl);
	}

	public final ScannedImage getScannedImage() {
		return (ScannedImage)scannedImage.get();
	}

	public final void setScannedImage(ScannedImage scannedImage) {
		this.scannedImage.set(scannedImage);
	}

	@Override
	protected Task<UploadStatus> createTask() {
		final String _uploadUrl = getUploadUrl();
		if (_uploadUrl == null) {
			throw new IllegalStateException("uploadUrl property value is null");
		}
		final ScannedImage _scannedImage = getScannedImage();
		if (_scannedImage == null) {
			throw new IllegalStateException("scannedImage property value is null");
		}

		return new Task<UploadStatus>() {
			@Override
			protected UploadStatus call() throws EBaoException {
	        	EbaoClient eBaoClient = EbaoClient.getInstance();
	        	return eBaoClient.upload(_uploadUrl, _scannedImage);
			}
		};
	}

}
