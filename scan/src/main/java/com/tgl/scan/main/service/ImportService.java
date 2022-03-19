package com.tgl.scan.main.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tgl.scan.main.bean.ScanConfig;
import com.tgl.scan.main.bean.ScannedImage;
import com.tgl.scan.main.bean.TiffRecord;
import com.tgl.scan.main.util.ScanUtil;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ImportService extends Service<List<TiffRecord>> {

	private static final Logger logger = LogManager.getLogger(ImportService.class);

	private final BooleanProperty queryFromPage = new SimpleBooleanProperty();
	private final ObjectProperty<ScanConfig> scanConfig = new SimpleObjectProperty<>();
	private final ObjectProperty<ScannedImage> latestImage = new SimpleObjectProperty<>();
	private final ObjectProperty<File> selectedFile = new SimpleObjectProperty<>();

	public final void setQueryFromPage(Boolean queryFromPage) {
		this.queryFromPage.set(queryFromPage);
	}

	public final Boolean getQueryFromPage() {
		return queryFromPage.get();
	}

	public final ScanConfig getScanConfig() {
		return scanConfig.get();
	}

	public final void setScanConfig(ScanConfig scanConfig) {
		this.scanConfig.set(scanConfig);
	}

	public final int getLatestImageIndexNo() {
		int latestImageIndexNo = 0;
		if (null != getLatestImage() && null != getLatestImage().indexNoProperty() ) {
			latestImageIndexNo = getLatestImage().indexNoProperty().getValue();
		}
		return latestImageIndexNo;
	}

	public final ScannedImage getLatestImage() {
		return latestImage.get();
	}

	public final void setLatestImage(ScannedImage latestImage) {
		this.latestImage.set(latestImage);
	}

	public final File getSelectedFile() {
		return selectedFile.get();
	}

	public final void setSelectedFile(File selectedFile) {
		this.selectedFile.set(selectedFile);
	}

	@Override
	protected Task<List<TiffRecord>> createTask() {
		final Boolean _queryFromPage = getQueryFromPage();
		if (_queryFromPage == null) {
			throw new IllegalStateException("queryFromPage property value is null");
		}
		final ScanConfig _scanConfig = getScanConfig();
		if (_scanConfig == null) {
			throw new IllegalStateException("scanConfig property value is null");
		}
		final ScannedImage _latestImage = getLatestImage();
		final File _selectedFile = getSelectedFile();
		if (_selectedFile == null) {
			throw new IllegalStateException("selectedFile property value is null");
		}

		return new Task<List<TiffRecord>>() {
			@Override
			protected List<TiffRecord> call() throws IOException {
				int lastScanOrder = 0;
				if ( _latestImage != null ) {
					lastScanOrder = Integer.valueOf(_latestImage.scanOrderProperty().getValue());
				}
				ScanUtil.setLastScanOrder(lastScanOrder);
				ScanUtil.setMultiPolicy(false);
				ScanUtil.setFileCodeCount(0);
				List<TiffRecord> recordList = ScanUtil.convertFile(_selectedFile, _queryFromPage, _scanConfig);
				return recordList;
			}
		};
	}

}
