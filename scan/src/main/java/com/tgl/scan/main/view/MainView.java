package com.tgl.scan.main.view;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.asprise.imaging.core.scan.twain.TwainConstants;
import com.asprise.imaging.core.scan.twain.TwainException;
import com.jfoenix.controls.JFXBadge;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXPopup.PopupHPosition;
import com.jfoenix.controls.JFXPopup.PopupVPosition;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbar.SnackbarEvent;
import com.jfoenix.controls.JFXSnackbarLayout;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTooltip;
import com.sun.jna.platform.DesktopWindow;
import com.tgl.scan.main.Constant;
import com.tgl.scan.main.bean.ImageRecordSet;
import com.tgl.scan.main.bean.LoginStatus;
import com.tgl.scan.main.bean.PageWarning;
import com.tgl.scan.main.bean.ScanConfig;
import com.tgl.scan.main.bean.ScannedImage;
import com.tgl.scan.main.bean.TiffRecord;
import com.tgl.scan.main.bean.TiffRecords;
import com.tgl.scan.main.bean.UploadProcessSummary;
import com.tgl.scan.main.http.EBaoException;
import com.tgl.scan.main.http.EbaoClient;
import com.tgl.scan.main.service.ImportService;
import com.tgl.scan.main.service.ScanService;
import com.tgl.scan.main.util.DialogUtil;
import com.tgl.scan.main.util.ImageRecordHelper;
import com.tgl.scan.main.util.ImageUtil;
import com.tgl.scan.main.util.ObjectsUtil;
import com.tgl.scan.main.util.PageNoValidator;
import com.tgl.scan.main.util.PropertiesCache;
import com.tgl.scan.main.util.ScanConfigUtil;
import com.tgl.scan.main.util.ScanUtil;
import com.tgl.scan.starter.AbstractFxView;

import jakarta.xml.bind.JAXBException;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.util.StringConverter;

public class MainView extends AbstractFxView {

	private static final Logger logger = LogManager.getLogger(MainView.class);

    private static final String ATTR_CONFIG_UPDATE_STATUS = "com.tgl.config.updatestatus";
    private static final String ATTR_SCAN_CREATE_TEMP_OWNER_FRAME = "com.tgl.scan.createTempOwnerFrame";
    private static final double DEFAULT_ZOOM_IN = 1.0;

	@FXML
    private StackPane root;
    @FXML
    private SplitPane splitPane;
    @FXML
    private ScrollPane previewScrollPane;
    @FXML
    private StackPane previewStackPane;
    @FXML
    private ImageView previewImageView;
    @FXML
    private ImageView logoImageView;
    @FXML
    private StackPane imageTableStackPane;

    // readonly table view
	@FXML
    private TableView<ScannedImage> imageTableView;
    @FXML
    private TableColumn<ScannedImage, Integer> indexNoColumn;
    @FXML
    private TableColumn<ScannedImage, String> scanOrderColumn;
    @FXML
    private TableColumn<ScannedImage, String> sendEmailColumn;
    @FXML
    private TableColumn<ScannedImage, String> isRemoteColumn;
    @FXML
    private TableColumn<ScannedImage, String> fileCodeColumn;
    @FXML
    private TableColumn<ScannedImage, String> mainFileTypeTextColumn;
    @FXML
    private TableColumn<ScannedImage, String> fileTypeTextColumn;
    @FXML
    private TableColumn<ScannedImage, String> companyCodeColumn;
    @FXML
    private TableColumn<ScannedImage, String> personalCodeColumn;
    @FXML
    private TableColumn<ScannedImage, String> filePageColumn;
    @FXML
    private TableColumn<ScannedImage, String> scanTimeColumn;
    @FXML
    private TableColumn<ScannedImage, String> actionTypeColumn;

    @FXML
    private JFXTextField txtOrgName; // 組織編碼
    @FXML
    private JFXTextField txtDeptName; // 部室名稱
	@FXML
	private JFXComboBox<Pair<String, String>> cbbMainFileType; // 影像主類型
	@FXML
	private JFXComboBox<Pair<String, String>> cbbFileType; // 影像子類型
    @FXML
    private JFXTextField txtFileCode; // 文件編號
    @FXML
    private JFXTextField txtFilePage; // 頁碼
	@FXML
	private JFXComboBox<String> cbbBoxNumber; // 箱號
    @FXML
	private JFXComboBox<Pair<String, String>> cbbBatchDeptType; // 批次號碼-部門別
    @FXML
    private JFXTextField txtBatchDate; // 日期
    @FXML
    private JFXTextField txtBatchArea; // 分區
    @FXML
    private JFXTextField txtBatchDocType; // 文件別
    @FXML
    private JFXTextField txtCompanyCode; // 公司碼
    @FXML
    private JFXTextField txtPersonalCode; // 個人碼
    @FXML
	private Label lbActionReplace;
    @FXML
	private JFXComboBox<Pair<String, String>> cbbActionReplace; // 替換
    @FXML
	private Label lbActionInsert;
    @FXML
	private JFXComboBox<Pair<String, String>> cbbActionInsert; // 插入
	@FXML
    private Label lbSendEmail;
	@FXML
	private JFXComboBox<Pair<String, String>> cbbSendEmail; // 是否發EMAIL
    @FXML
	private Label lbIsRemote;
    @FXML
	private JFXComboBox<Pair<String, String>> cbbIsRemote; // 視訊投保件
    @FXML
    private JFXTextField txtRemark; // 影像備註
    @FXML
    private Label lbDeptId;
	@FXML
    private Label lbCompanyCode;
	@FXML
	private JFXComboBox<Pair<String, String>> cbbDuplexMode; // 紙張來源
	@FXML
	private JFXComboBox<Pair<String, String>> cbbColorMode; // 影像模式
	@FXML
	private Hyperlink hlSourceName; // 預設掃描器名稱
    @FXML
    private JFXButton btnUpdate;
    @FXML
    private JFXButton btnRemove;
    @FXML
    private JFXButton btnRemovePartial;
    @FXML
    private JFXButton btnCopy;
    @FXML
    private JFXButton btnImport;
    @FXML
    private JFXButton btnScanSettings;
    @FXML
    private JFXButton btnScan;
    @FXML
    private JFXButton btnZoomIn;
    @FXML
    private JFXButton btnZoomOut;
    @FXML
    private JFXButton btnRotateRight;
    @FXML
    private JFXButton btnRotateLeft;
    @FXML
    private StackPane processPane;
    @FXML
    private Label processText;

	private JFXBadge uploadBadge;
	private JFXButton btnUploadBadge;
	private Label loginStatusLabel;
	private JFXButton btnLogin;
	private JFXButton btnRunningMode;
	private JFXButton btnSettings;
	private JFXPopup uploadPopup;
    private JFXSnackbar snackbar;
    private JFXTooltip btnLoginTooltip;
	private Label serverLabel;

    private String defaultScaner = null;
	private ScanConfig scanConfig;
	private boolean needToRelogin = false;
	private int loginStatus;
	private StringProperty loginStatusLabelText;
	private StringProperty loginStatusLabelCss;
	private StringProperty serverLabelText;

	private boolean launchParamFromWeb;
	private boolean launchParamQueryFromPage;
	private String launchParamReqToken;
	private String launchParamUserName;
	private String launchParamBoxNo;
	private String launchParamBatchDeptType;
	private String launchParamBatchDate;
	private String launchParamBatchArea;
	private String launchParamBatchDocType;

	private ImageRecordSet recordSet;
	private ImageRecordHelper recordSetHelper;

