package com.tgl.scan.main.view;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jfoenix.controls.JFXProgressBar;
import com.tgl.scan.main.Constant;
import com.tgl.scan.main.bean.ScanConfig;
import com.tgl.scan.main.bean.ScannedImage;
import com.tgl.scan.main.bean.UploadProcessSummary;
import com.tgl.scan.main.bean.UploadStatus;
import com.tgl.scan.main.http.EBaoException;
import com.tgl.scan.main.log.UploadLog;
import com.tgl.scan.main.service.UploadService;
import com.tgl.scan.main.util.DialogUtil;
import com.tgl.scan.main.util.PropertiesCache;
import com.tgl.scan.starter.AbstractFxView;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class UploadView extends AbstractFxView {

	private static final Logger logger = LogManager.getLogger(UploadView.class);

	public static final int UPLOAD_STATUS_READY = 0;
	public static final int UPLOAD_STATUS_UPLOADING = 1;
	public static final int UPLOAD_STATUS_UPLOADED = 2;
	public static final int UPLOAD_STATUS_FAILED = -1;

	// readonly table view
	@FXML
    private TableView<ScannedImage> uploadTableView;
    @FXML
    private TableColumn<ScannedImage, Integer> uploadStatusColumn;
    @FXML
    private TableColumn<ScannedImage, String> scanOrderColumn;
    @FXML
    private TableColumn<ScannedImage, String> fileCodeColumn;
    @FXML
    private TableColumn<ScannedImage, String> mainFileTypeColumn;
    @FXML
    private TableColumn<ScannedImage, String> fileTypeColumn;
    @FXML
    private TableColumn<ScannedImage, String> companyCodeColumn;
    @FXML
    private TableColumn<ScannedImage, String> personalCodeColumn;
    @FXML
    private TableColumn<ScannedImage, String> filePageColumn;
    @FXML
    private TableColumn<ScannedImage, String> scanTimeColumn;
    @FXML
    private TableColumn<ScannedImage, String> remarkColumn;
    @FXML
	private JFXProgressBar uploadProgress;
    @FXML
	private Label lbSuccess;
    @FXML
	private Label lbUpload;
    @FXML
	private Label lbFailed;

    private IntegerProperty cntSuccess = new SimpleIntegerProperty(0);
    private IntegerProperty cntUpload = new SimpleIntegerProperty(0);
    private IntegerProperty cntFailed = new SimpleIntegerProperty(0);
    private IntegerProperty uploadIndex = new SimpleIntegerProperty(-1);
    private IntegerProperty retryIndex = new SimpleIntegerProperty(-1);
    private String dialogTitle = null;
    private String dialogMessage = null;

    private MainView parent = null;
    private ScanConfig scanConfig = null;
    private boolean isUploadAll = false;
    private ObservableList<ScannedImage> imageList = null;
    private Integer selectedIndex = null;
    private String titleText = null;
    private boolean logWhenSuccess = false;

    public UploadView(MainView parent, ScanConfig scanConfig, ObservableList<ScannedImage> imageList, Integer selectedIndex) {
		super();
		this.parent = parent;
		this.scanConfig = scanConfig;
		this.imageList = imageList;
		this.selectedIndex = selectedIndex;
		titleText = null;
	}

    @Override
	public void initialize(URL location, ResourceBundle resources) {
    	uploadStatusColumn.setCellValueFactory(cellData -> cellData.getValue().uploadStatusProperty()==null ? null : cellData.getValue().uploadStatusProperty().asObject());
    	uploadStatusColumn.setCellFactory(new Callback<TableColumn<ScannedImage, Integer>, TableCell<ScannedImage, Integer>>() {
		    @Override
		    public TableCell<ScannedImage, Integer> call(TableColumn<ScannedImage, Integer> p) {
		        TableCell<ScannedImage, Integer> cell = new TableCell<ScannedImage, Integer>() {
		            @Override
		            public void updateItem(Integer item, boolean empty) {
		                super.updateItem(item, empty);
		                ImageView view = null;
	    	            if ( item == null ) {
	    	            	view = null;
	      	            } else {
	      	            	view = new ImageView();
		    	            if ( item.intValue() == UPLOAD_STATUS_READY ) { // Ready to upload
		      	            	view.setImage(new Image("images/icon-upload-wait.png"));
		      	            } else if ( item.intValue() == UPLOAD_STATUS_UPLOADING ) { // Uploading
		      	            	view.setImage(new Image("images/icon-uploading.gif"));
		      	            } else if ( item.intValue() == UPLOAD_STATUS_UPLOADED ) { // Uploaded
		      	            	view.setImage(new Image("images/icon-uploaded.png"));
		      	            } else if ( item.intValue() == UPLOAD_STATUS_FAILED ) { // Upload failed
		      	            	view.setImage(new Image("images/icon-upload-failed.png"));
		      	            }
		    	            view.setFitWidth(20);
		    	            view.setFitHeight(20);
	      	            }
		                setGraphic(view);
		           }
		        };
		        cell.setAlignment(Pos.CENTER);
		        return cell;
		    }
		});
    	uploadStatusColumn.setSortable(false);
    	scanOrderColumn.setCellValueFactory(cellData -> cellData.getValue().scanOrderProperty());
    	scanOrderColumn.setSortable(false);
    	fileCodeColumn.setCellValueFactory(cellData -> cellData.getValue().fileCodeProperty());
    	fileCodeColumn.setSortable(false);
    	mainFileTypeColumn.setCellValueFactory(cellData -> cellData.getValue().mainFileTypeProperty());
    	mainFileTypeColumn.setSortable(false);
    	fileTypeColumn.setCellValueFactory(cellData -> cellData.getValue().fileTypeProperty());
    	fileTypeColumn.setSortable(false);
    	companyCodeColumn.setCellValueFactory(cellData -> cellData.getValue().companyCodeProperty());
    	companyCodeColumn.setSortable(false);
    	personalCodeColumn.setCellValueFactory(cellData -> cellData.getValue().personalCodeProperty());
    	personalCodeColumn.setSortable(false);
    	filePageColumn.setCellValueFactory(cellData -> cellData.getValue().filePageProperty());
    	filePageColumn.setSortable(false);
    	scanTimeColumn.setCellValueFactory(cellData -> cellData.getValue().scanTimeProperty());
    	scanTimeColumn.setSortable(false);
    	remarkColumn.setCellValueFactory(cellData -> cellData.getValue().remarkProperty());
    	remarkColumn.setSortable(false);

		//加入 TableView 點選事件動作
    	uploadTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    	uploadTableView.setEditable(false);

    }

    public void stageShown() {
		String _logWhenSuccess = PropertiesCache.getInstance().getProperty(PropertiesCache.PROP_KEY.UPLOAD_LOG_WHEN_SUCCESS.propName());
		try {
			logWhenSuccess = Boolean.parseBoolean(_logWhenSuccess);
		} catch (Exception e) {
		}

		Stage stage = (Stage) UploadView.this.getScene().getWindow();
		this.titleText = stage.getTitle();

    	//Reset upload status
		if ( selectedIndex != null ) {
    		ScannedImage selectedItem = null;
    		for ( int i=0; i<this.imageList.size(); i++ ) {
    			ScannedImage item = this.imageList.get(i);
    			if ( i==selectedIndex ) {
        			item.uploadStatusProperty().setValue(UPLOAD_STATUS_READY);
        			item.stepProperty().setValue("uploadAll");
        			item.pageActionProperty().setValue("");
        			item.allowReservedProperty().setValue("");
        			selectedItem = item;
    			} else {
        			item.uploadStatusProperty().setValue(null);
    			}
    		}
        	uploadTableView.getItems().add(selectedItem);
		} else {
    		for ( ScannedImage item : this.imageList ) {
    			item.stepProperty().setValue("uploadAll");
    			item.uploadStatusProperty().setValue(UPLOAD_STATUS_READY);
    			item.pageActionProperty().setValue("");
    			item.allowReservedProperty().setValue("");
    		}
    		uploadTableView.getItems().addAll(this.imageList);
    		isUploadAll = true;
		}

    	if (logger.isDebugEnabled()) {
            logger.debug("Upload item count:{}", +uploadTableView.getItems().size());
    	}

    	//Set the right policy
    	uploadTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    	uploadTableView.getColumns().stream().forEach( (column) -> {
            //Minimal width = columnheader
            Text t = new Text(column.getText());
            double max = t.getLayoutBounds().getWidth();
            for ( int i = 0; i < uploadTableView.getItems().size(); i++ ) {
                //cell must not be empty
                if ( column.getCellData(i) != null ) {
                    t = new Text(column.getCellData(i).toString());
                    double calcwidth = t.getLayoutBounds().getWidth();
                    //remember new max-width
                    if ( calcwidth > max ) {
                        max = calcwidth;
                    }
                }
            }
            //set the new max-widht with some extra space
            column.setPrefWidth( max + 10.0d );
        } );
    	uploadTableView.prefHeightProperty().bind(UploadView.this.getScene().heightProperty());

    	cntSuccess.setValue(0);
    	cntUpload.setValue(uploadTableView.getItems().size());
    	cntFailed.setValue(0);
    	uploadIndex.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
		    	if (logger.isDebugEnabled()) {
					logger.debug("uploadIndex.changed({})", newValue);
		    	}
	            if (newValue.intValue()<0) {
	            	return;
	            } else if (newValue.intValue()==uploadTableView.getItems().size()) {
	            	uploadProcessDone();
	            	return;
	            }
				setTitle((newValue.intValue()+1) + "/" + uploadTableView.getItems().size());
	            startUpload(newValue.intValue(), false);
			}
        });
    	retryIndex.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
		    	if (logger.isDebugEnabled()) {
		            logger.debug("retryIndex.changed({})", newValue);
		    	}
	            startUpload(newValue.intValue(), true);
			}
        });
    	
    	lbSuccess.textProperty().bind(cntSuccess.asString());
    	lbUpload.textProperty().bind(cntUpload.asString());
    	lbFailed.textProperty().bind(cntFailed.asString());
    	uploadProgress.progressProperty().bind(
			Bindings.when(Bindings.equal(0, cntUpload))
				.then(0)
				.otherwise(cntSuccess.add(cntFailed).divide(cntUpload.doubleValue()))
		);

		setTitle("準備中");

		Platform.runLater(() -> {
			try {
				Thread.sleep(2000); // 等待畫面就緒
				uploadIndex.setValue(uploadIndex.get()+1);
			} catch (InterruptedException e) {
			}
		});
    }

    private void setTitle(String title) {
		Stage stage = (Stage) UploadView.this.getScene().getWindow();
		stage.setTitle(this.titleText + " - " + title);
    }

    private void startUpload(int index, boolean isRetryCase) {
    	if (logger.isDebugEnabled()) {
            logger.debug("startUpload({})", index);
    	}

    	ScannedImage imageItem = uploadTableView.getItems().get(index);
    	String fileType = imageItem.fileTypeProperty().get();
    	String fileCode = imageItem.fileCodeProperty().get();

    	//PCR: 244580 分檔頁無需上傳
    	if (Constant.SEP_FILE_TYPE.equals(fileType) || Constant.SEP_FILE_CODE.equals(fileCode)) {
    		if (logger.isDebugEnabled()) {
                logger.debug("Separator Page, Skipp Upload!");
    		}
        	cntSuccess.setValue(cntSuccess.getValue()+1);
        	imageItem.uploadStatusProperty().setValue(UPLOAD_STATUS_UPLOADED);
    		uploadIndex.setValue(uploadIndex.get()+1);
        	return;
    	}

    	UploadService service = new UploadService();
		service.setUploadUrl(this.scanConfig.getUploadURL());
		service.setScannedImage(imageItem);
		service.setOnReady(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
            	imageItem.uploadStatusProperty().setValue(UPLOAD_STATUS_READY);
            	if (logger.isDebugEnabled()) {
                    logger.debug("UploadService.onReady()");
            	}
            }
        });
		service.setOnRunning(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
            	imageItem.uploadStatusProperty().setValue(UPLOAD_STATUS_UPLOADING);
            	if (logger.isDebugEnabled()) {
                    logger.debug("UploadService.onRunning()");
            	}
            }
        });
		service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
            	UploadStatus status = (UploadStatus)t.getSource().getValue();
            	if (logger.isDebugEnabled()) {
                    logger.debug("UploadService.onSucceeded(), status:{}", (status == null ? "null" : status.toString()));
        		}

            	int state = 0;
            	if (!isRetryCase) {
                	state = checkUploadStatus(status, scanConfig, imageItem);
                	if (state>0) {
                		if (logger.isDebugEnabled()) {
                            logger.debug("Upload retry +1");
                		}
                		retryIndex.setValue(uploadIndex.get());
                		return;
                	}
            	}

        		if (status==null || status.getCode()==null || status.getCode()!=0) {
            		if (logger.isDebugEnabled()) {
                        logger.debug("Upload failed +1");
            		}

            		String message = scanConfig.getValueInTable("MSG-ERR-CODE-"+status.getCode(), status.getDescription());
            		UploadLog.appendFailed(imageItem, status.getCode() + ", " + message);
                	cntFailed.setValue(cntFailed.getValue()+1);
                	imageItem.uploadStatusProperty().setValue(UPLOAD_STATUS_FAILED);

                	if (!isUploadAll && state==0) { // 單筆, 上傳失敗, checkUploadStatus 回傳 0
						// Start: UAT-IR-479059，改在上傳完成刪除實體檔案及畫面記錄後，再顯示訊息
                		//String dialogTitle = scanConfig.getValueInTable(Constant.ERRINFO_DLG_TXT, "錯誤");
						//DialogUtil.showMessage(parent.getScene().getWindow(), dialogTitle, message, true);
                		dialogTitle = scanConfig.getValueInTable(Constant.ERRINFO_DLG_TXT, "錯誤");
                		dialogMessage = message;
                		// End: UAT-IR-479059
                	}
        		} else {
        			status.setDescription("上傳成功");
            		if (logger.isDebugEnabled()) {
                        logger.debug("Upload success +1");
            		}

            		if (logWhenSuccess) {
                		UploadLog.appendSuccess(imageItem);
            		}

            		cntSuccess.setValue(cntSuccess.getValue()+1);
                	imageItem.uploadStatusProperty().setValue(UPLOAD_STATUS_UPLOADED);

                	if (!isUploadAll && state==0) { // 單筆, 上傳成功, checkUploadStatus 回傳 0
						// Start: UAT-IR-479059，改在上傳完成刪除實體檔案及畫面記錄後，再顯示訊息
                    	//DialogUtil.showMessage(UploadView.this.getScene().getWindow(), "訊息", status.getDescription(), true);
                		dialogTitle = "訊息";
                		dialogMessage = status.getDescription();
                		// End: UAT-IR-479059
                	}
        		}

        		uploadIndex.setValue(uploadIndex.get()+1);
            }
        });
		service.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
        		EBaoException e = (EBaoException)t.getSource().getException();
            	String errorMessage = e.getMessage();
        		if (logger.isDebugEnabled()) {
                    logger.debug("UploadService.onFailed(), errorMessage:{}", errorMessage);
        		}
        		UploadLog.appendFailed(imageItem, errorMessage);
            	cntFailed.setValue(cntFailed.getValue()+1);
            	imageItem.uploadStatusProperty().setValue(UPLOAD_STATUS_FAILED);

            	if (!isUploadAll) { // 單筆, 上傳失敗
					// Start: UAT-IR-479059，改在上傳完成刪除實體檔案及畫面記錄後，再顯示訊息
            		//String dialogTitle = scanConfig.getValueInTable(Constant.ERRINFO_DLG_TXT, "錯誤");
					//DialogUtil.showMessage(parent.getScene().getWindow(), dialogTitle, errorMessage, true);
            		dialogTitle = scanConfig.getValueInTable(Constant.ERRINFO_DLG_TXT, "錯誤");
            		dialogMessage = errorMessage;
            		// End: UAT-IR-479059
            	}

            	uploadIndex.setValue(uploadIndex.get()+1);
            }
        });

    	if (logger.isDebugEnabled()) {
            logger.debug("Start to upload... index={}, imageFile={}", index, imageItem.fileNameProperty().get());
    	}

		service.start();
    }

	private int checkUploadStatus(UploadStatus status, ScanConfig scanConfig, ScannedImage imageItem) {
		int state = 0;

		if (null == status) {
			return state;
		}

		if (null == status.getCode()) {
			if (Constant.ERRCODE_IMAGE_DUPLICATE.equals(status.getDescription())) {
				String desc = scanConfig.getValueInTable(Constant.IMAGE_DUPLICATE, status.getDescription());
				status.setDescription(desc);
			} else if (Constant.ERRCODE_PARA_NOT_ENOUGH.equals(status.getDescription())) {
				String desc = scanConfig.getValueInTable(Constant.PARA_NOT_ENOUGH, status.getDescription());
				status.setDescription(desc);
			}
		} else {
			if (13 == status.getCode()) {
				String btnReplaceText = scanConfig.getValueInTable(Constant.PAGENO_BTN_REPLACE, Constant.PAGENO_BTN_REPLACE);
				String btnAfterText = scanConfig.getValueInTable(Constant.PAGENO_BTN_AFTER, Constant.PAGENO_BTN_AFTER);
				String btnCancelText = scanConfig.getValueInTable(Constant.BUTTON_CANCEL, Constant.BUTTON_CANCEL);

				String result = DialogUtil.showConfirm(parent.getScene().getWindow(), status.getDescription(), new String[] {btnReplaceText, btnAfterText, btnCancelText});
				if (btnReplaceText.equals(result)) {
					imageItem.pageActionProperty().setValue("REPLACED");
					state = 6;
				} else if (btnAfterText.equals(result)) {
					imageItem.pageActionProperty().setValue("PLACED");
					state = 7;
				} else {
					state = -1;
				}
			} else if (14 == status.getCode()) {
				String btnOkText = scanConfig.getValueInTable(Constant.BUTTON_OK, Constant.BUTTON_OK);
				String btnCancelText = scanConfig.getValueInTable(Constant.BUTTON_CANCEL, Constant.BUTTON_CANCEL);

				String result = DialogUtil.showConfirm(parent.getScene().getWindow(), status.getDescription(), new String[] {btnOkText, btnCancelText});
				if (btnOkText.equals(result)) {
					imageItem.allowReservedProperty().setValue("Y");
					state = 1;
				} else {
					state = -1;
				}
			} else if (15 == status.getCode()) {
				status.setDescription("未找到可被替換的影像");
			} else if (16 == status.getCode()) {
				status.setDescription("未找到可被插入的影像");
			} else if (13712 == status.getCode()) {
				status.setDescription("保單號碼不存在!");
			} else if (12324 == status.getCode()) {
				status.setDescription("參數傳輸不全!");
			} else if (400 == status.getCode()) {
				status.setDescription("保全還原件!");
			}
		}

		return state;
	}

	private void uploadProcessDone() {
		UploadLog.appendSummary(cntUpload.get(), cntSuccess.get(), cntFailed.get());
		setTitle("完成");
		// Start: UAT-IR-479059，改在上傳完成刪除實體檔案及畫面記錄後，再顯示訊息
		//if (!isUploadAll) {
		//	closeWindow();
		//}
		closeWindow();
		// End: UAT-IR-479059
	}

	private void closeWindow() {
		Platform.runLater(() -> {
			Stage stage = (Stage) UploadView.this.getScene().getWindow();
			stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
		});
	}

	public UploadProcessSummary getUploadProcessSummary() {
		return new UploadProcessSummary(
			cntSuccess.getValue(), 
			cntUpload.getValue(),
			cntFailed.getValue(), 
			dialogTitle, 
			dialogMessage
		);
	}

}
