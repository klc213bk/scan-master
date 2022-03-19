package com.tgl.scan.main.service;

import java.awt.Window;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ScanService extends Service<List<TiffRecord>> {

	private static final Logger logger = LogManager.getLogger(ScanService.class);

	private final StringProperty sourceName = new SimpleStringProperty();
	private final StringProperty colorMode = new SimpleStringProperty();
	private final StringProperty duplexMode = new SimpleStringProperty();
	private final BooleanProperty queryFromPage = new SimpleBooleanProperty();
	private final ObjectProperty<ScanConfig> scanConfig = new SimpleObjectProperty<ScanConfig>();
	private final ObjectProperty<ScannedImage> latestImage = new SimpleObjectProperty<ScannedImage>();
	private final ObjectProperty<Window> ownerFrame = new SimpleObjectProperty<Window>();

	public final String getColorMode() {
		return colorMode.get();
	}

	public final void setColorMode(String colorMode) {
		this.colorMode.set(colorMode);
	}

	public final String getSourceName() {
		return sourceName.get();
	}

	public final void setSourceName(String sourceName) {
		this.sourceName.set(sourceName);
	}

	public final void setDuplexMode(String duplexMode) {
		this.duplexMode.set(duplexMode);
	}

	public final String getDuplexMode() {
		return duplexMode.get();
	}

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

	public final Window getOwnerFrame() {
		return ownerFrame.get();
	}

	public final void setOwnerFrame(Window ownerFrame) {
		this.ownerFrame.set(ownerFrame);
	}

	@Override
	protected Task<List<TiffRecord>> createTask() {
		final String _sourceName = getSourceName();
		if (_sourceName == null) {
			throw new IllegalStateException("sourceName property value is null");
		}
		final String _colorMode = getColorMode();
		if (_colorMode == null) {
			throw new IllegalStateException("colorMode property value is null");
		}
		final String _duplexMode = getDuplexMode();
		if (_duplexMode == null) {
			throw new IllegalStateException("duplexMode property value is null");
		}
		final Boolean _queryFromPage = getQueryFromPage();
		if (_queryFromPage == null) {
			throw new IllegalStateException("queryFromPage property value is null");
		}
		final ScanConfig _scanConfig = getScanConfig();
		if (_scanConfig == null) {
			throw new IllegalStateException("scanConfig property value is null");
		}
		final ScannedImage _latestImage = getLatestImage();
		final Window _ownerFrame = getOwnerFrame();

		return new Task<List<TiffRecord>>() {
			@Override
			protected List<TiffRecord> call() throws IOException {
				int lastScanOrder = 0;
				if ( _latestImage != null ) {
					lastScanOrder = Integer.valueOf(_latestImage.scanOrderProperty().getValue());
				}
				ScanUtil.setLastScanOrder(lastScanOrder);

				List<TiffRecord> recordList = null;

				// 以下列程式碼，取得的 Window Handle 傳給 Asprise Imaging 物件使用時有問題，掃描的對話視窗一直沒出現
				//recordList = ScanUtil.scan(MainView.this.getScene().getWindow(), _sourceName, _colorMode, _duplexMode, _scanConfig); // 掃描後直接取得結果中的條碼

		    	if (_ownerFrame==null) {
			    	// 替代作法1: 以下列程式碼，可以顯示掃描的對話視窗，但不會顯示在最上層，會被其他視窗遮蔽
					recordList = ScanUtil.scan(_sourceName, _colorMode, _duplexMode, _queryFromPage, _scanConfig); // 掃描後直接取得結果中的條碼
		    	} else {
					// 替代作法2: 以下列程式碼，建立一個隱形的JFrame視窗，這個方法可以顯示掃描的對話視窗，但系統是否穩定、記憶體使用狀況...等，需再測試確認
					recordList = ScanUtil.scan(_ownerFrame, _sourceName, _colorMode, _duplexMode, _queryFromPage, _scanConfig); // 掃描後直接取得結果中的條碼
		    	}

				return recordList;
			}
		};
	}

}