	public MainView() {
		super();
	}

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("UploadPopupView.fxml"));
        loader.setController(new UploadPopupController());
		try {
	        uploadPopup = new JFXPopup(loader.load());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.loginStatus = LoginStatus.STATUS_NOT_USER_LOGGIN;
		this.scanConfig = null;
		this.recordSet = null;
		this.loginStatusLabelText = new SimpleStringProperty(this, "loginStatusLabelText");
		this.loginStatusLabelCss = new SimpleStringProperty(this, "loginStatusLabelCss");
		this.recordSetHelper = ImageRecordHelper.getInstance();
		this.serverLabelText = new SimpleStringProperty(this, "serverLabelText");

		setupImageTableView();
    	setupTooltips();
        setupConverters();
        setupActions();
        setupOthers();
    }

    // Invoke from stage.setOnShown(...)
    public void stageShown(String launchParameters) {
		if (logger.isDebugEnabled()) {
			logger.debug("");
		}

    	parseLaunchParameters(launchParameters); // App 第一次開啟
    	imageTableStackPane.prefHeightProperty().bind(MainView.this.getScene().heightProperty());
    	hlSourceName.setText("*未設定");
    	hlSourceName.setStyle(Constant.SCANER_CSS_NOT_SET);
		loadCachedData(false);
        if (needToRelogin || null == scanConfig) {
        	onAction_btnLogin();
		} else {
			setupUIValues();
			checkImageDir();
		}
    }

    public void parseLaunchParameters(String launchParameters) {
		if (logger.isDebugEnabled()) {
			logger.debug("launchParameters={}", launchParameters);
		}

    	String[] parameters = launchParameters.split("\\|", 11);
    	boolean appLaunchAgain = "true".equals(parameters[1]);
    	boolean fromWeb = "true".equals(parameters[2]);
    	boolean fromQueryPage = "true".equals(parameters[3]);
    	String reqToken = "".equals(parameters[4]) ? null : parameters[4];
    	String userName = "".equals(parameters[5]) ? null : parameters[5];
    	String boxNo = "".equals(parameters[6]) ? null : parameters[6];
    	String batchDeptType = "".equals(parameters[7]) ? null : parameters[7];
    	String batchDate = "".equals(parameters[8]) ? null : parameters[8];
    	String batchArea = "".equals(parameters[9]) ? null : parameters[9];
    	String batchDocType = "".equals(parameters[10]) ? null : parameters[10];

		if (logger.isDebugEnabled()) {
			logger.debug("appLaunchAgain={}", appLaunchAgain);
			logger.debug("launchParamFromWeb={}, fromWeb={}", launchParamFromWeb, fromWeb);
			logger.debug("launchParamQueryFromPage={}, fromQueryPage={}", launchParamQueryFromPage, fromQueryPage);
			logger.debug("launchParamReqToken={}, reqToken={}", launchParamReqToken, reqToken);
			logger.debug("launchParamUserName={}, userName={}", launchParamUserName, userName);
			logger.debug("launchParamBoxNo={}, boxNo={}", launchParamBoxNo, boxNo);
			logger.debug("launchParamBatchDeptType={}, batchDeptType={}", launchParamBatchDeptType, batchDeptType);
			logger.debug("launchParamBatchDate={}, batchDate={}", launchParamBatchDate, batchDate);
			logger.debug("launchParamBatchArea={}, batchArea={}", launchParamBatchArea, batchArea);
			logger.debug("launchParamBatchDocType={}, batchDocType={}", launchParamBatchDocType, batchDocType);
		}

    	if (appLaunchAgain) {
    		// App 第二次被啟動

    		Stage stage = ((Stage)MainView.this.getScene().getWindow());
//    		stage.setAlwaysOnTop(true);
//    		stage.setAlwaysOnTop(false);
    		stage.requestFocus();
    		stage.toFront();

    		// 情境 A. 若 fromQueryPage=true(App第二次欲再由網頁帶箱號參數開啟) & launchParamQueryFromPage=false(App原由本機或網頁無參數開啟)
    		//		1. 詢問是否切換成[替換/插入]模式，若是，則進行後續處理，若否，則不處理維持原狀
    		//		2. 檢查 userName(網頁帶入) 與 launchParamUserName(原APP登入的UserName) 是否相同
    		//			2.1 若相同時，則維持原 launchParamUserName
    		//			2.1 若不同時，則要求以網頁帶入的 userName 重新登入
			// 情境 B. 若 fromQueryPage=false(App第二次欲再由本機或網頁無參數開啟) & launchParamQueryFromPage=true(App原由網頁帶箱號參數開啟)
    		//		1. 詢問是否切換成[標準]模式，若是，則進行後續處理，若否，則不處理維持原狀
    		//		2. 若 fromWeb=true，則檢查 userName(網頁帶入) 與 launchParamUserName(原APP登入的UserName) 是否相同
    		//			2.1 若相同時，則維持原 launchParamUserName
    		//			2.1 若不同時，則要求以網頁帶入的 userName 重新登入
    		// 情境 C. 若 fromQueryPage=true(App第二次欲再由網頁帶箱號參數開啟) & launchParamQueryFromPage=true(App原由網頁帶箱號參數開啟)
    		//		1. 檢查 userName(網頁帶入) 與 launchParamUserName(原APP登入的UserName) 是否相同
    		//			2.1 若相同時，則維持原 launchParamUserName，後續項目不進行處理
    		//			2.1 若不同時，則要求以網頁帶入的 userName 重新登入，後續處理如下
			// 情境 D. 若 fromQueryPage=false(App第二次欲再由本機或網頁無參數開啟) & launchParamQueryFromPage=false(App原由本機或網頁無參數開啟)
    		//		1. 若 fromWeb=true，則檢查 userName(網頁帶入) 與 launchParamUserName(原APP登入的UserName) 是否相同
    		//			2.1 若相同時，則維持原 launchParamUserName，後續項目不進行處理
    		//			2.1 若不同時，則要求以網頁帶入的 userName 重新登入，後續處理如下

    		// Step 1: 檢核是否需要切換模式
    		if (fromQueryPage != launchParamQueryFromPage) {
    			String msg = fromQueryPage ? "原APP已開啟且為[標準]模式，是否切換為[替換/插入]模式？" : "原APP已開啟且為[替換/插入]模式，是否切換為[標準]模式？";
				String result = DialogUtil.showConfirm(MainView.this.getScene().getWindow(), msg, new String[] {"是(切換)", "否"});

				if (logger.isDebugEnabled()) {
					logger.debug(msg + " --> " + result);
				}

				if ("否".equals(result)) {
					return;
				}
    		}

			String propUserName = PropertiesCache.getInstance().getProperty(PropertiesCache.PROP_KEY.EBAO_USER_NAME.propName());
			if (logger.isDebugEnabled()) {
				logger.debug("fromWeb={}, userName={}, launchParamUserName={}, propUserName={}, needToRelogin={}", fromWeb, userName, launchParamUserName, propUserName, needToRelogin);
			}

			// Step 2: 若為網頁無參數開啟或網頁帶箱號參數開啟，則檢核 UserName 是否相同
    		if (fromWeb) {
        		if (propUserName==null || "".equals(propUserName.trim())) {
        			needToRelogin = true;
        		} else if (!userName.equals(propUserName)) {
        			needToRelogin = true;
        			String msg = String.format("最近一次登入的使用者帳號 %s 與網頁帶入的使用者帳號 %s 不同，請以 %s 重新登入！", propUserName, userName, userName);
        			showSnackbarWithClose(msg);
        		}
    		} else {
    			if (propUserName==null || "".equals(propUserName)) {
        			needToRelogin = false;
        		}
    		}

    		// Step 3: 重設 launchParam* 變數
    		launchParamFromWeb = fromWeb;
    		launchParamQueryFromPage = fromQueryPage;
    		launchParamReqToken = reqToken;
    		launchParamUserName = userName;
    		launchParamBoxNo = boxNo;
    		launchParamBatchDeptType = batchDeptType;
    		launchParamBatchDate = batchDate;
    		launchParamBatchArea = batchArea;
    		launchParamBatchDocType = batchDocType;
			if (logger.isDebugEnabled()) {
				logger.debug("重設 launchParam* 變數");
			}

			if (this.scanConfig!=null) {
	    		this.scanConfig.setFromQueryPage(Boolean.toString(launchParamQueryFromPage));
				if (launchParamQueryFromPage) {
					this.scanConfig.resetDefValues(launchParamBoxNo, launchParamBatchDeptType, launchParamBatchDate, launchParamBatchArea, launchParamBatchDocType);
				}
			}

			if (needToRelogin) {
				if (this.loginStatus == LoginStatus.STATUS_LOGGED_IN) {
					EbaoClient.getInstance().close();
					this.loginStatus = LoginStatus.STATUS_OFF_LINE;
				}
	        	onAction_btnLogin();
			} else {
				setupUIValues();
			}
    	} else {
    		// App 第一次開啟
    		launchParamFromWeb = fromWeb;
    		launchParamQueryFromPage = fromQueryPage;
    		launchParamReqToken = reqToken;
    		launchParamUserName = userName;
    		launchParamBoxNo = boxNo;
    		launchParamBatchDeptType = batchDeptType;
    		launchParamBatchDate = batchDate;
    		launchParamBatchArea = batchArea;
    		launchParamBatchDocType = batchDocType;

			String propUserName = PropertiesCache.getInstance().getProperty(PropertiesCache.PROP_KEY.EBAO_USER_NAME.propName());
			if (logger.isDebugEnabled()) {
				logger.debug("launchParamFromWeb={}, launchParamUserName={}, propUserName={}", launchParamFromWeb, launchParamUserName, propUserName);
			}

			if (launchParamFromWeb) {
        		if (propUserName==null || "".equals(propUserName.trim())) {
        			needToRelogin = true;
        		} else if (!launchParamUserName.equals(propUserName)) {
        			needToRelogin = true;
        			String msg = String.format("最近一次登入的使用者帳號 %s 與網頁帶入的使用者帳號 %s 不同。請以 %s 重新登入！", propUserName, launchParamUserName, launchParamUserName);
        			showSnackbarWithClose(msg);
        		}
    		}
    	}
    }

    public float getDividerPosition() {
        ObservableList<Divider> dividers = splitPane.getDividers();
        return dividers.get(0).positionProperty().getValue().floatValue();
    }

    public void setDividerPosition(float dividerPosition) {
        ObservableList<Divider> dividers = splitPane.getDividers();
        dividers.get(0).positionProperty().setValue(dividerPosition);
    }

    private void onAction_btnUploadBadge() {
		Platform.runLater(() -> {
	    	uploadPopup.show(btnUploadBadge,
                PopupVPosition.TOP,
                PopupHPosition.RIGHT,
                -26,
                26);
		});
    }

    private void onAction_btnLogin() {
		Platform.runLater(() -> {
	    	showLogin();
		});
    }

    private void showLogin() {
    	LoginStatus status = DialogUtil.showLoginDialog(this, this.loginStatus, this.needToRelogin ? launchParamUserName : null);

		if (logger.isDebugEnabled()) {
			logger.debug("ProcessCode={}, launchParamQueryFromPage={}, launchParamBoxNo={}, launchParamBatchDeptType={}, launchParamBatchDate={}, launchParamBatchArea={}, launchParamBatchDocType={}", status.getProcessCode(), launchParamQueryFromPage, launchParamBoxNo, launchParamBatchDeptType, launchParamBatchDate, launchParamBatchArea, launchParamBatchDocType);
		}

    	if (LoginStatus.PROC_CODE_SUCCESS.equals(status.getProcessCode())) {
    		this.loginStatus = LoginStatus.STATUS_LOGGED_IN;
    		this.scanConfig = status.getConfig();
    		this.scanConfig.setFromQueryPage(Boolean.toString(launchParamQueryFromPage));
			if (launchParamQueryFromPage) {
				this.scanConfig.resetDefValues(launchParamBoxNo, launchParamBatchDeptType, launchParamBatchDate, launchParamBatchArea, launchParamBatchDocType);
			}
			serverLabelText.set(status.getServerName()==null || "".equals(status.getServerName().trim()) ? "N/A" : status.getServerName());
    		this.needToRelogin = false;
    	} else if (LoginStatus.PROC_CODE_PERMISSION_DENY.equals(status.getProcessCode())) {
		    Stage stage = (Stage)this.getScene().getWindow();
		    stage.close();
    	} else if (LoginStatus.PROC_CODE_ERROR.equals(status.getProcessCode()) || 
    	           LoginStatus.PROC_CODE_WRONG_ID_PWD.equals(status.getProcessCode()) || 
    	           LoginStatus.PROC_CODE_SYSTEM_ERROR.equals(status.getProcessCode())) {
    		if (this.loginStatus == LoginStatus.STATUS_LOGGED_IN) {
    			this.loginStatus = LoginStatus.STATUS_OFF_LINE;
    		}
    	} else if (LoginStatus.PROC_CODE_LOG_OUT.equals(status.getProcessCode())) {
 			this.loginStatus = LoginStatus.STATUS_OFF_LINE;
    	}

    	setupUIValues();
    }

    private boolean checkLogin() {
    	boolean isSuccess = false;
		if (this.loginStatus == LoginStatus.STATUS_LOGGED_IN) {
			EbaoClient eBaoClient = EbaoClient.getInstance();
			try {
				this.scanConfig = eBaoClient.getScanConfig();
				if (launchParamQueryFromPage) {
					this.scanConfig.resetDefValues(launchParamBoxNo, launchParamBatchDeptType, launchParamBatchDate, launchParamBatchArea, launchParamBatchDocType);
				}
				isSuccess = true;
			} catch (EBaoException e) {
				this.loginStatus = LoginStatus.STATUS_OFF_LINE;
				logger.error(e);
			}
		} 
		if (!isSuccess) {
			showLogin();
			if (this.loginStatus == LoginStatus.STATUS_LOGGED_IN) {
				isSuccess = true;
			}
		}
		return isSuccess;
    }

    private void onMouseDoubleClicked_previewStackPane() {
		Platform.runLater(() -> {
			if (null == previewImageView.getImage()) 
				return;
			if ( previewImageView.getImage().getWidth() > previewImageView.getImage().getHeight() ) {
				double originFitWidth = previewImageView.getFitWidth();
				double newFitWidth = previewScrollPane.getWidth();
				if (logger.isDebugEnabled()) {
					logger.debug("onMouseDoubleClicked(), fitWidth: {} -> {}, fitHeight: {}", originFitWidth, newFitWidth, previewImageView.getFitHeight());
				}
				previewImageView.setFitWidth(newFitWidth*DEFAULT_ZOOM_IN);
				previewImageView.setFitHeight(0);
			} else {
				double originFitHeight = previewImageView.getFitHeight();
				double newFitHeight = previewScrollPane.getHeight();
				if (logger.isDebugEnabled()) {
					logger.debug("onMouseDoubleClicked(), fitWidth: {}, fitHeight: {} -> {}", previewImageView.getFitWidth(), originFitHeight, newFitHeight);
				}
				previewImageView.setFitWidth(0);
				previewImageView.setFitHeight(newFitHeight*DEFAULT_ZOOM_IN);
			}
		});
    }

    private void onScroll_previewStackPane(ScrollEvent event) {
    	if (event.getDeltaY() > 0) {
        	onAction_btnZoomIn();
    	} else {
        	onAction_btnZoomOut();
    	}
    }

    private void onAction_btnSettings() {
		Platform.runLater(() -> {
//			DialogUtil.showMessage(MainView.this.getScene().getWindow(), "Settings");

//			processPane.setVisible(!processPane.isVisible());

//			String msg = String.format("最近一次登入的使用者帳號 %s 與網頁帶入的使用者帳號 %s 不同，請以 %s 重新登入！", 1, 2, 3);
//			showSnackbarWithClose(msg);

			List<DesktopWindow> windows = com.sun.jna.platform.WindowUtils.getAllWindows(true);

//			WinDef.HWND hwnd = com.sun.jna.platform.win32.User32.INSTANCE.FindWindow(null, Constant.APP_ID);
//			long wid = Pointer.nativeValue(hwnd.getPointer());

			JFrame frame = new JFrame();
			JFXPanel jfxPanel = new JFXPanel();
			frame.setSize(0, 0);
			frame.setContentPane(jfxPanel);
			frame.setAlwaysOnTop(true);
			frame.setUndecorated(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.show();
	    	String sourceName = ScanUtil.showSelectScanerDialog(frame);
	    	frame.dispose();

//	    	String sourceName = ScanUtil.showSelectScanerDialog(MainView.this.getScene().getWindow());

			int xxx = -1;
			
			
			
			
			
			
			
//			String libraryBuildInfo = Imaging.getLibraryBuildInfo();
//			String libraryVersion = Imaging.getLibraryVersion();
//			String libraryVersionNumberOnly = Imaging.getLibraryVersionNumberOnly();
//			Properties systemInfo = Imaging.getSystemInfo();
//			boolean twainLoaded = Imaging.isTwainLoaded();
//			Properties twainConfig = Imaging.getTwainConfig();
//			int twainConfigVersion = Imaging.getTwainConfigVersion();
//			String twainVersionLoaded = Imaging.getTwainVersionLoaded();
//			String twainVersionSystem = Imaging.getTwainVersionSystem();

//			long wp = ScanUtil.getWindowPointer((Stage)this.getScene().getWindow());
//			Window window = this.getScene().getWindow();
//			ObservableList<Window> windows = this.getScene().getWindow().getWindows();
//			List<com.sun.glass.ui.Window> windows2 = com.sun.glass.ui.Window.getWindows();
//			long nh = windows2.get(0).getNativeHandle();
//			long nw = windows2.get(0).getNativeWindow();
//			com.asprise.imaging.core.util.system.Utils.getHwnd(com.sun.glass.ui.Window.getWindows().get(0).getNativeWindow());
		});
    }

    private void onAction_btnRunningMode() {
		Platform.runLater(() -> {
			String msg = null;
			if (launchParamQueryFromPage) {
				msg = "目前為[替換/插入模式]，是由網頁點選箱號後開啟替換或插入影像的功能。是否要切換回[標準模式]？";
				String result = DialogUtil.showConfirm(MainView.this.getScene().getWindow(), msg, new String[] {"是(切換)", "否"});
				if ("否".equals(result)) {
					return;
				}
				launchParamQueryFromPage = false;
				if (this.scanConfig!=null) {
		    		this.scanConfig.setFromQueryPage(Boolean.FALSE.toString());
				}
		    	setupUIValues();
			} else {
				msg = "目前為[標準模式]，無法執行替換或插入影像的功能。";
				DialogUtil.showMessage(MainView.this.getScene().getWindow(), msg);
			}
		});
    }

    private void onAction_btnUpdate() {
		Platform.runLater(() -> {
			// 檢查 imagerecordset.xml 是否被更動過
			if (recordSetHelper.xmlFileChanged()) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "影像掃描設定檔被其他程式更動了，將重新載入設定檔！");
				loadCachedData(true);
				setupUIValues();
				return;
			}
			int selectedIndex = imageTableView.getSelectionModel().getSelectedIndex();

			storeScannedImage();

			// 回寫 XML 檔案
    		try {
    			recordSetHelper.marshalToFile(recordSet);
    		} catch (JAXBException e) {
    			String errMsg = String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
    			DialogUtil.showErrorMessageAndWait(MainView.this.getScene().getWindow(), errMsg);
				logger.error(errMsg, e);
    		}

    		// 重新載入 XML 檔案
    		setupTableData();
    		imageTableView.getSelectionModel().select(selectedIndex);
    		imageTableView.requestFocus();
		});
    }

    private void storeScannedImage() {
		List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
		int selectedIndex = imageTableView.getSelectionModel().getSelectedIndex();
		ScannedImage selectedItem = imageTableView.getSelectionModel().getSelectedItem();

		if (logger.isDebugEnabled() ) {
			logger.debug("setRecordValues(), selectedIndex={}", selectedIndex);
		}

		// 驗證欄位是否不正確
		boolean valid = (selectedIndex < 0) ? false : validateScannedImage(selectedItem);

		if (valid) {

			// 儲存畫面中的變更

			String fileType = null == cbbFileType.getSelectionModel().getSelectedItem() ? "" : cbbFileType.getSelectionModel().getSelectedItem().getKey();
	    	String oriFileCode = selectedItem.fileCodeProperty().getValue();
	    	String fileCode = txtFileCode.getText();
	    	String actionReplace = null==cbbActionReplace.getSelectionModel().getSelectedItem() ? null : cbbActionReplace.getSelectionModel().getSelectedItem().getKey();
	    	String actionInsert = null==cbbActionInsert.getSelectionModel().getSelectedItem() ? null : cbbActionInsert.getSelectionModel().getSelectedItem().getKey();
	    	String actionType = "";
	    	if (null != actionReplace && Constant.YN_YES.equals(actionReplace)) {
	    		actionType = "替換";
	    	} else if (null != actionInsert && Constant.YN_YES.equals(actionInsert)) {
	    		actionType = "插入";
	    	}

	    	selectedItem.orgNameProperty().setValue(txtOrgName.getText());
	    	selectedItem.deptNameProperty().setValue(txtDeptName.getText());
	    	selectedItem.mainFileTypeProperty().setValue( null == cbbMainFileType.getSelectionModel().getSelectedItem() ? "" : cbbMainFileType.getSelectionModel().getSelectedItem().getKey() );
	    	selectedItem.mainFileTypeTextProperty().setValue( null == cbbMainFileType.getSelectionModel().getSelectedItem() ? "" : cbbMainFileType.getSelectionModel().getSelectedItem().getValue() );
	    	selectedItem.fileTypeProperty().setValue(fileType);
	    	selectedItem.fileTypeTextProperty().setValue( null == cbbFileType.getSelectionModel().getSelectedItem() ? "" : cbbFileType.getSelectionModel().getSelectedItem().getValue() );
	    	selectedItem.fileCodeProperty().setValue(fileCode);
	    	selectedItem.filePageProperty().setValue(txtFilePage.getText());
	    	selectedItem.boxNoProperty().setValue(cbbBoxNumber.getSelectionModel().getSelectedItem());
	    	selectedItem.batchDepTypeProperty().setValue( null == cbbBatchDeptType.getSelectionModel().getSelectedItem() ? "" : cbbBatchDeptType.getSelectionModel().getSelectedItem().getKey() );
	    	selectedItem.batchDateProperty().setValue(txtBatchDate.getText());
	    	selectedItem.batchAreaProperty().setValue(txtBatchArea.getText());
	    	selectedItem.batchDocTypeProperty().setValue(txtBatchDocType.getText());
	    	selectedItem.companyCodeProperty().setValue(txtCompanyCode.getText());
	    	selectedItem.personalCodeProperty().setValue(txtPersonalCode.getText());
	    	selectedItem.actionReplaceProperty().setValue( null == cbbActionReplace.getSelectionModel().getSelectedItem() ? "" : cbbActionReplace.getSelectionModel().getSelectedItem().getKey() );
	    	selectedItem.actionInsertProperty().setValue( null == cbbActionInsert.getSelectionModel().getSelectedItem() ? "" : cbbActionInsert.getSelectionModel().getSelectedItem().getKey() );
	    	selectedItem.actionTypeProperty().setValue(actionType);
	    	selectedItem.sendEmailProperty().setValue( null == cbbSendEmail.getSelectionModel().getSelectedItem() ? "" : cbbSendEmail.getSelectionModel().getSelectedItem().getKey() );
	    	selectedItem.isRemoteProperty().setValue( null == cbbIsRemote.getSelectionModel().getSelectedItem() ? "" : cbbIsRemote.getSelectionModel().getSelectedItem().getKey() );
	    	selectedItem.remarkProperty().setValue(txtRemark.getText());
	    	selectedItem.recordStatusProperty().setValue("1");
	    	String maxPage = scanConfig.getMaxPageByCardCode(fileType);
	    	selectedItem.maxPageProperty().setValue( null == maxPage ? "" : maxPage );

	    	ScanUtil.setLastBoxNo(cbbBoxNumber.getSelectionModel().getSelectedItem());
	    	ScanUtil.setLastBatchArea(txtBatchArea.getText());

			recordSetHelper.saveTiffData(selectedItem, recordList.get(selectedIndex));

	    	// PCR 244580 BR-CMN-PIC-019 文件編號整批修改
	    	if ( ( scanConfig.isDeptPos() || "LA".equals(scanConfig.getBatchDepTypeValue() ) ) && 
	    		 !fileCode.equals(oriFileCode) ) { // 避免改動非文件編號欄位,也會更新所有文件編號
	    		if (logger.isDebugEnabled() ) {
	    			logger.debug("isDeptPos={}, batchDepTypeValue={}, newFileCode={}, oriFileCode={}", scanConfig.isDeptPos(), scanConfig.getBatchDepTypeValue(), fileCode, oriFileCode);
	    		}

	    		for ( int i=(selectedIndex+1); i<imageTableView.getItems().size(); i++ ) {
	    			ScannedImage item = imageTableView.getItems().get(i);
	    			String itemFileType = item.fileTypeProperty().getValue();
	    			if ( Constant.SEP_FILE_TYPE.equals(itemFileType) ) {
	    	    		if (logger.isDebugEnabled() ) {
	    	    			logger.debug("FileType is POSZ999, exist!");
	    	    		}
	    				break;
	    			}

	    			// 調整為該筆以下的FILE_CODE全部更新
	    			String itemFileCode = item.fileCodeProperty().getValue();
	    			item.fileCodeProperty().setValue(fileCode);
	        		if (logger.isDebugEnabled() ) {
	        			logger.debug("index={}, newFileCode={}, itemFileCode={}", i, fileCode, itemFileCode);
	        		}
	        		recordSetHelper.saveTiffData(item, recordList.get(i));
	    		}
	    	}

		}

    	// 更新掃瞄序號
		recordSetHelper.resetScanOrder(recordList);
    }

    private boolean validateScannedImage(ScannedImage selectedItem) {
    	String errorMessage = "";
    	String signature = selectedItem.signatureProperty().getValue();
    	String mainFileType = null == cbbMainFileType.getSelectionModel().getSelectedItem() ? null : cbbMainFileType.getSelectionModel().getSelectedItem().getKey();
    	String fileType = null == cbbFileType.getSelectionModel().getSelectedItem() ? null : cbbFileType.getSelectionModel().getSelectedItem().getKey();
    	String fileCode = txtFileCode.getText();
    	String filePage = txtFilePage.getText();
    	String companyCode = txtCompanyCode.getText();
    	String personalCode = txtPersonalCode.getText();

		if (logger.isDebugEnabled() ) {
			logger.debug("validateScannedImage(), signature={}, mainFileType={}, fileType={}, fileCode={}, filePage={}, companyCode={}, personalCode={}", signature, mainFileType, fileType, fileCode, filePage, companyCode, personalCode);
		}

    	if (Constant.MAINFILETYPE_GID.equals(mainFileType)) {
        	if (ObjectsUtil.isEmpty(companyCode) && ObjectsUtil.isEmpty(personalCode)) { // 公司碼、個人碼為空值
        		errorMessage += "請輸入公司碼或個人碼!\n";
        	} else {
        		if (ObjectsUtil.isNotEmpty(companyCode) && companyCode.length() != 8 ) {
            		errorMessage += "請輸入完整公司碼，長度應為8碼!\n";
        		}
        		if (ObjectsUtil.isNotEmpty(personalCode) && personalCode.length() != 6 ) {
            		errorMessage += "請輸入完整個人碼，長度應為6碼!\n";
        		}
        	}
    	}

    	if (ObjectsUtil.isEmpty(mainFileType)) {
    		errorMessage += "請輸入 影像主類型\n";
    	}
    	if (ObjectsUtil.isEmpty(fileType)) {
    		errorMessage += "請輸入 影像子類型\n";
    	}
    	if (ObjectsUtil.isEmpty(fileCode)) {
    		errorMessage += "請輸入 文件編號\n";
    	}
    	if (ObjectsUtil.isEmpty(filePage) && "N".equals(signature)) {
    		errorMessage += "請輸入 頁碼\n";
    	}
    	if (ObjectsUtil.isNotEmpty(filePage) && "Y".equals(signature)) {
    		errorMessage += "<簽名檔影像>請勿輸入頁碼!\n";
    	}

    	boolean valid = true;
    	if ( errorMessage.length()>0 ) {
    		valid = false;
    		DialogUtil.showErrorMessageAndWait(MainView.this.getScene().getWindow(), errorMessage);
    	}

    	return valid;
    }

    private void onAction_btnRemove() {
		Platform.runLater(() -> {
			int selectedIndex = imageTableView.getSelectionModel().getSelectedIndex();
			if (selectedIndex < 0) 
				return;

			ScannedImage removeItem = imageTableView.getSelectionModel().getSelectedItem();
			String errorMessage = null;

			// 刪除實體影像檔案
			String scanOrder = removeItem.scanOrderProperty().getValue();
			String fileCode = removeItem.fileCodeProperty().getValue();
			String fileName = removeItem.fileNameProperty().getValue();
			String fileURL = removeItem.fileURLProperty().getValue();
    		Path file = Paths.get(fileURL);
    		try {
				Files.delete(file);
			} catch (NoSuchFileException e) {
			} catch (IOException e) {
				errorMessage = String.format("序號:%s 文件編號:%s，無法刪除影像檔 %s ！", scanOrder, fileCode, fileName);
				logger.error(errorMessage, e);
			}

    		if (null == errorMessage) {
    			// 刪除記錄並回寫 XML 檔案
    			List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
    			recordList.remove(selectedIndex);
        		try {
        			recordSetHelper.marshalToFile(recordSet);
        		} catch (JAXBException e) {
    				errorMessage = String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
    				logger.error(errorMessage, e);
        		}
    		}

    		if (null == errorMessage) {
        		// 刪除 TableView 記錄
    			int rowCount = imageTableView.getItems().size();
    			imageTableView.getSelectionModel().clearSelection();
    			imageTableView.getItems().remove(selectedIndex);
    			// 選取下一筆
    			if (imageTableView.getItems().size() > 0) {
    				if ((rowCount-1)==selectedIndex) {
    					selectedIndex--;
    				}
    				imageTableView.getSelectionModel().select(selectedIndex);
    			} else {
        			setupButtonStatus();
    			}
    		} else {
    			DialogUtil.showErrorMessage(MainView.this.getScene().getWindow(), errorMessage);
    		}

    		imageTableView.requestFocus();
		});
    }

    private void onAction_btnRemovePartial() {
		Platform.runLater(() -> {
			int maxScanOrder = -1;
			int currentScanOrder = -1;
			String scanOrder = null;
			for (ScannedImage item : imageTableView.getItems()) {
				scanOrder = item.scanOrderProperty().getValue();
				Integer _scanOrderInt = null;
				try {
					_scanOrderInt = Integer.valueOf(scanOrder);
				} catch (Exception e) {
				}
				if (null != _scanOrderInt) {
					maxScanOrder = maxScanOrder < _scanOrderInt.intValue() ? _scanOrderInt.intValue() : maxScanOrder;
				}
			}

			ScannedImage selectedItem = imageTableView.getSelectionModel().getSelectedItem();
			if (null == selectedItem) {
				currentScanOrder = maxScanOrder;
			} else {
				scanOrder = selectedItem.scanOrderProperty().getValue();
				currentScanOrder = Integer.parseInt(scanOrder);
			}

	    	int from = DialogUtil.showRemoveRangeDialog(this.getScene().getWindow(), currentScanOrder);
	    	if (from == -1) {
	    		imageTableView.requestFocus();
	    		return;
	    	}

	    	int to;
	    	if (currentScanOrder < from) {
				// currentScanOrder:3 < from:5 
	    		to = from;
	    		from = currentScanOrder;
	    	} else {
				// currentScanOrder:10 >= from:5 
	    		to = currentScanOrder;
	    	}
	    	int next = to + 1;

			String btnOkText = scanConfig.getValueInTable(Constant.BUTTON_OK, Constant.BUTTON_OK);
			String btnCancelText = scanConfig.getValueInTable(Constant.BUTTON_CANCEL, Constant.BUTTON_CANCEL);

	    	// 確認是否刪除
			String msg = String.format("請確認整批刪除第%s至%s？", from, to);
			String result = DialogUtil.showConfirm(MainView.this.getScene().getWindow(), msg, new String[] {btnOkText, btnCancelText});
			if (btnCancelText.equals(result)) {
	    		imageTableView.requestFocus();
				return;
			}

			// 檢查檔案是否存在
	    	if (checkImageFileExist(from, to)) {
				result = DialogUtil.showConfirm(MainView.this.getScene().getWindow(), "刪除序號有非空白影像，請確認！", new String[] {btnOkText, btnCancelText});
				if (btnCancelText.equals(result)) {
		    		imageTableView.requestFocus();
					return;
				}
	    	}

	    	removePartial(from, to);

			imageTableView.getSelectionModel().clearSelection();
			for (ScannedImage item : imageTableView.getItems()) {
				scanOrder = item.scanOrderProperty().getValue();
				Integer _scanOrderInt = null;
				try {
					_scanOrderInt = Integer.valueOf(scanOrder);
				} catch (Exception e) {
				}
				if (null != _scanOrderInt && _scanOrderInt.intValue() >= next) {
					imageTableView.getSelectionModel().select(item);
					break;
				}
			}

			imageTableView.requestFocus();
		});
    }

    private boolean checkImageFileExist(int from, int to) {
    	boolean hasImageFile = false;
		int rowCount = imageTableView.getItems().size();

		for (int i=0; i<rowCount; i++) {
			ScannedImage removeItem = imageTableView.getItems().get(i);
			String scanOrder = removeItem.scanOrderProperty().getValue();
			String fileURL = removeItem.fileURLProperty().getValue();
			int _scanOrder = Integer.parseInt(scanOrder);
			if (_scanOrder < from || _scanOrder > to || ObjectsUtil.isEmpty(fileURL)) {
				continue;
			}

			if (logger.isDebugEnabled() ) {
				logger.debug("scanOrder={}, fileURL={}", scanOrder, fileURL);
			}

			// 檢查實體影像檔案是否存在
            Path path = Paths.get(fileURL);
			if (Files.exists(path)) {
				hasImageFile = true;
				break;
			}
		}

		if (logger.isDebugEnabled() ) {
			logger.debug("hasImageFile={}", hasImageFile);
		}

		return hasImageFile;
    }

    private void removePartial(int from, int to) {
		imageTableView.getSelectionModel().clearSelection();
		int rowCount = imageTableView.getItems().size();
		List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
		List<String> removeFailedItems = new ArrayList<String>();
		int removeCount = 0;

		for (int i=rowCount-1; i>=0; i--) {
			ScannedImage removeItem = imageTableView.getItems().get(i);
			String scanOrder = removeItem.scanOrderProperty().getValue();
			int _scanOrder = Integer.parseInt(scanOrder);
			if (_scanOrder < from || _scanOrder > to) {
				continue;
			}

			String itemDetail = null;

			// 刪除實體影像檔案
			String fileCode = removeItem.fileCodeProperty().getValue();
			String fileName = removeItem.fileNameProperty().getValue();
			String fileURL = removeItem.fileURLProperty().getValue();
    		Path file = Paths.get(fileURL);
    		try {
				Files.delete(file);
			} catch (NoSuchFileException e) {
			} catch (IOException e) {
				itemDetail = String.format("● 序號:%s 文件編號:%s 影像檔:%s", scanOrder, fileCode, fileName);
				logger.error(itemDetail, e);
			}

    		if (null == itemDetail) {
    			recordList.remove(i);
    			imageTableView.getItems().remove(i);
    			removeCount++;
    		} else {
    			removeFailedItems.add(itemDetail);
    		}
		}

		String errorMessage = "";
		for (int i=removeFailedItems.size()-1; i>=0; i--) {
			String itemDetail = removeFailedItems.get(i);
			errorMessage += itemDetail + "\n";
		}
		if (errorMessage.length() > 0) {
			errorMessage = "無法刪除下列影像檔：\n\n" + errorMessage;
		}

		if (removeCount > 0) {
			try {
				// 刪除記錄並回寫 XML 檔案
				recordSetHelper.marshalToFile(recordSet);
			} catch (JAXBException e) {
				errorMessage = errorMessage + "\n" + String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
				logger.error(errorMessage, e);
			}
			setupButtonStatus();
		}

		if (errorMessage.length() > 0) {
			DialogUtil.showErrorMessage(MainView.this.getScene().getWindow(), errorMessage);
		}
    }

    private void onAction_btnCopy() {
		Platform.runLater(() -> {
			int selectedIndex = imageTableView.getSelectionModel().getSelectedIndex();
			if (selectedIndex < 0) 
				return;

			String errorMessage = null;
			Path newFilePath = null;
			TiffRecord newTiffRecord = null;
			ScannedImage copyItem = imageTableView.getSelectionModel().getSelectedItem();
			String fileName = copyItem.fileNameProperty().getValue().toLowerCase();
			String copyFileName = "_copy" + Constant.FILE_EXT_TIFF;
			if ( fileName.endsWith(Constant.FILE_EXT_JPG) || fileName.endsWith(Constant.FILE_EXT_JPEG) ) {
				copyFileName = "_copy" + Constant.FILE_EXT_JPG;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String newFileName = sdf.format(new Date())+copyFileName;
			String newFileURL = ImageRecordHelper.IMAGE_ARCHIVE_DIR + File.separator + newFileName;

			try {
				newFilePath = ImageUtil.copyImageItem(copyItem, newFileURL);
			} catch (NoSuchFileException e) {
				errorMessage = String.format("複製失敗！找不到影像檔 %s 。", copyItem.fileNameProperty().getValue());
				logger.error(errorMessage, e);
			} catch (IOException e) {
				errorMessage = String.format("無法複製影像檔 %s ！", copyItem.fileNameProperty().getValue());
				logger.error(errorMessage, e);
			}

			if (null == errorMessage) {
    			// 複製記錄並回寫 XML 檔案
    			List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
    			TiffRecord tiffRecord = recordList.get(selectedIndex);
        		try {
        			newTiffRecord = recordSetHelper.cloneTiffRecord(tiffRecord, newFilePath.getFileName().toString());
        			recordList.add(selectedIndex+1, newTiffRecord);
        			recordSetHelper.marshalToFile(recordSet);
        		} catch (IllegalAccessException e) {
    				errorMessage = "影像設定讀取失敗！";
    				logger.error(errorMessage, e);
				} catch (InvocationTargetException e) {
    				errorMessage = "影像設定抄寫失敗！";
    				logger.error(errorMessage, e);
        		} catch (JAXBException e) {
    				errorMessage = String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
    				logger.error(errorMessage, e);
				}
    		}

    		if (null == errorMessage) {
    			// 加入 TableView 複製的新記錄
				ScannedImage newItem = recordSetHelper.convert(newTiffRecord);
				newItem.indexNoProperty().setValue(copyItem.indexNoProperty().getValue()+1);
    			imageTableView.getSelectionModel().clearSelection();
    			imageTableView.getItems().add(selectedIndex+1, newItem);
    			// 選取複製的新記錄
				imageTableView.getSelectionModel().select(selectedIndex+1);
    		} else {
    			DialogUtil.showErrorMessage(MainView.this.getScene().getWindow(), errorMessage);
    		}

    		imageTableView.requestFocus();
		});
    }

    private void onAction_btnImport() {

		Platform.runLater(() -> {
			FileChooser fileChooser = new FileChooser();	
			fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("TIFF Files", "*.tif", "*.tiff"), //開啟 FileChooser 限制為 tiff/tif 檔案才行使用
				new FileChooser.ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg"),  //開啟 FileChooser 限制為 jpeg/jpg 檔案才行使用
				new FileChooser.ExtensionFilter("TIFF & JPEG Files", "*.tif", "*.tiff", "*.jpg", "*.jpeg") //開啟 FileChooser 限制為 tiff/tif & jpeg/jpg 檔案才行使用
			);
			File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());
			if (null == selectedFile) {
				return;
			}

        	showProcessingPane("影像導入中");

        	//取得 TableView 上最後一筆資料
			ScannedImage latestImage = null;
			int rowCount = imageTableView.getItems().size();
			if (rowCount > 0) {
				latestImage = imageTableView.getItems().get(rowCount-1);
			}

			// 與UI Thread區隔，以另一個Thread處理
			ImportService service = new ImportService();
			service.setQueryFromPage(launchParamQueryFromPage);
			service.setScanConfig(this.scanConfig);
			service.setLatestImage(latestImage);
			service.setSelectedFile(selectedFile);
	        service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
	            @Override
	            public void handle(WorkerStateEvent t) {
	            	int latestImageIndexNo = ((ImportService)t.getSource()).getLatestImageIndexNo();
					@SuppressWarnings("unchecked")
					List<TiffRecord> importedRecordList = (List<TiffRecord>)t.getSource().getValue();
	        		if (logger.isDebugEnabled()) {
	        			logger.debug("Image imported! " + importedRecordList);
	        		}

	    			String errorMessage = null;

	    			if (importedRecordList != null) {
		    			List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
	    				for (TiffRecord newTiffRecord : importedRecordList) {
	    					recordList.add(newTiffRecord);
	    				}

		    			try {
		    				recordSetHelper.marshalToFile(recordSet);
		        		} catch (JAXBException e) {
		    				errorMessage = String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
		    				logger.error(errorMessage, e);
						}
	    			}

	            	hideProcessingPane();

	            	if (null == errorMessage) {
		    			// 加入 TableView 複製的新記錄
		    			if (importedRecordList != null) {
		    				for (TiffRecord newTiffRecord : importedRecordList) {
		    					ScannedImage newItem = recordSetHelper.convert(newTiffRecord);
		    					newItem.indexNoProperty().setValue(++latestImageIndexNo);
		    	    			imageTableView.getItems().add(newItem);
		    				}
		        			// 選取複製的新記錄
		    				imageTableView.getSelectionModel().select(imageTableView.getItems().size());
		    				imageTableView.requestFocus();
		    			}
		    		} else {
		    			DialogUtil.showErrorMessage(MainView.this.getScene().getWindow(), errorMessage);
		    		}
	            }
	        });
	        service.setOnFailed(new EventHandler<WorkerStateEvent>() {
	            @Override
	            public void handle(WorkerStateEvent t) {
	            	hideProcessingPane();
	            	String errorMessage = t.getSource().getException().getMessage();
	        		if (logger.isDebugEnabled()) {
	        			logger.debug("Image import failed! " + errorMessage);
	        		}
	    			DialogUtil.showErrorMessage(MainView.this.getScene().getWindow(), errorMessage);
	            }
	        });

	        service.start();
		});
    }

    private void onAction_btnScan() {
		Platform.runLater(() -> {
			if ( ObjectsUtil.isEmpty(defaultScaner) ) {
				selectScaner();
				if ( ObjectsUtil.isEmpty(defaultScaner) ) {
					List<String> sourceList = ScanUtil.getSourceNameList();
					String message = ( sourceList==null || sourceList.size()==0 ) ? "找不到掃描器！請確認電源是否開啟。" : "未選取掃描器！";
	    			DialogUtil.showErrorMessageAndWait(MainView.this.getScene().getWindow(), message);
					return;
				}
			}

			String colorMode = null == cbbColorMode.getSelectionModel().getSelectedItem() ? null : cbbColorMode.getSelectionModel().getSelectedItem().getKey();
	    	String duplexMode = null == cbbDuplexMode.getSelectionModel().getSelectedItem() ? null : cbbDuplexMode.getSelectionModel().getSelectedItem().getKey();
	    	if ( ObjectsUtil.isEmpty(colorMode) || ObjectsUtil.isEmpty(duplexMode) ) {
    			DialogUtil.showErrorMessageAndWait(MainView.this.getScene().getWindow(), "紙張來源或影像模式未選擇！");
    			return;
	    	}

			showProcessingPane("影像處理中");

        	//取得 TableView 上最後一筆資料
			ScannedImage latestImage = null;
			int rowCount = imageTableView.getItems().size();
			if (rowCount > 0) {
				latestImage = imageTableView.getItems().get(rowCount-1);
			}

			// 與UI Thread區隔，以另一個Thread處理
			ScanService service = new ScanService();
			service.setSourceName(defaultScaner);
			service.setColorMode(colorMode);
			service.setDuplexMode(duplexMode);
			service.setQueryFromPage(launchParamQueryFromPage);
			service.setScanConfig(this.scanConfig);
			service.setLatestImage(latestImage);

			String str = System.getProperty(ATTR_SCAN_CREATE_TEMP_OWNER_FRAME, "false");
	    	boolean isCreateTempOwnerFrame = "true".equals(str) ? true : false;
	    	if (isCreateTempOwnerFrame) {
				service.setOwnerFrame(createTempOwnerFrame(false));
	    	}

	        service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
	            @Override
	            public void handle(WorkerStateEvent t) {
	            	java.awt.Window ownerFrame = ((ScanService)t.getSource()).getOwnerFrame();
	            	if (ownerFrame!=null) {
		            	ownerFrame.dispose();
	            	}

	            	int latestImageIndexNo = ((ScanService)t.getSource()).getLatestImageIndexNo();
					@SuppressWarnings("unchecked")
					List<TiffRecord> importedRecordList = (List<TiffRecord>)t.getSource().getValue();
	        		if (logger.isDebugEnabled()) {
	        			logger.debug("Scan completed! " + importedRecordList);
	        		}

	    			String errorMessage = null;

	    			if (importedRecordList != null) {
		    			List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
	    				for (TiffRecord newTiffRecord : importedRecordList) {
	    					recordList.add(newTiffRecord);
	    				}

		    			try {
		    				recordSetHelper.marshalToFile(recordSet);
		        		} catch (JAXBException e) {
		    				errorMessage = String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
		    				logger.error(errorMessage, e);
						}
	    			}

	            	hideProcessingPane();

	            	if (null == errorMessage) {
		    			// 加入 TableView 複製的新記錄
		    			if (importedRecordList != null) {
		    				for (TiffRecord newTiffRecord : importedRecordList) {
		    					ScannedImage newItem = recordSetHelper.convert(newTiffRecord);
		    					newItem.indexNoProperty().setValue(++latestImageIndexNo);
		    	    			imageTableView.getItems().add(newItem);
		    				}
		        			// 選取複製的新記錄
		    				imageTableView.getSelectionModel().select(imageTableView.getItems().size());
		    			}
		    		} else {
		    			DialogUtil.showErrorMessage(MainView.this.getScene().getWindow(), errorMessage);
		    		}
	            }
	        });
	        service.setOnFailed(new EventHandler<WorkerStateEvent>() {
	            @Override
	            public void handle(WorkerStateEvent t) {
	            	java.awt.Window ownerFrame = ((ScanService)t.getSource()).getOwnerFrame();
	            	if (ownerFrame!=null) {
		            	ownerFrame.dispose();
	            	}

	            	hideProcessingPane();
	            	String errorMessage = "無法開始掃描！ " + t.getSource().getException().getMessage();
	    			DialogUtil.showErrorMessage(MainView.this.getScene().getWindow(), errorMessage);
	            }
	        });

	        service.start();
		});
    }

    private void onAction_btnScanSettings() {
		Platform.runLater(() -> {
			if ( ObjectsUtil.isEmpty(defaultScaner) ) {
				selectScaner();
				if ( ObjectsUtil.isEmpty(defaultScaner) ) {
					List<String> sourceList = ScanUtil.getSourceNameList();
					String message = ( sourceList==null || sourceList.size()==0 ) ? "找不到掃描器！請確認電源是否開啟。" : "未選取掃描器！";
	    			DialogUtil.showErrorMessageAndWait(MainView.this.getScene().getWindow(), message);
					return;
				}
			}

    		String str = System.getProperty(ATTR_SCAN_CREATE_TEMP_OWNER_FRAME, "false");
	    	boolean isCreateTempOwnerFrame = "true".equals(str) ? true : false;

    		if (logger.isDebugEnabled()) {
    			logger.debug("Scaner settings, source={}, isCreateTempOwnerFrame={}", defaultScaner, isCreateTempOwnerFrame);
    		}

    		com.asprise.imaging.core.Result result = null;
    		JFrame ownerFrame = null;

    		try {
        		// 以下列程式碼，取得的 Window Handle 傳給 Asprise Imaging 物件使用時有問題，掃描的對話視窗一直沒出現
				//result = ScanUtil.setupSource(MainView.this.getScene().getWindow(), defaultScaner);

    	    	if (!isCreateTempOwnerFrame) {
        			// 替代作法1: 以下列程式碼，可以顯示掃描的對話視窗，但不會顯示在最上層，會被其他視窗遮蔽
    				result = ScanUtil.setupSource(defaultScaner);
    	    	} else {
    	        	// 替代作法2: 以下列程式碼，建立一個隱形的JFrame視窗，這個方法可以顯示掃描的對話視窗，但系統是否穩定、記憶體使用狀況...等，需再測試確認
    	        	ownerFrame = createTempOwnerFrame(false);
    				result = ScanUtil.setupSource(ownerFrame, defaultScaner);
    	    	}

        		if (logger.isDebugEnabled()) {
    				logger.debug(result == null ? "(null)" : result.toJson(true));
        		}
			} catch (TwainException e) {
				DialogUtil.showErrorMessageAndWait(MainView.this.getScene().getWindow(), "無法設定掃描器！ " + e.getMessage());
			} finally {
	        	ownerFrame.dispose();
			}
		});
    }

    private void onAction_hlSourceName() {
		Platform.runLater(() -> {
			selectScaner();
		});
    }

    private JFrame createTempOwnerFrame(boolean overlap) {
		JFXPanel jfxPanel = new JFXPanel();

    	JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setContentPane(jfxPanel);
		frame.setAlwaysOnTop(true);
		frame.setUndecorated(true);

    	javafx.stage.Window win = MainView.this.getScene().getWindow();
		int x = (int)win.getX();
		int y = (int)win.getY();
		int width = (int)win.getWidth();
		int height = (int)win.getHeight();

		if (logger.isDebugEnabled()) {
			logger.debug("overlap={}, x={}, y={}, width={}, height={}", overlap, x, y, width, height);
		}

		if (overlap) {
	    	//建立一個隱形的 JFrame 視窗, 疊在 ScanApp 上層 (選取掃描機時可以使用，但掃描及顯示掃描設置視窗時會被遮蔽)
			StackPane stack = new StackPane();
	        Scene scene = new Scene(stack, width, height);
	        Text hello = new Text("Hello");
	        hello.setVisible(false);
			stack.getChildren().add(hello);
	    	jfxPanel.setScene(scene);
			frame.setOpacity(0.0f);
			frame.setSize(width, height);
		}

		frame.setLocation(x, y);
		frame.pack();
		frame.setVisible(true);

		return frame;
    }

    private void selectScaner() {
    	String sourceName = null;

    	String str = System.getProperty(ATTR_SCAN_CREATE_TEMP_OWNER_FRAME, "false");
    	boolean isCreateTempOwnerFrame = "true".equals(str) ? true : false;

		if (logger.isDebugEnabled()) {
			logger.debug("isCreateTempOwnerFrame={}", isCreateTempOwnerFrame);
		}

    	// 以下列程式碼，取得的 Window Handle 傳給 Asprise Imaging 物件使用時有問題，掃描的對話視窗一直沒出現
    	//sourceName = ScanUtil.showSelectScanerDialog(MainView.this.getScene().getWindow());

    	if (!isCreateTempOwnerFrame) {
        	// 替代作法1: 以下列程式碼，可以顯示掃描的對話視窗，但不會顯示在最上層，會被其他視窗遮蔽
        	sourceName = ScanUtil.showSelectScanerDialog();
    	} else {
        	// 替代作法2: 以下列程式碼，建立一個隱形的JFrame視窗，這個方法可以顯示掃描的對話視窗，但系統是否穩定、記憶體使用狀況...等，需再測試確認
        	JFrame ownerFrame = createTempOwnerFrame(true);
        	sourceName = ScanUtil.showSelectScanerDialog(ownerFrame);
        	ownerFrame.dispose();
    	}

    	if ( ObjectsUtil.isNotEmpty(sourceName) ) {
			hlSourceName.setText(sourceName);
			hlSourceName.setStyle(Constant.SCANER_CSS_SET);
			defaultScaner = sourceName;
		}
    }

    private void onAction_btnZoomIn() {
		Platform.runLater(() -> {
			if (null == previewImageView.getImage()) 
				return;
			if ( previewImageView.getImage().getWidth() > previewImageView.getImage().getHeight() ) {
				double originFitWidth = previewImageView.getFitWidth();
				double newFitWidth = originFitWidth * 1.1;
				if (logger.isDebugEnabled()) {
					logger.debug("btnZoomIn(), fitWidth: {} -> {}, fitHeight: {}", originFitWidth, newFitWidth, previewImageView.getFitHeight());
				}
				previewImageView.setFitWidth(newFitWidth);
				previewImageView.setFitHeight(0);
			} else {
				double originFitHeight = previewImageView.getFitHeight();
				double newFitHeight = originFitHeight * 1.1;
				if (logger.isDebugEnabled()) {
					logger.debug("btnZoomIn(), fitWidth: {}, fitHeight: {} -> {}", previewImageView.getFitWidth(), originFitHeight, newFitHeight);
				}
				previewImageView.setFitWidth(0);
				previewImageView.setFitHeight(newFitHeight);
			}
		});
    }

    private void onAction_btnZoomOut() {
		Platform.runLater(() -> {
			if (null == previewImageView.getImage()) 
				return;
			if ( previewImageView.getImage().getWidth() > previewImageView.getImage().getHeight() ) {
				double originFitWidth = previewImageView.getFitWidth();
				double newFitWidth = originFitWidth * 0.9;
				if (logger.isDebugEnabled()) {
					logger.debug("btnZoomOut(), fitWidth: {} -> {}, fitHeight: {}", originFitWidth, newFitWidth, previewImageView.getFitHeight());
				}
				previewImageView.setFitWidth(newFitWidth);
				previewImageView.setFitHeight(0);
			} else {
				double originFitHeight = previewImageView.getFitHeight();
				double newFitHeight = originFitHeight * 0.9;
				if (logger.isDebugEnabled()) {
					logger.debug("btnZoomOut(), fitWidth: {}, fitHeight: {} -> {}", previewImageView.getFitWidth(), originFitHeight, newFitHeight);
				}
				previewImageView.setFitWidth(0);
				previewImageView.setFitHeight(newFitHeight);
			}
		});
    }

    private void onAction_btnRotateRight() {
		Platform.runLater(() -> {
			if (null == previewImageView.getImage()) 
				return;
			rotateImage(90); //向右旋轉90度
		});
    }

    private void onAction_btnRotateLeft() {
    	Platform.runLater(() -> {
			if (null == previewImageView.getImage()) 
				return;
			rotateImage(270); //向左旋轉90度
		});
    }

    private void rotateImage(int rotation) {
//    	showSnackbar("影像處理中..."); //不受控先關掉

		double originFitWidth = previewImageView.getFitWidth();
		double originFitHeight = previewImageView.getFitHeight();

		if (logger.isDebugEnabled()) {
			logger.debug("rotateImage(), rotation={}, originFitWidth={}, originFitHeight={}", rotation, originFitWidth, originFitHeight);
		}

		Image newImage = null;
		try {
			ScannedImage simage = imageTableView.getSelectionModel().getSelectedItem();
			BufferedImage rotateBuffImage = ImageUtil.rotateAndWriteImage(simage, rotation);
			newImage = ImageUtil.bufferedImageToFxImage(rotateBuffImage);
		} catch (IOException e) {
			DialogUtil.showErrorMessage(MainView.this.getScene().getWindow(), "影像檔存檔失敗！\n"+e.getMessage());
		}

		if ( newImage != null ) {
			previewImageView.setImage(newImage);
			if ( newImage.getWidth() > newImage.getHeight() ) {
				previewImageView.setFitWidth(previewScrollPane.getWidth());
				previewImageView.setFitHeight(0);
			} else {
				previewImageView.setFitWidth(0);
				previewImageView.setFitHeight(previewScrollPane.getHeight());
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Set to rotated image! newImage.width={}, newImage.height={}, previewImageView.fitWidth={}, previewImageView.fitHeight={}", newImage.getWidth(), newImage.getHeight(), previewImageView.getFitWidth(), previewImageView.getFitHeight());
			}
		}

//    	hideSnackbar(); //不受控先關掉
    }

    private void onAction_btnUpload() {
		Platform.runLater(() -> {
		    String updateStatus = System.getProperty(ATTR_CONFIG_UPDATE_STATUS, "");
		    if (!"AppIsUpToDate".equals(updateStatus)) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "無法上傳檔案！因為掃描程式啟動時您略過了版本檢查，無法確認程式是否為最新版本。請關閉並重新啟動掃描程式，讓程式檢查並更新為最新版本。");
				return;
		    }

			List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
			int selectedIndex = imageTableView.getSelectionModel().getSelectedIndex();
			ScannedImage selectedItem = imageTableView.getSelectionModel().getSelectedItem();
			if (selectedIndex<0) {
				DialogUtil.showMessageAndWait(MainView.this.getScene().getWindow(), "請選擇欲上傳的影像");
				return;
			}

			// 檢查 imagerecordset.xml 是否被更動過
			if (recordSetHelper.xmlFileChanged()) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "影像掃描設定檔被其他程式更動了，將重新載入設定檔！");
				loadCachedData(true);
				setupUIValues();
				return;
			}

			// 檢查影像檔是否存在
			if (imageFileNotExist()) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "影像檔已經被移除，無法上傳檔案！\nScan files have been removed from disk! It can not be uploaded!");
				return;
			}

			boolean isUploadAll = false; // 上傳指定的一個影像檔

			// 欄位檢核，檢核 FilePage 應為數字
			// 不需要實作，TextFormatter 已過濾

			// BR-CMN-PIC-016 檢核是否可替換, 由Server Side檢核
			// BR-CMN-PIC-017 檢核是否輸入批次號碼	
			boolean isBatchNoValid = validateBatchNo(isUploadAll);
			if (!isBatchNoValid) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "批次號碼未輸入！");
				return;
			}

			// BR-CMN-PIC-XXX 檢核是否輸入箱號
			boolean isBoxNoValid = validateBoxNo(isUploadAll);
			if (!isBoxNoValid) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "箱號未輸入！");
				return;
			}

			if (scanConfig.isDeptNB()) {
				// PCR_386372 - 掃瞄上傳啟動核保完成收到補全件Email通知
				boolean sendEmailValid = validateSendEmail(isUploadAll);
				if (!sendEmailValid) {
					return;
				}

				// PCR_268354 - 自動核保及核保功能優化需求(視訊投保件)
				boolean isRemoteValid = validateIsRemote(isUploadAll);
				if (!isRemoteValid) {
					return;
				}
			}  

			// BR-CMN-PIC-005 檢核頁碼是否匹配
			boolean isFilePageValid = validateFilePage(isUploadAll);
			if (!isFilePageValid) {
				return;
			}

			selectedItem.stepProperty().set("upload");
			// 將 ScanConfig 資訊寫回 imagerecord
			//fillScanConfig(scanConfig, selectedItem); // UAT-IR-478019 掃描帳號與上傳帳號不同時，上傳人員、部室名稱、批次號碼應帶入掃描人員資料
			recordSetHelper.saveTiffData(selectedItem, recordList.get(selectedIndex));

			// 回寫 XML 檔案
    		try {
    			recordSetHelper.marshalToFile(recordSet);
    		} catch (JAXBException e) {
    			String errMsg = String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
    			DialogUtil.showErrorMessageAndWait(MainView.this.getScene().getWindow(), errMsg);
				logger.error(errMsg, e);
				return;
    		}

			// 檢查必需登入 eBao Server
			if (!checkLogin()) {
				this.showSnackbar("未登入或離線使用時無法上傳檔案，請登入 eBao Server！", true, Duration.seconds(5.0));
				return;
			}

			UploadProcessSummary uploadSummary = DialogUtil.showUploadDialog(MainView.this, this.scanConfig, imageTableView.getItems(), selectedIndex);
			if (logger.isDebugEnabled()) {
				logger.debug("dialogTitle={}, dialogMessage={}, cntSuccess={}, cntUpload={}, cntFailed={}", uploadSummary.getDialogTitle(), uploadSummary.getDialogMessage(), uploadSummary.getCntSuccess(), uploadSummary.getCntUpload(), uploadSummary.getCntFailed());
			}

	    	removeUploadedItems();

			String dialogTitle = uploadSummary.getDialogTitle()==null ? "訊息" : uploadSummary.getDialogTitle();
			String dialogMessage = uploadSummary.getDialogMessage()==null ? 
					String.format("上傳完成！成功合計：%s，上傳合計：%s，失敗合計：%s", uploadSummary.getCntSuccess(), uploadSummary.getCntUpload(), uploadSummary.getCntFailed()) : 
					uploadSummary.getDialogMessage();
			DialogUtil.showMessage(MainView.this.getScene().getWindow(), dialogTitle, dialogMessage, true);
		});
    }

    private void onAction_btnUploadAll() {
		Platform.runLater(() -> {
		    String updateStatus = System.getProperty(ATTR_CONFIG_UPDATE_STATUS, "");
		    if (!"AppIsUpToDate".equals(updateStatus)) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "無法上傳檔案！因為掃描程式啟動時您略過了版本檢查，無法確認程式是否為最新版本。請關閉並重新啟動掃描程式，讓程式檢查並更新為最新版本。");
				return;
		    }

			List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
			if (imageTableView.getItems().size()==0) {
				DialogUtil.showMessageAndWait(MainView.this.getScene().getWindow(), "佇列區無影像可上傳！");
				return;
			}

			// 檢查 imagerecordset.xml 是否被更動過
			if (recordSetHelper.xmlFileChanged()) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "影像掃描設定檔被其他程式更動了，將重新載入設定檔！");
				loadCachedData(true);
				setupUIValues();
				return;
			}

			// 檢查影像檔是否存在
			int notExistIndex = imageFilesNotExist();
			if (notExistIndex>-1) {
    			String msg = String.format("影像檔(NO:%s)已經被移除，無法上傳檔案！\nScan files(NO:%s) have been removed from disk! Please restart application to reload file list!", notExistIndex+1, notExistIndex+1);
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, msg);
				imageTableView.getSelectionModel().select(notExistIndex);
				imageTableView.scrollTo(notExistIndex);
				return;
			}

			// BR-CMN-PIC-006 檢核是否有插入或替換文件
			if (isInsertOrReplaceAction()) {
				return;
			}

			boolean isUploadAll = true; // 上傳指定的一個影像檔
			boolean isValid = true;

			// BR-CMN-PIC-017 檢核是否輸入批次號碼
			if (isValid) {
				boolean isBatchNoValid = validateBatchNo(isUploadAll);
				if (!isBatchNoValid) {
					DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "批次號碼未輸入！");
					isValid = false;
				}
			}

			// BR-CMN-PIC-XXX 檢核是否輸入箱號
			if (isValid) {
				boolean isBoxNoValid = validateBoxNo(isUploadAll);
				if (!isBoxNoValid) {
					DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "箱號未輸入！");
					isValid = false;
				}
			}

			if (scanConfig.isDeptNB()) {
				// PCR_386372 - 掃瞄上傳啟動核保完成收到補全件Email通知
				if (isValid) {
					boolean sendEmailValid = validateSendEmail(isUploadAll);
					if (!sendEmailValid) {
						isValid = false;
					}
				}

				// PCR_268354 - 自動核保及核保功能優化需求(視訊投保件)
				if (isValid) {
					boolean isRemoteValid = validateIsRemote(isUploadAll);
					if (!isRemoteValid) {
						isValid = false;
					}
				}
			}

			// BR-CMN-PIC-005 檢核頁碼是否匹配
			if (isValid) {
				boolean isFilePageValid = validateFilePage(isUploadAll);
				if (!isFilePageValid) {
					isValid = false;
				}
			}

			for (int i=0; i<imageTableView.getItems().size(); i++) {
				ScannedImage imageItem = imageTableView.getItems().get(i);
				// 將 ScanConfig 資訊寫回 imagerecord
				//fillScanConfig(scanConfig, imageItem); // UAT-IR-478019 掃描帳號與上傳帳號不同時，上傳人員、部室名稱、批次號碼應帶入掃描人員資料
				recordSetHelper.saveTiffData(imageItem, recordList.get(i));
			}

			// 回寫 XML 檔案
    		try {
    			recordSetHelper.marshalToFile(recordSet);
    		} catch (JAXBException e) {
    			String errMsg = String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
    			DialogUtil.showErrorMessageAndWait(MainView.this.getScene().getWindow(), errMsg);
				logger.error(errMsg, e);
				isValid = false;
    		}

			if (!isValid) {
				return;
			}

    		// 檢查必需登入 eBao Server
			if (!checkLogin()) {
				this.showSnackbar("未登入或離線使用時無法上傳檔案，請登入 eBao Server！", true, Duration.seconds(5.0));
				return;
			}

			UploadProcessSummary uploadSummary = DialogUtil.showUploadDialog(MainView.this, this.scanConfig, imageTableView.getItems(), null);
			if (logger.isDebugEnabled()) {
				logger.debug("dialogTitle={}, dialogMessage={}, cntSuccess={}, cntUpload={}, cntFailed={}", uploadSummary.getDialogTitle(), uploadSummary.getDialogMessage(), uploadSummary.getCntSuccess(), uploadSummary.getCntUpload(), uploadSummary.getCntFailed());
			}

			removeUploadedItems();

			String dialogTitle = uploadSummary.getDialogTitle()==null ? "訊息" : uploadSummary.getDialogTitle();
			String dialogMessage = uploadSummary.getDialogMessage()==null ? 
					String.format("上傳完成！成功合計：%s，上傳合計：%s，失敗合計：%s", uploadSummary.getCntSuccess(), uploadSummary.getCntUpload(), uploadSummary.getCntFailed()) : 
					uploadSummary.getDialogMessage();
			DialogUtil.showMessage(MainView.this.getScene().getWindow(), dialogTitle, dialogMessage, true);
		});
    }

    private void onAction_btnUploadStatus() {
		Platform.runLater(() -> {
			DialogUtil.showUploadLogDialog(MainView.this);
		});
    }

    private void setupImageTableView() {
		if (logger.isDebugEnabled()) {
			logger.debug("");
		}

    	indexNoColumn.setCellValueFactory(cellData -> cellData.getValue().indexNoProperty()==null ? null : cellData.getValue().indexNoProperty().asObject());
//    	indexNoColumn.setCellFactory(new Callback<TableColumn<ScannedImage, Integer>, TableCell<ScannedImage, Integer>>() {
//            @Override
//            public TableCell<ScannedImage, Integer> call(TableColumn<ScannedImage, Integer> p) {
//                TableCell<ScannedImage, Integer> cell = new TableCell<ScannedImage, Integer>() {
//                    @Override
//                    public void updateItem(Integer item, boolean empty) {
//                        super.updateItem(item, empty);
//                        setText(empty ? null : getString());
//                        setGraphic(null);
//                    }
//                    private String getString() {
//                        return getItem() == null ? "" : getItem().toString();
//                    }
//                };
//                cell.setAlignment(Pos.CENTER);
//                return cell;
//            }
//        });
    	indexNoColumn.setSortable(false);
    	indexNoColumn.setVisible(false);
    	scanOrderColumn.setCellValueFactory(cellData -> cellData.getValue().scanOrderProperty());
//    	scanOrderColumn.setCellFactory(new Callback<TableColumn<ScannedImage, String>, TableCell<ScannedImage, String>>() {
//            @Override
//            public TableCell<ScannedImage, String> call(TableColumn<ScannedImage, String> p) {
//                TableCell<ScannedImage, String> cell = new TableCell<ScannedImage, String>() {
//                    @Override
//                    public void updateItem(String item, boolean empty) {
//                        super.updateItem(item, empty);
//                        setText(empty ? null : getString());
//                        setGraphic(null);
//                    }
//                    private String getString() {
//                        return getItem() == null ? "" : getItem().toString();
//                    }
//                };
//                cell.setAlignment(Pos.CENTER);
//                return cell;
//            }
//        });
    	scanOrderColumn.setSortable(false);
    	sendEmailColumn.setCellValueFactory(cellData -> cellData.getValue().sendEmailProperty());
    	sendEmailColumn.setCellFactory(new Callback<TableColumn<ScannedImage, String>, TableCell<ScannedImage, String>>() {
            @Override
            public TableCell<ScannedImage, String> call(TableColumn<ScannedImage, String> p) {
                TableCell<ScannedImage, String> cell = new TableCell<ScannedImage, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : getString());
                        setGraphic(null);
                    }

                    private String getString() {
                    	String item = getItem();
                        return item==null || "".equals(item) ? "" : "Y".equals(item) ? "是" : "否";
                    }
                };
                cell.setAlignment(Pos.CENTER);
                return cell;
            }
        });
    	sendEmailColumn.setSortable(false);
    	makeHeaderWrappable(sendEmailColumn);
    	isRemoteColumn.setCellValueFactory(cellData -> cellData.getValue().isRemoteProperty());
    	isRemoteColumn.setCellFactory(new Callback<TableColumn<ScannedImage, String>, TableCell<ScannedImage, String>>() {
            @Override
            public TableCell<ScannedImage, String> call(TableColumn<ScannedImage, String> p) {
                TableCell<ScannedImage, String> cell = new TableCell<ScannedImage, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : getString());
                        setGraphic(null);
                    }

                    private String getString() {
                    	String item = getItem();
                        return item==null || "".equals(item) ? "" : "Y".equals(item) ? "是" : "否";
                    }
                };
                cell.setAlignment(Pos.CENTER);
                return cell;
            }
        });
    	isRemoteColumn.setSortable(false);
    	makeHeaderWrappable(isRemoteColumn);
    	fileCodeColumn.setCellValueFactory(cellData -> cellData.getValue().fileCodeProperty());
    	fileCodeColumn.setSortable(false);
    	mainFileTypeTextColumn.setCellValueFactory(cellData -> cellData.getValue().mainFileTypeTextProperty());
    	mainFileTypeTextColumn.setSortable(false);
    	fileTypeTextColumn.setCellValueFactory(cellData -> cellData.getValue().fileTypeTextProperty());
    	fileTypeTextColumn.setSortable(false);
        companyCodeColumn.setCellValueFactory(cellData -> cellData.getValue().companyCodeProperty());
        companyCodeColumn.setSortable(false);
        personalCodeColumn.setCellValueFactory(cellData -> cellData.getValue().personalCodeProperty());
        personalCodeColumn.setSortable(false);
    	filePageColumn.setCellValueFactory(cellData -> cellData.getValue().filePageProperty());
//    	filePageColumn.setCellFactory(new Callback<TableColumn<ScannedImage, String>, TableCell<ScannedImage, String>>() {
//            @Override
//            public TableCell<ScannedImage, String> call(TableColumn<ScannedImage, String> p) {
//                TableCell<ScannedImage, String> cell = new TableCell<ScannedImage, String>() {
//                    @Override
//                    public void updateItem(String item, boolean empty) {
//                        super.updateItem(item, empty);
//                        setText(empty ? null : getString());
//                        setGraphic(null);
//                    }
//                    private String getString() {
//                        return getItem() == null ? "" : getItem().toString();
//                    }
//                };
//                cell.setAlignment(Pos.CENTER);
//                return cell;
//            }
//        });
    	filePageColumn.setSortable(false);
    	scanTimeColumn.setCellValueFactory(cellData -> cellData.getValue().scanTimeProperty());
    	scanTimeColumn.setSortable(false);
    	actionTypeColumn.setCellValueFactory(cellData -> cellData.getValue().actionTypeProperty());
    	actionTypeColumn.setSortable(false);

    	//加入 TableView 點選事件動作
    	imageTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    	imageTableView.setEditable(false);
    	imageTableView.getSelectionModel().selectedIndexProperty().addListener((obs, oldSelection, newSelection) -> {
    		//Platform.runLater(() -> {
        		if ( null != newSelection && -1 < newSelection.intValue()) {
    				ScannedImage record = imageTableView.getItems().get(newSelection.intValue());
    				setupFormValue(record); //設定資料至索引編號以及左側影像預覽
    			} else {
    				clearFormValue();
    				setupConfigValue();
    			}
    			setupButtonStatus();
    		//});
    	});
    }

	private void makeHeaderWrappable(TableColumn column) {
		Label label = new Label(column.getText());
		label.setStyle("-fx-padding: 1px;");
		label.setWrapText(true);
		label.setAlignment(Pos.CENTER);
		label.setTextAlignment(TextAlignment.CENTER);

		StackPane stack = new StackPane();
		stack.getChildren().add(label);
		stack.prefWidthProperty().bind(column.widthProperty().subtract(5));
		label.prefWidthProperty().bind(stack.prefWidthProperty());
		column.setText(null);
		column.setGraphic(stack);
	}

    private void setupTableColumns() {
    	boolean showNBColumns = scanConfig == null ? true : scanConfig.isDeptNB();
    	sendEmailColumn.setVisible(showNBColumns);
    	isRemoteColumn.setVisible(showNBColumns);

    	boolean showGidColumns = scanConfig == null ? false : scanConfig.isDeptGid();
    	companyCodeColumn.setVisible(showGidColumns);
    	personalCodeColumn.setVisible(showGidColumns);
    }

    private void setupTooltips() {
		if (logger.isDebugEnabled()) {
			logger.debug("");
		}

		JFXTooltip.setVisibleDuration(Duration.millis(3000));
    	final JFXTooltip btnUpdateTooltip = new JFXTooltip("Ctrl+M");
    	final JFXTooltip btnRemoveTooltip = new JFXTooltip("Ctrl+R");
    	final JFXTooltip btnRemovePartialTooltip = new JFXTooltip("Ctrl+A");
    	final JFXTooltip btnCopyTooltip = new JFXTooltip("Ctrl+C");
    	final JFXTooltip btnImportTooltip = new JFXTooltip("Ctrl+I");
    	final JFXTooltip btnScanSettingsTooltip = new JFXTooltip("Ctrl+T");
    	final JFXTooltip btnScanTooltip = new JFXTooltip("Ctrl+S");
    	final JFXTooltip btnZoomInTooltip = new JFXTooltip("放大 (Ctrl+=)");
    	final JFXTooltip btnZoomOutTooltip = new JFXTooltip("縮小 (Ctrl+-)");
    	final JFXTooltip btnRotateRightTooltip = new JFXTooltip("向右旋轉90度 (Ctrl+0)");
    	final JFXTooltip btnRotateLeftTooltip = new JFXTooltip("向左旋轉90度 (Ctrl+9)");
        JFXTooltip.install(btnUpdate, btnUpdateTooltip, Pos.BOTTOM_CENTER);
        JFXTooltip.install(btnRemove, btnRemoveTooltip, Pos.BOTTOM_CENTER);
        JFXTooltip.install(btnRemovePartial, btnRemovePartialTooltip, Pos.BOTTOM_CENTER);
        JFXTooltip.install(btnCopy, btnCopyTooltip, Pos.BOTTOM_CENTER);
        JFXTooltip.install(btnImport, btnImportTooltip, Pos.BOTTOM_CENTER);
        JFXTooltip.install(btnScanSettings, btnScanSettingsTooltip, Pos.BOTTOM_CENTER);
        JFXTooltip.install(btnScan, btnScanTooltip, Pos.BOTTOM_CENTER);
        JFXTooltip.install(btnZoomIn, btnZoomInTooltip, Pos.BOTTOM_CENTER);
        JFXTooltip.install(btnZoomOut, btnZoomOutTooltip, Pos.BOTTOM_CENTER);
        JFXTooltip.install(btnRotateRight, btnRotateRightTooltip, Pos.BOTTOM_CENTER);
        JFXTooltip.install(btnRotateLeft, btnRotateLeftTooltip, Pos.BOTTOM_CENTER);
    }

    private void setupConverters() {
		if (logger.isDebugEnabled()) {
			logger.debug("");
		}

//    	cbbMainFileType.setOnAction(action -> {
//        	if (null == scanConfig) {
//        		return;
//        	}
//        	cbbFileType.getItems().clear();
//        	cbbFileType.getItems().add(new Pair<String, String>("", ""));
//        	String mainFileType = null==cbbMainFileType.getSelectionModel().getSelectedItem() ? null : cbbMainFileType.getSelectionModel().getSelectedItem().getKey();
//        	if (null != mainFileType && !"".equals(mainFileType)) {
//            	cbbFileType.getItems().addAll(scanConfig.getTotalFileTypes().get(mainFileType));
//        	}
//        });
        cbbMainFileType.setConverter(new StringConverter<Pair<String,String>>() {
			@Override
			public String toString(Pair<String, String> object) {
				return object==null ? "" : object.getValue();
			}
			@Override
			public Pair<String, String> fromString(String string) {
				return cbbMainFileType.getItems().stream().filter(ap -> ap.getKey().equals(string)).findFirst().orElse(null);
			}
        });

        cbbFileType.setConverter(new StringConverter<Pair<String,String>>() {
			@Override
			public String toString(Pair<String, String> object) {
				return object==null ? "" : object.getValue();
			}
			@Override
			public Pair<String, String> fromString(String string) {
				return cbbFileType.getItems().stream().filter(ap -> ap.getKey().equals(string)).findFirst().orElse(null);
			}
        });

        cbbBatchDeptType.setConverter(new StringConverter<Pair<String,String>>() {
			@Override
			public String toString(Pair<String, String> object) {
				return object==null ? "" : object.getValue();
			}
			@Override
			public Pair<String, String> fromString(String string) {
				return cbbBatchDeptType.getItems().stream().filter(ap -> ap.getKey().equals(string)).findFirst().orElse(null);
			}
        });

        // 替換
        cbbActionReplace.setOnAction(action -> {
        	String actionReplace = null==cbbActionReplace.getSelectionModel().getSelectedItem() ? null : cbbActionReplace.getSelectionModel().getSelectedItem().getKey();
        	if (null != actionReplace && Constant.YN_YES.equals(actionReplace)) {
        		cbbActionInsert.getSelectionModel().select(cbbActionInsert.getConverter().fromString(Constant.YN_NO));
        	}
        });
        cbbActionReplace.setConverter(new StringConverter<Pair<String, String>>() {
			@Override
			public String toString(Pair<String, String> object) {
				return object==null ? "" : object.getValue();
			}
			@Override
			public Pair<String, String> fromString(String string) {
				return cbbActionReplace.getItems().stream().filter(ap -> ap.getKey().equals(string)).findFirst().orElse(null);
			}
        });

        // 插入
        cbbActionInsert.setOnAction(action -> {
        	String actionInsert = null==cbbActionInsert.getSelectionModel().getSelectedItem() ? null : cbbActionInsert.getSelectionModel().getSelectedItem().getKey();
        	if (null != actionInsert && Constant.YN_YES.equals(actionInsert)) {
        		cbbActionReplace.getSelectionModel().select(cbbActionReplace.getConverter().fromString(Constant.YN_NO));
        	}
        });
        cbbActionInsert.setConverter(new StringConverter<Pair<String, String>>() {
			@Override
			public String toString(Pair<String, String> object) {
				return object==null ? "" : object.getValue();
			}
			@Override
			public Pair<String, String> fromString(String string) {
				return cbbActionInsert.getItems().stream().filter(ap -> ap.getKey().equals(string)).findFirst().orElse(null);
			}
        });

        // 是否發EMAIL
        cbbSendEmail.setConverter(new StringConverter<Pair<String, String>>() {
			@Override
			public String toString(Pair<String, String> object) {
				return object==null ? "" : object.getValue();
			}
			@Override
			public Pair<String, String> fromString(String string) {
				return cbbSendEmail.getItems().stream().filter(ap -> ap.getKey().equals(string)).findFirst().orElse(null);
			}
        });

        // 視訊投保件
        cbbIsRemote.setConverter(new StringConverter<Pair<String, String>>() {
			@Override
			public String toString(Pair<String, String> object) {
				return object==null ? "" : object.getValue();
			}
			@Override
			public Pair<String, String> fromString(String string) {
				return cbbIsRemote.getItems().stream().filter(ap -> ap.getKey().equals(string)).findFirst().orElse(null);
			}
        });

        // 紙張來源初始化
        cbbDuplexMode.setConverter(new StringConverter<Pair<String,String>>() {
			@Override
			public String toString(Pair<String, String> object) {
				return object==null ? "" : object.getValue();
			}
			@Override
			public Pair<String, String> fromString(String string) {
				return cbbDuplexMode.getItems().stream().filter(ap -> ap.getKey().equals(string)).findFirst().orElse(null);
			}
        });

		// 影像模式初始化
        cbbColorMode.setConverter(new StringConverter<Pair<String,String>>() {
			@Override
			public String toString(Pair<String, String> object) {
				return object==null ? "" : object.getValue();
			}
			@Override
			public Pair<String, String> fromString(String string) {
				return cbbColorMode.getItems().stream().filter(ap -> ap.getKey().equals(string)).findFirst().orElse(null);
			}
        });
    }

    private void setupActions() {
		if (logger.isDebugEnabled()) {
			logger.debug("");
		}

		previewStackPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
    	    @Override
    	    public void handle(MouseEvent event) {
    	        if(event.getButton().equals(MouseButton.PRIMARY)){
    	            if(event.getClickCount() == 2){
    	        		onMouseDoubleClicked_previewStackPane();
    	        		event.consume();
    	            }
    	        }
    	    }
    	});
    	previewStackPane.setOnScroll(event -> {
            if (event.isControlDown()) {
            	onScroll_previewStackPane(event);
            	event.consume();
            }
        });

    	btnUpdate.setOnAction(action -> {
        	onAction_btnUpdate();
        });
        // 這一段 JavaFX 一直有問題，改其他寫法
        //btnUpdate.disableProperty().bind(Bindings.size(imageTableView.getItems()).lessThan(1));

        btnRemove.setOnAction(action -> {
        	onAction_btnRemove();
        });
        //btnRemove.disableProperty().bind(Bindings.size(imageTableView.getSelectionModel().getSelectedItems()).lessThan(1));

        btnRemovePartial.setOnAction(action -> {
        	onAction_btnRemovePartial();
        });
        // 這一段 JavaFX 一直有問題，改其他寫法
        //btnRemovePartial.disableProperty().bind(Bindings.size(imageTableView.getItems()).lessThan(1));

        btnCopy.setOnAction(action -> {
        	onAction_btnCopy();
        });
        //btnCopy.disableProperty().bind(Bindings.size(imageTableView.getSelectionModel().getSelectedItems()).lessThan(1));

        btnImport.setOnAction(action -> {
        	onAction_btnImport();
        });

        btnScan.setOnAction(action -> {
        	onAction_btnScan();
        });

        btnScanSettings.setOnAction(action -> {
        	onAction_btnScanSettings();
        });

        hlSourceName.setOnAction(action -> {
        	onAction_hlSourceName();
        });

        btnZoomIn.setOnAction(action -> {
        	onAction_btnZoomIn();
        });

        btnZoomOut.setOnAction(action -> {
        	onAction_btnZoomOut();
        });

        btnRotateRight.setOnAction(action -> {
        	onAction_btnRotateRight();
        });

        btnRotateLeft.setOnAction(action -> {
        	onAction_btnRotateLeft();
        });
    }

    private void setupOthers() {
		if (logger.isDebugEnabled()) {
			logger.debug("");
		}

    	lbDeptId.setText("批次號碼\n部門別");
    	lbCompanyCode.setText("公司碼\n(團險保單號碼)");
    	lbSendEmail.setText("是否發EMAIL\n(補送掃件應選否)");

    	txtFilePage.setTextFormatter(
			new TextFormatter<>(change -> {
			    if (change.isContentChange()) {
					String text = change.getText();
		    	    if (!text.matches("[0-9]*")) {
		    	        return null;
		    	    }
			    }
    	        return change;
			})
		);
    	txtBatchDate.setTextFormatter(
			new TextFormatter<>(change -> {
			    if (change.isContentChange()) {
	    	        String text = change.getText();
		    		if (logger.isDebugEnabled()) {
		    			logger.debug("txtBatchDate.textFormatter(), text={}, launchParamQueryFromPage={}", text, launchParamQueryFromPage);
		    		}
		            if (launchParamQueryFromPage) {
		            	// 替換/插入模式 (欄位不可編輯，但值有可能為"NA")
		    	        return change;
		            } else {
		            	// 標準模式
			    	    if (!text.matches("[0-9]*")) {
			    	        return null;
			    	    }
		            }
			    }
    	        return change;
			})
		);
    	txtBatchDate.focusedProperty().addListener(new ChangeListener<Boolean>() {
    	    @Override
    	    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
    	    	if (!newPropertyValue) {
    	        	String batchDate = ObjectsUtil.encodeToPreventXSSAttack(txtBatchDate.getText());
    	        	if (null == batchDate || batchDate.length() == 0) {
    	        		return;
    	        	} else {
    	        		if (batchDate.length() < 7) {
    	        			int lenDiff = 7-batchDate.length();
    	        			for (int i=0; i<lenDiff; i++) {
    	        				batchDate = "0" + batchDate;
    	        			}
    	        			txtBatchDate.setText(ObjectsUtil.decodeToPreventXSSAttack(batchDate));
        	        	} else if (batchDate.length() > 7) {
    	        			txtBatchDate.setText(ObjectsUtil.decodeToPreventXSSAttack(batchDate.substring(0, 7)));
        	        	}
    	        	}
    	        }
    	    }
    	});
    	txtBatchArea.focusedProperty().addListener(new ChangeListener<Boolean>() {
    	    @Override
    	    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
    	    	if (!newPropertyValue) {
    	        	String batchArea = ObjectsUtil.encodeToPreventXSSAttack(txtBatchArea.getText());
    	        	if (null == batchArea) {
    	        		return;
    	        	} else {
    	        		String newBatchArea = batchArea.trim();
    	        		if (newBatchArea.length() == 1) {
    	        			newBatchArea = "0" + newBatchArea;
        	        	} else if (newBatchArea.length() > 2) {
        	        		newBatchArea = newBatchArea.substring(0, 2);
        	        	}
	        			txtBatchArea.setText(ObjectsUtil.decodeToPreventXSSAttack(newBatchArea));
    	        	}
    	        }
    	    }
    	});
    	txtBatchDocType.focusedProperty().addListener(new ChangeListener<Boolean>() {
    	    @Override
    	    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
    	        if (!newPropertyValue) {
    	        	String batchDocType = ObjectsUtil.encodeToPreventXSSAttack(txtBatchDocType.getText());
    	        	if (null == batchDocType) {
    	        		return;
    	        	} else {
    	        		String newBatchDocType = batchDocType.trim();
    	        		if (newBatchDocType.length() == 1) {
    	        			newBatchDocType = "0" + newBatchDocType;
        	        	} else if (newBatchDocType.length() > 2) {
        	        		newBatchDocType = newBatchDocType.substring(0, 2);
        	        	}
    	        		txtBatchDocType.setText(ObjectsUtil.decodeToPreventXSSAttack(newBatchDocType));
    	        	}
    	        }
    	    }
    	});
    	txtCompanyCode.focusedProperty().addListener(new ChangeListener<Boolean>() {
    	    @Override
    	    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
    	        if (!newPropertyValue) {
    	        	String companyCode = ObjectsUtil.encodeToPreventXSSAttack(txtCompanyCode.getText());
	        		txtFileCode.setText(ObjectsUtil.decodeToPreventXSSAttack(companyCode));
    	        }
    	    }
    	});
    	txtPersonalCode.focusedProperty().addListener(new ChangeListener<Boolean>() {
    	    @Override
    	    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
    	        if (!newPropertyValue) {
    	        	String personalCode = ObjectsUtil.encodeToPreventXSSAttack(txtPersonalCode.getText());
    	        	if (null == personalCode) {
    	        		return;
    	        	} else {
    	        		String newPersonalCode = personalCode.trim();
    	        		if (newPersonalCode.length() > 0 && newPersonalCode.length() < 6) {
    	        			int lenDiff = 6-newPersonalCode.length();
    	        			for (int i=0; i<lenDiff; i++) {
    	        				newPersonalCode = "0" + newPersonalCode;
    	        			}
        	        	} else if (personalCode.length() > 6) {
        	        		newPersonalCode = personalCode.substring(0, 6);
        	        	}
	        			txtPersonalCode.setText(ObjectsUtil.decodeToPreventXSSAttack(newPersonalCode));
    	        	}
    	        }
    	    }
    	});
    	txtRemark.setTextFormatter(
			new TextFormatter<>(change -> {
			    if (change.isContentChange()) {
			    	int maxByteLength = 102; // IR-463380，限制最多34個中文字
		            String oldText = change.getControlText();
		            String newText = change.getControlNewText();
		            int newByteLength = 0;
		            try {
						byte[] newTextBytes = newText.getBytes("UTF-8");
						if (newTextBytes != null) {
							newByteLength = newTextBytes.length;
						}
					} catch (UnsupportedEncodingException e) {
						logger.error("Get utf8 bytes failed!", e);
					}
		            int oldLength = oldText.length();
			        int newLength = newText.length();
//					if (logger.isDebugEnabled()) {
//						logger.debug("newByteLength={}, newText={}, newLength={}, oldText={}, oldLength={}", newByteLength, newText, newLength, oldText, oldLength);
//					}
			        if (newByteLength > maxByteLength) {
			            change.setText(oldText);
			            change.setRange(0, oldLength);
			        }
			    }
			    return change;
			})
		);

    	snackbar = new JFXSnackbar(root);
        snackbar.setPrefWidth(300);
    }

    private void setupTableData() {
		if (logger.isDebugEnabled()) {
			logger.debug("loginStatus={}", loginStatus);
		}

    	ObservableList<ScannedImage> data = FXCollections.observableArrayList();

		if (this.loginStatus == LoginStatus.STATUS_LOGGED_IN || 
			this.loginStatus == LoginStatus.STATUS_OFF_LINE) {

			if (null != recordSet && null != recordSet.getRecords() && 
				null != recordSet.getRecords().getRecordList()) {
				List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
				for ( int i=0; i<recordList.size(); i++) {
					TiffRecord tiffRecord = recordList.get(i);
					ScannedImage imageTiff = recordSetHelper.convert(tiffRecord);
					imageTiff.indexNoProperty().set(i*100);
					data.add(imageTiff);
				}
				recordSetHelper.sortRecordSet(data);
			}

		}

		imageTableView.getItems().clear();
		imageTableView.getItems().addAll(data);

		setupButtonStatus();
    }

	private void setupButtonStatus() {
		int rowCount = null==imageTableView.getItems() ? 0 : imageTableView.getItems().size();
		int selectedIndex = imageTableView.getSelectionModel().getSelectedIndex();

		if (logger.isDebugEnabled()) {
			logger.debug("rowCount={}, selectedIndex={}", rowCount, selectedIndex);
		}

		if (rowCount > 0) {
			btnUpdate.setDisable(false);
		    btnRemovePartial.setDisable(false);
		} else {
			btnUpdate.setDisable(true);
		    btnRemovePartial.setDisable(true);
		}

		if (selectedIndex > -1) {
			btnRemove.setDisable(false);
			btnCopy.setDisable(false);
		} else {
			btnRemove.setDisable(true);
			btnCopy.setDisable(true);
		}

		if (this.loginStatus == LoginStatus.STATUS_NOT_USER_LOGGIN) {
			btnImport.setDisable(true);
		    btnScanSettings.setDisable(true);
		    btnScan.setDisable(true);
		} else {
			btnImport.setDisable(false);
		    btnScanSettings.setDisable(false);
		    btnScan.setDisable(false);
		}
	}

	private void loadCachedData(boolean reloadXmlOnly) {
		List<String> errorMessages = new ArrayList<String>();

		if (logger.isDebugEnabled()) {
			logger.debug("reloadXmlOnly={}, launchParamQueryFromPage={}, launchParamBoxNo={}, launchParamBatchDeptType={}, launchParamBatchDate={}, launchParamBatchArea={}, launchParamBatchDocType={}", reloadXmlOnly, launchParamQueryFromPage, launchParamBoxNo, launchParamBatchDeptType, launchParamBatchDate, launchParamBatchArea, launchParamBatchDocType);
		}

		if (!reloadXmlOnly) {
			// Step 1: Load properties
			PropertiesCache propertiesCache = PropertiesCache.getInstance();

			// Step 2: Load scanconfig.xml
			try {
	    		String config = ScanConfigUtil.readConfig();
				this.scanConfig = ScanConfigUtil.parseHtml(config);
	    		this.scanConfig.setFromQueryPage(Boolean.toString(launchParamQueryFromPage));
				if (launchParamQueryFromPage) {
					this.scanConfig.resetDefValues(launchParamBoxNo, launchParamBatchDeptType, launchParamBatchDate, launchParamBatchArea, launchParamBatchDocType);
				}
	    		launchParamUserName = propertiesCache.getProperty(PropertiesCache.PROP_KEY.EBAO_USER_NAME.propName());
				if (launchParamUserName != null && !"".equals(launchParamUserName.trim())) {
		    		this.loginStatus = LoginStatus.STATUS_OFF_LINE;
				}
	    		String serverName = propertiesCache.getProperty(PropertiesCache.PROP_KEY.EBAO_HOST.propName());
				serverLabelText.set(serverName==null || "".equals(serverName.trim()) ? "N/A" : serverName);
			} catch (NoSuchFileException nsfe) {
			} catch (IOException ioe) {
				logger.error("", ioe);
				errorMessages.add(ioe.getMessage());
			} catch (Exception e) {
				logger.error("", e);
				errorMessages.add(e.getMessage());
			}
		}

		// Step 3: Load imagerecordset.xml
		ImageRecordSet tmpRecordSet = null;
    	if ( recordSetHelper.xmlFileExists() ) {
    		try {
    			tmpRecordSet = recordSetHelper.unmarshalFromFile();
    		} catch (JAXBException e) {
    			logger.error(e.getMessage(), e);
    			String backupFileName = null;
    			try {
    				backupFileName = recordSetHelper.backupXmlFile();
    			} catch (IOException ioe) {
        			logger.error("", ioe);
    			}
    			String loadErrorMessage = String.format("%s 檔案內容有誤，無法解析！原檔案將備份並更名為 %s 以利除錯追蹤。", ImageRecordHelper.RECORD_SET_FILE_NAME, backupFileName);
    			errorMessages.add(loadErrorMessage);
    		}
    	}
		TiffRecords records = null;
    	if ( this.recordSet == null ) {
    		records = new TiffRecords();
    		records.setRecordList(new ArrayList<TiffRecord>());
    		this.recordSet = new ImageRecordSet();
    		this.recordSet.setRecords(records);
    	} else {
    		records = this.recordSet.getRecords();
    	}
    	records.getRecordList().clear();
    	if ( tmpRecordSet!=null && tmpRecordSet.getRecords()!=null && tmpRecordSet.getRecords().getRecordList()!=null ) {
    		for (TiffRecord tiffRecord : tmpRecordSet.getRecords().getRecordList()) {
    			records.getRecordList().add(tiffRecord);
    		}
    	} 
		try {
			recordSetHelper.marshalToFile(this.recordSet);
		} catch (JAXBException e) {
			logger.error("", e);
			errorMessages.add(String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME));
		}

    	if (errorMessages.size() > 0) {
    		DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), true, errorMessages.toArray(new String[0]));
    	}
	}

    private void setupUIValues() {
    	if (logger.isDebugEnabled()) {
    		logger.debug("loginStatus={}, launchParamQueryFromPage={}", this.loginStatus, this.launchParamQueryFromPage);
    	}

		// Reset btnRunningMode icon
		if (this.launchParamQueryFromPage) {
			btnRunningMode.setGraphic(ToolbarPane.RUNNING_MODE_REPLACE_INSERT);
		} else {
			btnRunningMode.setGraphic(ToolbarPane.RUNNING_MODE_STANDARD);
		}

		// Reset header text
    	if (needToRelogin) {
			loginStatusLabelText.set(launchParamUserName + ":" + Constant.TXT_LOGIN_STATUS_CHANGE_USER);
	    	loginStatusLabelCss.set(Constant.LOGIN_CSS_WARNING);
	    	btnLoginTooltip.textProperty().setValue("登入 (Ctrl+L)");
    	} else if (this.loginStatus == LoginStatus.STATUS_LOGGED_IN) {
			String username = PropertiesCache.getInstance().getProperty(PropertiesCache.PROP_KEY.EBAO_USER_NAME.propName());
			loginStatusLabelText.set(Constant.TXT_LOGIN_STATUS_WELCOME + " " + username);
	    	loginStatusLabelCss.set(Constant.LOGIN_CSS_NORMAL);
	    	btnLoginTooltip.textProperty().setValue("登出 (Ctrl+L)");
		} else if (this.loginStatus == LoginStatus.STATUS_OFF_LINE) {
			String username = PropertiesCache.getInstance().getProperty(PropertiesCache.PROP_KEY.EBAO_USER_NAME.propName());
			loginStatusLabelText.set(username + ":" + Constant.TXT_LOGIN_STATUS_OFF_LINE);
	    	loginStatusLabelCss.set(Constant.LOGIN_CSS_WARNING);
	    	btnLoginTooltip.textProperty().setValue("登入 (Ctrl+L)");
		} else {
			loginStatusLabelText.set(Constant.TXT_LOGIN_STATUS_NOT_LOG_IN);
	    	loginStatusLabelCss.set(Constant.LOGIN_CSS_WARNING);
	    	btnLoginTooltip.textProperty().setValue("登入 (Ctrl+L)");
		}

    	// Reset TableView records
    	setupTableColumns();
		setupTableData();

		// Reset form value
		clearFormValue();
		setupConfigValue();
    }

    private void checkImageDir() {
		if (logger.isDebugEnabled()) {
			logger.debug("");
		}

		List<String> imageRecordList = new ArrayList<String>();
    	for ( ScannedImage imageItem : imageTableView.getItems() ) {
    		imageRecordList.add(imageItem.fileNameProperty().get());
    	}
    	ImageRecordHelper.getInstance().removeUnusedFiles(imageRecordList);

		// 檢查影像檔是否存在
    	String checkStr = checkImageFilesNotExist();
    	if (checkStr!=null) {
			String msg = String.format("影像檔(第%s筆)存在佇列區但實體檔案已經被移除，可能已上傳後不正常關閉APP或實體檔案被人為移除，請手動刪除佇列區中上述影像檔記錄！", checkStr);
			DialogUtil.showErrorMessages(MainView.this.getScene().getWindow(), false, msg);
    	}
    }

    private void setupConfigValue() {
		if (logger.isDebugEnabled()) {
			logger.debug("null == scanConfig --> {}", (null == scanConfig));
		}

    	if (null == scanConfig) {
			return;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("OrgName={}, DeptName={}, BoxNo={}, BatchDepType={}, BatchDate={}, BatchArea={}, BatchDocType={}, ActionReplace={}, ActionInsert={}, FromQueryPage={}, UpdateRole={}, ScanDuplex={}, ScanType={}", scanConfig.getOrgName(), scanConfig.getDeptName(), scanConfig.getBoxNo(), scanConfig.getBatchDepType(), scanConfig.getBatchDate(), scanConfig.getBatchArea(), scanConfig.getBatchDocType(), scanConfig.getActionReplace(), scanConfig.getActionInsert(), scanConfig.getFromQueryPage(), scanConfig.getUpdateRole(), ScanUtil.getScanDuplex(), ScanUtil.getScanType());
			logger.debug("DefBoxNo={}, DefBatchDepType={}, DefBatchDate={}, DefBatchArea={}, DefBatchDocType={}", scanConfig.getDefBoxNo(), scanConfig.getDefBatchDepType(), scanConfig.getDefBatchDate(), scanConfig.getDefBatchArea(), scanConfig.getDefBatchDocType());
		}

    	// 解析 scanconfig.xml 後填入

		// 組織編碼
		txtOrgName.setText(scanConfig.getOrgName());
		// 部室名稱
		txtDeptName.setText(scanConfig.getDeptName());

		// 影像主類型
		cbbMainFileType.getItems().clear();
		cbbMainFileType.getItems().add(new Pair<String, String>("", ""));
		// 影像子類型
		cbbFileType.getItems().clear();
        cbbFileType.getItems().add(new Pair<String, String>("", ""));

		List<Pair<String, String>> mainFileTypeList = scanConfig.getMainFileTypeList();
		if (mainFileTypeList!=null) {
	        cbbMainFileType.getItems().addAll(mainFileTypeList);
	        ObservableList<Pair<String, String>> fileTypeList = FXCollections.observableArrayList();
	        for (Pair<String, String> mainFileType : mainFileTypeList) {
	        	String key = mainFileType.getKey();
	        	fileTypeList.addAll(scanConfig.getTotalFileTypes().get(key));
	        }
			FXCollections.sort(fileTypeList, new Comparator<Pair<String, String>>() {
				@Override
				public int compare(Pair<String, String> o1, Pair<String, String> o2) {
					String compare1 = null, compare2 = null;

					String key1 = o1.getKey();
					if (ObjectsUtil.isEmpty(key1)) {
						compare1 = "Z-";
					} else if (key1.startsWith("UNB")) {
						compare1 = "1-" + key1;
					} else if (key1.startsWith("POS")) {
						compare1 = "2-" + key1;
					} else if (key1.startsWith("CLM")) {
						compare1 = "3-" + key1;
					} else if (key1.startsWith("GID")) {
						compare1 = "4-" + key1;
					} else {
						compare1 = "Z-" + key1;
					}

					String key2 = o2.getKey();
					if (ObjectsUtil.isEmpty(key2)) {
						compare2 = "Z-";
					} else if (key2.startsWith("UNB")) {
						compare2 = "1-" + key2;
					} else if (key2.startsWith("POS")) {
						compare2 = "2-" + key2;
					} else if (key2.startsWith("CLM")) {
						compare2 = "3-" + key2;
					} else if (key2.startsWith("GID")) {
						compare2 = "4-" + key2;
					} else {
						compare1 = "Z-" + key2;
					}

					return compare1.compareTo(compare2);
				}
			});		
			cbbFileType.getItems().addAll(fileTypeList);
		}

        // 箱號
        cbbBoxNumber.getItems().clear();
        cbbBoxNumber.getItems().add("");
        cbbBoxNumber.getItems().addAll(scanConfig.getBoxNos());
        String defBoxNo = scanConfig.getDefBoxNo();
        if (launchParamQueryFromPage) {
            if (!scanConfig.getBoxNos().contains(defBoxNo)) {
                cbbBoxNumber.getItems().add(defBoxNo);
            }
        }
        // 批次號碼-部門別
        cbbBatchDeptType.getItems().clear();
        cbbBatchDeptType.getItems().add(new Pair<String, String>("", ""));
        cbbBatchDeptType.getItems().addAll(scanConfig.getBatchDepTypeList());
        // 日期
        
        // 替換
        if (cbbActionReplace.getItems().size()==0) {
            cbbActionReplace.getItems().add(new Pair<String, String>("", ""));
            cbbActionReplace.getItems().add(new Pair<String, String>(Constant.YN_YES, Constant.TXT_YN_YES));
            cbbActionReplace.getItems().add(new Pair<String, String>(Constant.YN_NO, Constant.TXT_YN_NO));
        }
        String actionReplaceStr = scanConfig.getActionReplace();
        Pair<String, String> actionReplace = cbbActionReplace.getConverter().fromString(actionReplaceStr);
        cbbActionReplace.getSelectionModel().select(actionReplace);
        // 插入
        if (cbbActionInsert.getItems().size()==0) {
            cbbActionInsert.getItems().add(new Pair<String, String>("", ""));
            cbbActionInsert.getItems().add(new Pair<String, String>(Constant.YN_YES, Constant.TXT_YN_YES));
            cbbActionInsert.getItems().add(new Pair<String, String>(Constant.YN_NO, Constant.TXT_YN_NO));
        }
        String actionInsertStr = scanConfig.getActionInsert();
        Pair<String, String> actionInsert = cbbActionInsert.getConverter().fromString(actionInsertStr);
        cbbActionInsert.getSelectionModel().select(actionInsert);
        String fromQueryPage = scanConfig.getFromQueryPage();
        String updateRole = scanConfig .getUpdateRole();
        boolean disableReplace = false;
        boolean disableInsert = false;
        if ("false".equals(fromQueryPage)) {
            disableReplace = true;
            disableInsert = true;
        } else if ("N".equals(updateRole)) {
            disableReplace = true;
            cbbActionReplace.getSelectionModel().select(cbbActionReplace.getConverter().fromString("N"));
        }
    	lbActionReplace.setDisable(disableReplace);
    	cbbActionReplace.setDisable(disableReplace);
    	lbActionInsert.setDisable(disableInsert);
    	cbbActionInsert.setDisable(disableInsert);
        // 是否發EMAIL
        if (cbbSendEmail.getItems().size()==0) {
            cbbSendEmail.getItems().add(new Pair<String, String>("", ""));
            cbbSendEmail.getItems().add(new Pair<String, String>(Constant.YN_YES, Constant.TXT_YN_YES));
            cbbSendEmail.getItems().add(new Pair<String, String>(Constant.YN_NO, Constant.TXT_YN_NO));
        } else {
        	cbbSendEmail.getSelectionModel().clearSelection();
        }
        // 視訊投保件
        if (cbbIsRemote.getItems().size()==0) {
            cbbIsRemote.getItems().add(new Pair<String, String>("", ""));
            cbbIsRemote.getItems().add(new Pair<String, String>(Constant.YN_YES, Constant.TXT_YN_YES));
            cbbIsRemote.getItems().add(new Pair<String, String>(Constant.YN_NO, Constant.TXT_YN_NO));
        } else {
        	cbbIsRemote.getSelectionModel().clearSelection();
        }
        boolean disableSendEmail = true;
        boolean disableIsRemote = true;
        if (scanConfig.isDeptNB()) {
        	disableSendEmail = false;
        	disableIsRemote = false;
        }
    	lbSendEmail.setDisable(disableSendEmail);
    	cbbSendEmail.setDisable(disableSendEmail);
    	lbIsRemote.setDisable(disableIsRemote);
    	cbbIsRemote.setDisable(disableIsRemote);

		// 紙張來源-初始值除了UNB(新契約)為雙面，其餘都單面
        if (cbbDuplexMode.getItems().size()==0) {
            cbbDuplexMode.getItems().add(new Pair<String, String>("", ""));
            cbbDuplexMode.getItems().add(new Pair<String, String>(Constant.DUPLEX_MODE_SINGLE_PAGE, Constant.TXT_DUPLEX_MODE_SINGLE_PAGE));
            cbbDuplexMode.getItems().add(new Pair<String, String>(Constant.DUPLEX_MODE_DOUBLE_PAGE, Constant.TXT_DUPLEX_MODE_DOUBLE_PAGE));
        }
        cbbDuplexMode.getSelectionModel().select( ScanUtil.getScanDuplex() == TwainConstants.TWDX_NONE ? 1 : 2 );
        
        // 影像模式-預設初始值為黑白
        if (cbbColorMode.getItems().size()==0) {
            cbbColorMode.getItems().add(new Pair<String, String>("", ""));
            cbbColorMode.getItems().add(new Pair<String, String>(Constant.COLOR_MODE_BLACK_WHITE, Constant.TXT_COLOR_MODE_BLACK_WHITE));
            cbbColorMode.getItems().add(new Pair<String, String>(Constant.COLOR_MODE_COLOR, Constant.TXT_COLOR_MODE_COLOR));
        }
        cbbColorMode.getSelectionModel().select( ScanUtil.getScanType() ==  TwainConstants.TWPT_BW ? 1 : 2 );

		if (logger.isDebugEnabled()) {
			logger.debug("launchParamQueryFromPage={}, launchParamBoxNo={}, launchParamBatchDeptType={}, launchParamBatchDate={}, launchParamBatchArea={}, launchParamBatchDocType={}", launchParamQueryFromPage, launchParamBoxNo, launchParamBatchDeptType, launchParamBatchDate, launchParamBatchArea, launchParamBatchDocType);
		}

        if (launchParamQueryFromPage) {
        	// 替換/插入模式

    		if (logger.isDebugEnabled()) {
    			logger.debug("Replace/Insert Mode! launchParamBoxNo={}, launchParamBatchDeptType={}, launchParamBatchDate={}, launchParamBatchArea={}, launchParamBatchDocType={}", launchParamBoxNo, launchParamBatchDeptType, launchParamBatchDate, launchParamBatchArea, launchParamBatchDocType);
    			logger.debug("DefBoxNo={}, DefBatchDepType={}, DefBatchDate={}, DefBatchArea={}, DefBatchDocType={}", scanConfig.getDefBoxNo(), scanConfig.getDefBatchDepType(), scanConfig.getDefBatchDate(), scanConfig.getDefBatchArea(), scanConfig.getDefBatchDocType());
    		}

        	// 箱號
        	cbbBoxNumber.getSelectionModel().select(cbbBoxNumber.getConverter().fromString(defBoxNo));
        	cbbBoxNumber.setDisable(true);
        	// 批次號碼-部門別
            String defBatchDepType = scanConfig.getDefBatchDepType();
            cbbBatchDeptType.getSelectionModel().select(cbbBatchDeptType.getConverter().fromString(defBatchDepType));
        	if (cbbBatchDeptType.getSelectionModel().getSelectedIndex()<0) {
        		cbbBatchDeptType.getItems().add(new Pair<String, String>(defBatchDepType, defBatchDepType));
                cbbBatchDeptType.getSelectionModel().select(cbbBatchDeptType.getConverter().fromString(defBatchDepType));
        	}
        	cbbBatchDeptType.setDisable(true);
        	// 批次號碼-日期
        	txtBatchDate.setText(scanConfig.getDefBatchDate());
        	txtBatchDate.setDisable(true);
        	// 分區
        	txtBatchArea.setText(scanConfig.getDefBatchArea());
        	txtBatchArea.setDisable(true);
        	// 文件別
        	txtBatchDocType.setText(scanConfig.getDefBatchDocType());
        	txtBatchDocType.setDisable(true);
        } else {
        	// 標準模式

        	// 箱號
        	cbbBoxNumber.setDisable(false);
        	// 批次號碼-部門別
            cbbBatchDeptType.getSelectionModel().select(cbbBatchDeptType.getConverter().fromString(scanConfig.getBatchDepType()));
        	cbbBatchDeptType.setDisable(false);
        	// 日期
            txtBatchDate.setText(scanConfig.getBatchDate());
        	txtBatchDate.setDisable(false);
        	// 分區
        	txtBatchArea.setText(scanConfig.getBatchArea());
        	txtBatchArea.setDisable(false);
        	// 文件別
            txtBatchDocType.setText(scanConfig.getBatchDocType());
        	txtBatchDocType.setDisable(false);
        }
    }

	private void clearFormValue() {
		if (logger.isDebugEnabled()) {
			logger.debug("");
		}

		cbbMainFileType.getItems().clear();
		cbbFileType.getItems().clear();
        cbbBoxNumber.getItems().clear();
        cbbBatchDeptType.getItems().clear();
        cbbActionReplace.getItems().clear();
        cbbActionInsert.getItems().clear();
        cbbSendEmail.getItems().clear();
        cbbIsRemote.getItems().clear();

        cbbDuplexMode.getItems().clear();
        cbbColorMode.getItems().clear();
    	lbActionReplace.setDisable(true);
    	cbbActionReplace.setDisable(true);
    	lbActionInsert.setDisable(true);
    	cbbActionInsert.setDisable(true);
    	lbSendEmail.setDisable(true);
    	cbbSendEmail.setDisable(true);
    	lbIsRemote.setDisable(true);
    	cbbIsRemote.setDisable(true);

		txtOrgName.setText("");
		txtDeptName.setText("");
		txtFileCode.setText("");
	    txtFilePage.setText("");
	    txtBatchDate.setText("");
	    txtBatchArea.setText("");
	    txtBatchDocType.setText("");
	    txtCompanyCode.setText("");
	    txtPersonalCode.setText("");
	    txtRemark.setText("");

	    // 清空預覽頁面
		previewImageView.setImage(null);
		previewImageView.setFitWidth(0);
		previewImageView.setFitHeight(0);
		previewImageView.setVisible(false);
		btnZoomIn.setDisable(true);
		btnZoomOut.setDisable(true);
		btnRotateRight.setDisable(true);
		btnRotateLeft.setDisable(true);

		logoImageView.setVisible(true);
	}

	private void setupFormValue(ScannedImage record) {
		String orgName = record.orgNameProperty().get();
		String deptName = record.deptNameProperty().get();
		String mainFileType = record.mainFileTypeProperty().get();
		String fileType = record.fileTypeProperty().get();
		String fileCode = record.fileCodeProperty().get();
		String filePage = record.filePageProperty().get();
		String boxNo = record.boxNoProperty().get();
		String batchDepType = record.batchDepTypeProperty().get();
		String batchDate = record.batchDateProperty().get();
		String batchArea = record.batchAreaProperty().get();
		String batchDocType = record.batchDocTypeProperty().get();
		String companyCode = record.companyCodeProperty().get();
		String personalCode = record.personalCodeProperty().get();
		String actionReplace = record.actionReplaceProperty().get();
		String actionInsert = record.actionInsertProperty().get();
		String isRemote = record.isRemoteProperty().get();
		String sendEmail = record.sendEmailProperty().get();
		String remark = record.remarkProperty().get();

		if (logger.isDebugEnabled()) {
			logger.debug("orgName={}, deptName={}, mainFileType={}, fileType={}, fileCode={}, filePage={}" , orgName, deptName, mainFileType, fileType, fileCode, filePage);
			logger.debug("boxNo={}, batchDepType={}, batchDate={}, batchArea={}, batchDocType={}, companyCode={}, personalCode={}" , boxNo, batchDepType, batchDate, batchArea, batchDocType, companyCode, personalCode);
			logger.debug("actionReplace={}, actionInsert={}, isRemote={}, sendEmail={}, remark={}" , actionReplace, actionInsert, isRemote, sendEmail, remark);
		}

		txtOrgName.setText(orgName);
		txtDeptName.setText(deptName);
		cbbMainFileType.getSelectionModel().select(cbbMainFileType.getConverter().fromString(mainFileType));
		cbbFileType.getSelectionModel().select(cbbFileType.getConverter().fromString(fileType));
	    txtFileCode.setText(fileCode);
	    txtFilePage.setText(filePage);
	    if (ObjectsUtil.isNotEmpty(boxNo) && !cbbBoxNumber.getItems().contains(boxNo)) {
	    	// UAT-IR-478019 掃描帳號與上傳帳號不同時，箱號應為原掃描人員設定的資料
	        cbbBoxNumber.getItems().clear();
	        cbbBoxNumber.getItems().add(boxNo);
	    }
	    cbbBoxNumber.getSelectionModel().select(cbbBoxNumber.getConverter().fromString(boxNo));
        cbbBatchDeptType.getSelectionModel().select(cbbBatchDeptType.getConverter().fromString(batchDepType));
	    txtBatchDate.setText(batchDate);
	    txtBatchArea.setText(batchArea);
	    txtBatchDocType.setText(batchDocType);
	    txtCompanyCode.setText(companyCode);
	    txtPersonalCode.setText(personalCode);
	    cbbActionReplace.getSelectionModel().select(cbbActionReplace.getConverter().fromString(actionReplace));
	    cbbActionInsert.getSelectionModel().select(cbbActionInsert.getConverter().fromString(actionInsert));
		cbbIsRemote.getSelectionModel().select(cbbIsRemote.getConverter().fromString(isRemote));
		cbbSendEmail.getSelectionModel().select(cbbSendEmail.getConverter().fromString(sendEmail));
	    txtRemark.setText(remark);

	    //調出影像tiff檔案呈現
	    String fileUrl = record.fileURLProperty().get();
		Image image = null;
		try {
			image = ImageUtil.createFxImage(fileUrl);
		} catch (IOException e) {
			// 舊程式找不到檔案不顯示訊息
			//DialogUtil.showErrorMessage(MainView.this.getScene().getWindow(), e.getMessage());
			// 上傳全部後會一直跳,故移除不顯示
			//this.showSnackbar(e.getMessage(), true, Duration.seconds(2.0));
			logger.error(e);

		}

		if (null == image) {
		    // 清空預覽頁面
			previewImageView.setImage(null);
			previewImageView.setFitWidth(0);
			previewImageView.setFitHeight(0);
			previewImageView.setVisible(false);
			logoImageView.setVisible(true);
			btnZoomIn.setDisable(true);
			btnZoomOut.setDisable(true);
			btnRotateRight.setDisable(true);
			btnRotateLeft.setDisable(true);
		} else {
			if ( image.getWidth() > image.getHeight() ) {
				previewImageView.setFitHeight(0);
				previewImageView.setFitWidth(previewScrollPane.getWidth()*DEFAULT_ZOOM_IN);
			} else {
				previewImageView.setFitWidth(0);
				previewImageView.setFitHeight(previewScrollPane.getHeight()*DEFAULT_ZOOM_IN);
			}
			previewImageView.setPreserveRatio(true);
//			previewImageView.setRotate(0);
			previewImageView.setImage(image);
			previewImageView.setVisible(true);
			logoImageView.setVisible(false);
			btnZoomIn.setDisable(false);
			btnZoomOut.setDisable(false);
			btnRotateRight.setDisable(false);
			btnRotateLeft.setDisable(false);
		}
	}

	private boolean imageFileNotExist() {
		boolean notExist = false;
		ScannedImage selectedItem = imageTableView.getSelectionModel().getSelectedItem();
		if (null != selectedItem) {
			String fileURL = selectedItem.fileURLProperty().getValue();
			notExist = Files.notExists(Paths.get(fileURL));
		}
		return notExist;
	}

	private int imageFilesNotExist() {
		int index = -1;
		int notExistIndex = -1;
		for(ScannedImage item : imageTableView.getItems()) {
			index++;
			String fileURL = item.fileURLProperty().getValue();
			if (Files.notExists(Paths.get(fileURL))) {
				notExistIndex = index;
				break;
			}
		}
		return notExistIndex;
	}

	private String checkImageFilesNotExist() {
		int index = 0;
		List<Integer> indexList = new ArrayList<Integer>();
		for (ScannedImage item : imageTableView.getItems()) {
			index++;
			String fileURL = item.fileURLProperty().getValue();
			if (Files.notExists(Paths.get(fileURL))) {
				indexList.add(index);
			}
		}

		if (indexList.size()==0) {
			return null;
		} else if (indexList.size()==1) {
			return String.valueOf(indexList.get(0));
		}

		indexList.add(Integer.MAX_VALUE); // 故意加1筆在最後
		List<String> indexStrlist = new ArrayList<String>();

		int start = -1, end = -1, idxNo = -1;
		for (int i=0; i<indexList.size(); i++) {
			idxNo = indexList.get(i);
			if (i==0) { // 第1筆
				start = idxNo;
				end = idxNo;
				continue;
			} else if (idxNo==(end+1)) { // 有連號
				end = idxNo;
				continue;
			}
			if (start==end) {
				indexStrlist.add(String.valueOf(start));
			} else if (start+1==end) {
				indexStrlist.add(String.valueOf(start));
				indexStrlist.add(String.valueOf(end));
			} else {
				indexStrlist.add(String.valueOf(start) + "~" + String.valueOf(end));
			}
			start = idxNo;
			end = idxNo;
		}

		return String.join("、", indexStrlist.toArray(String[]::new));
	}

	private boolean validateBatchNo(boolean isUploadAll) {
		if (logger.isDebugEnabled()) {
			logger.debug("validateBatchNo(), isUploadAll={}", isUploadAll);
		}

		String batchDepType = null;
		String batchDate = null;
		String batchArea = null;
		String batchDocType = null;

		if (isUploadAll) {
			boolean isValid = false;
			for(ScannedImage item : imageTableView.getItems()) {
				batchDepType = item.batchDepTypeProperty().getValue();
				batchDate = item.batchDateProperty().getValue();
				batchArea = item.batchAreaProperty().getValue();
				batchDocType = item.batchDocTypeProperty().getValue();
				if (ObjectsUtil.isNotEmpty(batchDepType) && ObjectsUtil.isNotEmpty(batchDate) && ObjectsUtil.isNotEmpty(batchArea) && ObjectsUtil.isNotEmpty(batchDocType)) {
					isValid = true;
					if (logger.isDebugEnabled()) {
						logger.debug("isValid={}, batchDepType={}, batchDate={}, batchArea={}, batchDocType={}", isValid, batchDepType, batchDate, batchArea, batchDocType);
					}
					break;
				}
			}
			if (isValid) {
				for(ScannedImage item : imageTableView.getItems()) {
					item.batchDepTypeProperty().setValue(batchDepType);
					item.batchDateProperty().setValue(batchDate);
					item.batchAreaProperty().setValue(batchArea);
					item.batchDocTypeProperty().setValue(batchDocType);
				}
			}
			return isValid;
		} else {
			ScannedImage selectedItem = imageTableView.getSelectionModel().getSelectedItem();
			batchDepType = selectedItem.batchDepTypeProperty().getValue();
			batchDate = selectedItem.batchDateProperty().getValue();
			batchArea = selectedItem.batchAreaProperty().getValue();
			batchDocType = selectedItem.batchDocTypeProperty().getValue();
			if (logger.isDebugEnabled()) {
				logger.debug("batchDepType={}, batchDate={}, batchArea={}, batchDocType={}", batchDepType, batchDate, batchArea, batchDocType);
			}
			if (ObjectsUtil.isNotEmpty(batchDepType) && ObjectsUtil.isNotEmpty(batchDate) && ObjectsUtil.isNotEmpty(batchArea) && ObjectsUtil.isNotEmpty(batchDocType)) {
				return true;
			}
		}

		return false;
	}

	private boolean validateFilePage(boolean isUploadAll) {
		boolean isSuccess = true;
		boolean needToCheck = false;
		String lastFileType = null;
		String lastFileCode = null;
		String lastFilePage = null;
		String lastMaxPage = null;
		String mainFileType = null;
		String fileType = null;
		String fileCode = null;
		String filePage = null;
		String maxPage = null;
		String signature = null;
		PageNoValidator pageNoValidator = new PageNoValidator();
		ObservableList<PageWarning> pageWarningList = FXCollections.observableArrayList();
		int rowCount = imageTableView.getItems().size();

		// Sort 會改變上傳時間
		// sort by FileCode & PageNo
		//m_irsRS.SortRecords();
		//SyncWithXML();

		if (logger.isDebugEnabled()) {
			logger.debug("validateFilePage(), isUploadAll={}, rowCount={}", isUploadAll, rowCount);
		}

		for (int i=0; i<rowCount; i++) {
			ScannedImage item = imageTableView.getItems().get(i);
			needToCheck = false;
			mainFileType = item.mainFileTypeProperty().getValue();
			fileType = item.fileTypeProperty().getValue();
			fileCode = item.fileCodeProperty().getValue();
			filePage = item.filePageProperty().getValue();
			maxPage = item.maxPageProperty().getValue();
			signature = item.signatureProperty().getValue();

			if (logger.isDebugEnabled()) {
				logger.debug("mainFileType={}, fileType={}, fileCode={}, filePage={}, maxPage={}, signature={}", mainFileType, fileType, fileCode, filePage, maxPage, signature);
			}

			if ("0".equals(maxPage) || "Y".equals(signature)) { // 最大頁數=0 || 切簽名影像,不檢核
				lastMaxPage = maxPage;
				if (logger.isDebugEnabled()) {
					logger.debug("maxPage=0 or is signature, Ignore pageNo check!");
				}
				continue;
			} else if ("1".equals(maxPage) && "1".equals(filePage)) { // 最大頁數=1,頁碼=1,不檢核
				if (pageNoValidator.size()<=0) {
					lastFileType = fileType;
					lastFileCode = fileCode;
					lastFilePage = filePage;	
					lastMaxPage  = maxPage;	
					if (logger.isDebugEnabled()) {
						logger.debug("maxPage=1 && filePage=1, Ignore pageNo check!");
					}
					continue;
				}
			}

			if (
				(pageNoValidator.size()<=0) || 
				( 
					ObjectsUtil.isNotEmpty(lastFileType) && ObjectsUtil.isNotEmpty(fileCode) && 
					ObjectsUtil.isEquals(fileType, lastFileType) &&  ObjectsUtil.isEquals(fileCode, lastFileCode)
				) )
			{
				pageNoValidator.setMultiPolicy(false);
				pageNoValidator.add(item);
				if (logger.isDebugEnabled()) {
					logger.debug("第一筆或非組合保單,同FileType/FileCode, AddRecord");
				}
			} else if (
				("UNB".equals(mainFileType) || "POS".equals(mainFileType)) &&
				ObjectsUtil.isEquals(fileType, lastFileType) && 
				fileCode.length()==11 && 
				ObjectsUtil.isEquals(ObjectsUtil.left(fileCode, 10), ObjectsUtil.left(lastFileCode, 10))
			) {
				pageNoValidator.setMultiPolicy(true);
				pageNoValidator.add(item);
				if (logger.isDebugEnabled()) {
					logger.debug("組合保單,同FileType,同FileCode(10), AddRecord");
				}
			} else {
				// FileCode/FileType changed  
				needToCheck = true;
				if (logger.isDebugEnabled()) {
					logger.debug("FileType||FileCode改變");
				}
			}

			if (needToCheck) {
				if (!pageNoValidator.validate()) {
					if (logger.isDebugEnabled()) {
						logger.debug("pageNoValidator.validate(): Failed!");
					}
					int cnt = pageNoValidator.size();
					for (int j=0; j<cnt; j++) {
						ScannedImage validImage = pageNoValidator.get(j);
						String scanOrder = validImage.scanOrderProperty().getValue();
						String errorMainFileType = validImage.mainFileTypeProperty().getValue();
						String errorFileType = validImage.fileTypeProperty().getValue();
						String errorFileCode = validImage.fileCodeProperty().getValue();
						String errorFilePage = validImage.filePageProperty().getValue();
						String errorCompanyCode = validImage.companyCodeProperty().getValue();
						String errorPersonalCode = validImage.personalCodeProperty().getValue();
						String errorActionInsert = validImage.actionInsertProperty().getValue();
						String errorActionReplace = validImage.actionReplaceProperty().getValue();
						String errorScanTime = validImage.scanTimeProperty().getValue();
						String errorRemark = "";
						String errorCardDesc = this.scanConfig.getDescByCardCode(errorFileType);
						Integer errorIndexNo = validImage.indexNoProperty().getValue();

						if ("Y".equals(errorActionInsert)) {
							errorRemark = "插入";
						} else if ("Y".equals(errorActionReplace)) {
							errorRemark = "替換";
						}
						if (logger.isDebugEnabled()) {
							logger.debug("PageNo is invalid, append error msg... ");
							logger.debug("scanOrder={}, errorMainFileType={}, errorFileType={}, errorFileCode={}, errorFilePage={}, errorCompanyCode={}, errorPersonalCode={}, errorActionInsert={}, errorActionReplace={}, errorScanTime={}, errorRemark={}, errorCardDesc={}, errorIndexNo={}, ", scanOrder, errorMainFileType, errorFileType, errorFileCode, errorFilePage, errorCompanyCode, errorPersonalCode, errorActionInsert, errorActionReplace, errorScanTime, errorRemark, errorCardDesc, errorIndexNo);
						}

						PageWarning pageWarning = new PageWarning(
							scanOrder, 
							errorFileCode, 
							errorMainFileType, 
							errorFileType + "-" + errorCardDesc, 
							errorCompanyCode, 
							errorPersonalCode, 
							errorFilePage, 
							errorScanTime, 
							errorRemark, 
							errorIndexNo 
						);
						pageWarningList.add(pageWarning);

						isSuccess = false;
					}
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("pageNoValidator.validate(): Passed!");
					}
				}

				pageNoValidator.clear();

				if ("1".equals(maxPage) && "1".equals(filePage)) { // 最大頁數=1,頁碼=1,不檢核
					if (logger.isDebugEnabled()) {
						logger.debug("maxPage==1 && filePage==1, DO NOT AddRecord");
					}
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("AddRecord");
					}
					pageNoValidator.add(item);
				}
			}

			lastFileType = fileType;
			lastFileCode = fileCode;
			lastFilePage = filePage;	
			lastMaxPage  = maxPage;		

			if (logger.isDebugEnabled()) {
				logger.debug("lastFileType={}, lastFileCode={}, lastFilePage={}, lastMaxPage={}", lastFileType, lastFileCode, lastFilePage, lastMaxPage);
			}
		}
		
		// Last Record
		if (pageNoValidator.size()>0) {
			if (!pageNoValidator.validate()) {
				if (logger.isDebugEnabled()) {
					logger.debug("pageNoValidator.validate(): Failed!");
				}
				int cnt = pageNoValidator.size();
				for (int j=0; j<cnt; j++) {
					ScannedImage validImage = pageNoValidator.get(j);
					String scanOrder = validImage.scanOrderProperty().getValue();
					String errorMainFileType = validImage.mainFileTypeProperty().getValue();
					String errorFileType = validImage.fileTypeProperty().getValue();
					String errorFileCode = validImage.fileCodeProperty().getValue();
					String errorFilePage = validImage.filePageProperty().getValue();
					String errorCompanyCode = validImage.companyCodeProperty().getValue();
					String errorPersonalCode = validImage.personalCodeProperty().getValue();
					String errorActionInsert = validImage.actionInsertProperty().getValue();
					String errorActionReplace = validImage.actionReplaceProperty().getValue();
					String errorScanTime = validImage.scanTimeProperty().getValue();
					String errorRemark = "";
					String errorCardDesc = this.scanConfig.getDescByCardCode(errorFileType);
					Integer errorIndexNo = validImage.indexNoProperty().getValue();

					if ("Y".equals(errorActionInsert)) {
						errorRemark = "插入";
					} else if ("Y".equals(errorActionReplace)) {
						errorRemark = "替換";
					}
					if (logger.isDebugEnabled()) {
						logger.debug("PageNo is invalid, append error msg ... ");
						logger.debug("scanOrder={}, errorMainFileType={}, errorFileType={}, errorFileCode={}, errorFilePage={}, errorCompanyCode={}, errorPersonalCode={}, errorActionInsert={}, errorActionReplace={}, errorScanTime={}, errorRemark={}, errorCardDesc={}, errorIndexNo={}, ", scanOrder, errorMainFileType, errorFileType, errorFileCode, errorFilePage, errorCompanyCode, errorPersonalCode, errorActionInsert, errorActionReplace, errorScanTime, errorRemark, errorCardDesc, errorIndexNo);
					}

					PageWarning pageWarning = new PageWarning(
						scanOrder, 
						errorFileCode, 
						errorMainFileType, 
						errorFileType + "-" + errorCardDesc, 
						errorCompanyCode, 
						errorPersonalCode, 
						errorFilePage, 
						errorScanTime, 
						errorRemark, 
						errorIndexNo 
					);
					pageWarningList.add(pageWarning);

					isSuccess = false;
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("pageNoValidator.validate(): Passed!");
				}
			}

			pageNoValidator.clear();
			pageNoValidator = null;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("isSuccess={}", isSuccess);
		}

		if (!isSuccess) {
			boolean stillUpload = false;
			Integer result = DialogUtil.showPageNoWarningDialog(MainView.this, pageWarningList);
			if (logger.isDebugEnabled()) {
				logger.debug("validateFilePage() --> showPageNoWarningDialog(), result={}", result);
			}
			if (null!=result) { // 返回檢查
				if (Integer.valueOf("-1").equals(result)) {
					// 仍需上傳
					stillUpload =  true;
				} else {
					// 返回檢查: 有選擇一筆
	    			imageTableView.getSelectionModel().clearSelection();
	    			for (int i=0; i< imageTableView.getItems().size(); i++) {
	    				ScannedImage item = imageTableView.getItems().get(i);
	    				if (result.equals(item.indexNoProperty().getValue())) {
	    					imageTableView.getSelectionModel().select(i);
	    					imageTableView.scrollTo(i);
	    					break;
	    				}
	    			}
				}
			} else {
				// 返回檢查: 未選擇任一筆
			}

			pageWarningList.clear();
			pageWarningList = null;

			return stillUpload;
		}

		return true;
	}

	private boolean validateBoxNo(boolean isUploadAll) {
		if (logger.isDebugEnabled()) {
			logger.debug("validateBoxNo(), isUploadAll={}", isUploadAll);
		}

		String boxNo = null;
		String scanTime = null;

		if (isUploadAll) {
			boolean isValid = false;
			for(ScannedImage item : imageTableView.getItems()) {
				boxNo = item.boxNoProperty().getValue();
				scanTime = item.scanTimeProperty().getValue();
				if (ObjectsUtil.isNotEmpty(boxNo)) {
					isValid = true;
					if (logger.isDebugEnabled()) {
						logger.debug("isValid={}, boxNo={}, scanTime={}", isValid, boxNo, scanTime);
					}
					break;
				}
			}
			if (isValid) {
				for(ScannedImage item : imageTableView.getItems()) {
					item.boxNoProperty().setValue(boxNo);
					item.scanTimeProperty().setValue(scanTime);
				}
			}
			return isValid;
		} else {
			ScannedImage selectedItem = imageTableView.getSelectionModel().getSelectedItem();
			boxNo = selectedItem.boxNoProperty().getValue();
			if (logger.isDebugEnabled()) {
				logger.debug("boxNo={}", boxNo);
			}
			if (ObjectsUtil.isNotEmpty(boxNo)) {
				return true;
			}
		}

		return false;
	}

	private boolean validateSendEmail(boolean isUploadAll) {
		if (logger.isDebugEnabled()) {
			logger.debug("validateSendEmail(), isUploadAll={}", isUploadAll);
		}

		boolean isValid = true;
		String scanOrder = null;
		String sendEmail = null;
		String firstSendEmail = null;
		Map<String, String> scanOrderMap = new LinkedHashMap<String, String>();
		String errorMessage = "";

		if (isUploadAll) {
			for(ScannedImage item : imageTableView.getItems()) {
				scanOrder = item.scanOrderProperty().getValue();
				sendEmail = item.sendEmailProperty().getValue();
				if (ObjectsUtil.isNotEmpty(sendEmail)) {
					if (logger.isDebugEnabled()) {
						logger.debug("scanOrder={}, sendEmail={}", scanOrder, sendEmail);
					}
					scanOrderMap.put(scanOrder, sendEmail);

					if (firstSendEmail == null) {
						firstSendEmail = sendEmail;
					} else {
						if (!sendEmail.equals(firstSendEmail)) {
							isValid = false;
						}
					}
				}
			}
			if (scanOrderMap.size() > 0) {
				if (isValid) {
					for(ScannedImage item : imageTableView.getItems()) {
						item.sendEmailProperty().setValue(firstSendEmail);
					}
				} else {
					errorMessage = String.format("序號 %s 是否發EMAIL 設置不一致！", String.join("、", scanOrderMap.keySet()));
				}
			} else {
				isValid = false;
				errorMessage = "是否發EMAIL 未設置！";
			}
		} else {
			ScannedImage selectedItem = imageTableView.getSelectionModel().getSelectedItem();
			sendEmail = selectedItem.sendEmailProperty().getValue();
			if (logger.isDebugEnabled()) {
				logger.debug("sendEmail={}", sendEmail);
			}
			if (ObjectsUtil.isEmpty(sendEmail)) {
				isValid = false;
				errorMessage = "是否發EMAIL 未設置！";
			}
		}

		if (!isValid) {
			DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, errorMessage);
		}

		return isValid;
	}

	private boolean validateIsRemote(boolean isUploadAll) {
		if (logger.isDebugEnabled()) {
			logger.debug("validateIsRemote(), isUploadAll={}", isUploadAll);
		}

		boolean isValid = true;
		Map<String, String> errorMessageMap = new LinkedHashMap<String, String>();

		if (isUploadAll) {
			List<String> fileCodeList = new ArrayList<String>();
			for(ScannedImage item : imageTableView.getItems()) {
				String fileCode = item.fileCodeProperty().getValue();
				if (ObjectsUtil.isNotEmpty(fileCode) && !fileCodeList.contains(fileCode)) {
					fileCodeList.add(fileCode);
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("fileCodeList={}", String.join(",", fileCodeList));
			}

			for (String fileCode : fileCodeList) {
				boolean isRemoteValid = true;
				String scanOrder = null;
				String isRemote = null;
				String firstIsRemote = null;
				Map<String, String> scanOrderMap = new LinkedHashMap<String, String>();

				for (ScannedImage item : imageTableView.getItems()) {
					if (!fileCode.equals(item.fileCodeProperty().getValue())) {
						continue;
					}

					scanOrder = item.scanOrderProperty().getValue();
					isRemote = item.isRemoteProperty().getValue();
					if (ObjectsUtil.isNotEmpty(isRemote)) {
						if (logger.isDebugEnabled()) {
							logger.debug("fileCode={}, scanOrder={}, isRemote={}", fileCode, scanOrder, isRemote);
						}
						scanOrderMap.put(scanOrder, isRemote);

						if (firstIsRemote == null) {
							firstIsRemote = isRemote;
						} else {
							if (!isRemote.equals(firstIsRemote)) {
								isRemoteValid = false;
							}
						}
					}
				}
				if (scanOrderMap.size() > 0) {
					if (isRemoteValid) {
						for(ScannedImage item : imageTableView.getItems()) {
							if (!fileCode.equals(item.fileCodeProperty().getValue())) {
								continue;
							}
							item.isRemoteProperty().setValue(firstIsRemote);
						}
					} else {
						errorMessageMap.put(fileCode, String.format("序號 %s 視訊投保件 設置不一致！", String.join("、", scanOrderMap.keySet())));
					}
				}
			}
		}

		int errorCount = errorMessageMap.size();
		if (errorCount > 0) {
			isValid = false;
			String errorMessage = "";
			int i = 0;
			for (String fileCode : errorMessageMap.keySet()) {
				errorMessage += (i==0 ? "" : "\r\n") + (errorCount==1 ? "" : ("保單 " + fileCode + "，")) + errorMessageMap.get(fileCode);
				i++;
			}
			DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, errorMessage);
		}

		return isValid;
	}

	private boolean isInsertOrReplaceAction() {
		String actionInsert = null;
		String actionReplace = null;
		boolean isInsertOrReplace = false;

		if (imageTableView.getItems().size()>1) {
			for(ScannedImage item : imageTableView.getItems()) {
				actionInsert = item.actionInsertProperty().getValue();
				actionReplace = item.actionReplaceProperty().getValue();
				if ("Y".equals(actionInsert) || "Y".equals(actionReplace)) {
					isInsertOrReplace = true;
					break;
				}
			}
		} else {
			// 單張影像進行插入/替換時,不檢核
		}

		if (isInsertOrReplace) {
			DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "存在插入或替換文件，不可執行上傳全部，請先將插入或替換文件上傳！");
			return true;
		}

		return false;
	}

	private void fillScanConfig(ScanConfig scanConfig, ScannedImage imageItem) {
		if (imageItem==null) {
			logger.debug("imageItem=null");
			return;
		}

		String orgCode = scanConfig.getOrgCode();
		String orgName = scanConfig.getOrgName();
		String deptId = scanConfig.getDeptId();
		String deptName = scanConfig.getDeptName();
		String batchDepTypeValue = scanConfig.getBatchDepTypeValue();
		String fromQueryPage = scanConfig.getFromQueryPage();
		String bizDept = scanConfig.getBizDept();
		String isGID = scanConfig.getIsGID();
		String rocDate = scanConfig.getRocDate();
		String updateRole = scanConfig.getUpdateRole();
		String empId = scanConfig.getEmpId();
		String step = scanConfig.getStep();
		String defBatchDepType = scanConfig.getDefBatchDepType();
		String defBatchDate = scanConfig.getDefBatchDate();
		String batchDepType = scanConfig.getBatchDepType();
		String batchDate = scanConfig.getBatchDate();

		if (logger.isDebugEnabled()) {
			logger.debug("orgCode={}, orgName={}, deptId={}, deptName={}, batchDepTypeValue={}, fromQueryPage={}", orgCode, orgName, deptId, deptName, batchDepTypeValue, fromQueryPage);
			logger.debug("bizDept={}, isGID={}, rocDate={}, updateRole={}, empId={}, step={}", bizDept, isGID, rocDate, updateRole, empId, step);
			logger.debug("launchParamQueryFromPage={}, defBatchDepType={}, defBatchDate={}, batchDepType={}, batchDate={}", launchParamQueryFromPage, defBatchDepType, defBatchDate, batchDepType, batchDate);
		}

		imageItem.orgCodeProperty().set(orgCode); //組織代碼
		imageItem.orgNameProperty().set(orgName); //組織編碼
		imageItem.deptIdProperty().set(deptId); //登入者所屬部門ID
		imageItem.deptNameProperty().set(deptName); //部室名稱
		imageItem.batchDepTypeValueProperty().set(batchDepTypeValue);//批次號碼-部門類別值
		imageItem.fromQueryPageProperty().set(fromQueryPage); //是否從查詢網頁帶起此控件
		imageItem.bizDeptProperty().set(bizDept); //商業部門
		imageItem.isGIDProperty().set(isGID); //是否為團險
		imageItem.rocDateProperty().set(rocDate); //紀錄日期
		imageItem.updateRoleProperty().set(updateRole); //登入使用者是否有異動權限
		imageItem.empIdProperty().set(empId); //員工編號
		imageItem.stepProperty().set(step);

		if (launchParamQueryFromPage) {
			imageItem.batchDepTypeProperty().set(defBatchDepType);//批次號碼-部門類別
			imageItem.batchDateProperty().set(defBatchDate); //批次日期
		} else {
			imageItem.batchDepTypeProperty().set(batchDepType);//批次號碼-部門類別
			imageItem.batchDateProperty().set(batchDate); //批次日期
		}
	}

	private void removeUploadedItems() {
		List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
		int rowCount = imageTableView.getItems().size();
		int removeCount = 0;
		String errorMessage = null;

		if (logger.isDebugEnabled()) {
			logger.debug("removeUploadedItems()");
		}

		for (int i=rowCount-1; i>=0; i--) {
			ScannedImage removeItem = imageTableView.getItems().get(i);
			String scanOrder = removeItem.scanOrderProperty().getValue();
			String fileURL = removeItem.fileURLProperty().getValue();
			Integer uploadStatus = removeItem.uploadStatusProperty().getValue();
			if (logger.isDebugEnabled()) {
				logger.debug("Image item: scanOrder={}, fileURL={}, uploadStatus={}", scanOrder, fileURL, uploadStatus);
			}

			if (uploadStatus == null || uploadStatus.intValue() != UploadView.UPLOAD_STATUS_UPLOADED) {
				continue;
			}

			// UAT-IR-479059，改在上傳後就刪除實體檔案
			/*
			Path file = Paths.get(fileURL);
			try {
				Files.delete(file);
			} catch (Exception e) {
				errorMessage = String.format("無法刪除已上傳的影像檔 %s ！", fileURL);
				logger.error(errorMessage, e);
			}
			*/

			recordList.remove(i);
			imageTableView.getItems().remove(i);
			removeCount++;

		}

		if (logger.isDebugEnabled()) {
			logger.debug("removeCount={}", removeCount);
		}

		if (removeCount == 0) {
			return;
		}

		imageTableView.getSelectionModel().clearSelection();

		// 更新掃瞄序號
		recordSetHelper.resetScanOrder(recordList);
		try {
			// 回寫 XML 檔案
			recordSetHelper.marshalToFile(recordSet);
		} catch (JAXBException e) {
			errorMessage = String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
			logger.error(errorMessage, e);
		}
		// 重新載入 XML 檔案
		setupTableData();
	}

	public class UploadPopupController {
        @FXML
        private JFXListView<?> uploadPopupList;

        @FXML
        private void submit() {
            switch ( uploadPopupList.getSelectionModel().getSelectedIndex() ) {
                case 0:
                	onAction_btnUpload();
                	break;
                case 1:
                	onAction_btnUploadAll();
                	break;
                case 2:
                	onAction_btnUploadStatus();
                	break;
                default:
                	break;
        	}
            uploadPopupList.getSelectionModel().clearSelection();
        }
    }

	public void registerUploadBadgeButton(JFXButton button) {
		this.btnUploadBadge = button;
		this.btnUploadBadge.setOnAction(action -> {
    		onAction_btnUploadBadge();
		});
//        final JFXTooltip btnUploadTooltip = new JFXTooltip("上傳 (Ctrl+U)\n上傳全部 (Ctrl+Y)\n詳細資訊 (Ctrl+D)");
//        JFXTooltip.install(btnUploadBadge, btnUploadTooltip, Pos.BOTTOM_CENTER);
	}

	public void registerLoginButton(JFXButton button) {
		this.btnLogin = button;
		this.btnLogin.setOnAction(action -> {
    		onAction_btnLogin();
		});
        btnLoginTooltip = new JFXTooltip("登入 (Ctrl+L)");
        JFXTooltip.install(btnLogin, btnLoginTooltip, Pos.BOTTOM_CENTER);
	}

	public void registerRunningModeButton(JFXButton button) {
		this.btnRunningMode = button;
		this.btnRunningMode.setOnAction(action -> {
    		onAction_btnRunningMode();
		});
		final JFXTooltip btnRunningModeTooltip = new JFXTooltip("執行模式 (Ctrl+O)");
        JFXTooltip.install(btnRunningMode, btnRunningModeTooltip, Pos.BOTTOM_CENTER);
	}

	public void registerSettingsButton(JFXButton button) {
		this.btnSettings = button;
		this.btnSettings.setOnAction(action -> {
    		onAction_btnSettings();
		});
        final JFXTooltip btnSettingsTooltip = new JFXTooltip("設定 (Ctrl+P)");
        JFXTooltip.install(btnSettings, btnSettingsTooltip, Pos.BOTTOM_CENTER);
	}

	public void registerLoginStatusLabel(Label label) {
		this.loginStatusLabel = label;
		this.loginStatusLabel.textProperty().bind(loginStatusLabelText);
		this.loginStatusLabel.styleProperty().bind(loginStatusLabelCss);
	}

	public void registerUploadBadge(JFXBadge badge) {
		this.uploadBadge = badge;
		this.uploadBadge.textProperty().bind(Bindings.size((imageTableView.getItems())).asString());
	}

	public void registerServerLabel(Label label) {
		this.serverLabel = label;
		this.serverLabel.textProperty().bind(serverLabelText);
//		this.serverLabel.styleProperty().bind(loginStatusLabelCss);
	}

	public void setOnKeyPressed(KeyEvent keyEvent) {
		switch (keyEvent.getCode()) {
		case L:
			if (keyEvent.isControlDown()) {
				btnLogin.fire();
			    keyEvent.consume();
			}
			break;
		case O:
			if (keyEvent.isControlDown()) {
				btnRunningMode.fire();
			    keyEvent.consume();
			}
			break;
		case P:
			if (keyEvent.isControlDown()) {
				btnSettings.fire();
			    keyEvent.consume();
			}
			break;
		case M:
			if (keyEvent.isControlDown()) {
				btnUpdate.fire();
			    keyEvent.consume();
			}
			break;
		case R:
			if (keyEvent.isControlDown()) {
				btnRemove.fire();
			    keyEvent.consume();
			}
			break;
		case A:
			if (keyEvent.isControlDown()) {
				btnRemovePartial.fire();
			    keyEvent.consume();
			}
			break;
		case C:
			if (keyEvent.isControlDown()) {
				btnCopy.fire();
			    keyEvent.consume();
			}
			break;
		case I:
			if (keyEvent.isControlDown()) {
				btnImport.fire();
			    keyEvent.consume();
			}
			break;
		case T:
			if (keyEvent.isControlDown()) {
				btnScanSettings.fire();
			    keyEvent.consume();
			}
			break;
		case S:
			if (keyEvent.isControlDown()) {
				btnScan.fire();
			    keyEvent.consume();
			}
			break;
		case EQUALS:
			if (keyEvent.isControlDown()) {
				btnZoomIn.fire();
			    keyEvent.consume();
			}
			break;
		case MINUS:
			if (keyEvent.isControlDown()) {
				btnZoomOut.fire();
			    keyEvent.consume();
			}
			break;
		case DIGIT0:
			if (keyEvent.isControlDown()) {
				btnRotateRight.fire();
			    keyEvent.consume();
			}
			break;
		case DIGIT9:
			if (keyEvent.isControlDown()) {
				btnRotateLeft.fire();
			    keyEvent.consume();
			}
			break;
		case U:
			if (keyEvent.isControlDown()) {
				onAction_btnUpload();
			    keyEvent.consume();
			}
			break;
		case Y:
			if (keyEvent.isControlDown()) {
				onAction_btnUploadAll();
			    keyEvent.consume();
			}
			break;
		case D:
			if (keyEvent.isControlDown()) {
				onAction_btnUploadStatus();
			    keyEvent.consume();
			}
			break;
		default:
			break;
		}
	}

	public void showSnackbarWithClose(String message) {
		snackbar.enqueue(
	    	new SnackbarEvent(
	    		new JFXSnackbarLayout(message, "關閉", action -> snackbar.close() ), 
	    		Duration.INDEFINITE, 
	    		null
	    	)
	    );
	}

	public void showSnackbar(String message) {
		showSnackbar(message, false, Duration.INDEFINITE);
	}

	public void showSnackbar(String message, boolean closePreiousMessage) {
		showSnackbar(message, closePreiousMessage, Duration.INDEFINITE);
	}

	public void showSnackbar(String message, Duration timeout) {
		showSnackbar(message, false, timeout);
	}

	public void showSnackbar(String message, boolean closePreiousMessage, Duration timeout) {
		if (closePreiousMessage) {
			snackbar.close();
		}
		snackbar.enqueue(
	    	new SnackbarEvent(
	    		new JFXSnackbarLayout(message), 
	    		timeout, 
	    		null
	    	)
	    );
	}

	public void hideSnackbar() {
		snackbar.close();
	}

	private void showProcessingPane(String message) {
	    Stage stage = (Stage)this.getScene().getWindow();
//		stage.setOpacity(0.85f);

		processText.setText(message != null ? message : "處理中...");
		processPane.setVisible(true);
	}

	private void hideProcessingPane() {
	    Stage stage = (Stage)this.getScene().getWindow();
//		stage.setOpacity(1.0f);

		processPane.setVisible(false);
	}

}
