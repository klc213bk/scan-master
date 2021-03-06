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
    private JFXTextField txtOrgName; // ????????????
    @FXML
    private JFXTextField txtDeptName; // ????????????
	@FXML
	private JFXComboBox<Pair<String, String>> cbbMainFileType; // ???????????????
	@FXML
	private JFXComboBox<Pair<String, String>> cbbFileType; // ???????????????
    @FXML
    private JFXTextField txtFileCode; // ????????????
    @FXML
    private JFXTextField txtFilePage; // ??????
	@FXML
	private JFXComboBox<String> cbbBoxNumber; // ??????
    @FXML
	private JFXComboBox<Pair<String, String>> cbbBatchDeptType; // ????????????-?????????
    @FXML
    private JFXTextField txtBatchDate; // ??????
    @FXML
    private JFXTextField txtBatchArea; // ??????
    @FXML
    private JFXTextField txtBatchDocType; // ?????????
    @FXML
    private JFXTextField txtCompanyCode; // ?????????
    @FXML
    private JFXTextField txtPersonalCode; // ?????????
    @FXML
	private Label lbActionReplace;
    @FXML
	private JFXComboBox<Pair<String, String>> cbbActionReplace; // ??????
    @FXML
	private Label lbActionInsert;
    @FXML
	private JFXComboBox<Pair<String, String>> cbbActionInsert; // ??????
	@FXML
    private Label lbSendEmail;
	@FXML
	private JFXComboBox<Pair<String, String>> cbbSendEmail; // ?????????EMAIL
    @FXML
	private Label lbIsRemote;
    @FXML
	private JFXComboBox<Pair<String, String>> cbbIsRemote; // ???????????????
    @FXML
    private JFXTextField txtRemark; // ????????????
    @FXML
    private Label lbDeptId;
	@FXML
    private Label lbCompanyCode;
	@FXML
	private JFXComboBox<Pair<String, String>> cbbDuplexMode; // ????????????
	@FXML
	private JFXComboBox<Pair<String, String>> cbbColorMode; // ????????????
	@FXML
	private Hyperlink hlSourceName; // ?????????????????????
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

    	parseLaunchParameters(launchParameters); // App ???????????????
    	imageTableStackPane.prefHeightProperty().bind(MainView.this.getScene().heightProperty());
    	hlSourceName.setText("*?????????");
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
    		// App ??????????????????

    		Stage stage = ((Stage)MainView.this.getScene().getWindow());
//    		stage.setAlwaysOnTop(true);
//    		stage.setAlwaysOnTop(false);
    		stage.requestFocus();
    		stage.toFront();

    		// ?????? A. ??? fromQueryPage=true(App?????????????????????????????????????????????) & launchParamQueryFromPage=false(App????????????????????????????????????)
    		//		1. ?????????????????????[??????/??????]???????????????????????????????????????????????????????????????????????????
    		//		2. ?????? userName(????????????) ??? launchParamUserName(???APP?????????UserName) ????????????
    		//			2.1 ??????????????????????????? launchParamUserName
    		//			2.1 ?????????????????????????????????????????? userName ????????????
			// ?????? B. ??? fromQueryPage=false(App????????????????????????????????????????????????) & launchParamQueryFromPage=true(App?????????????????????????????????)
    		//		1. ?????????????????????[??????]???????????????????????????????????????????????????????????????????????????
    		//		2. ??? fromWeb=true???????????? userName(????????????) ??? launchParamUserName(???APP?????????UserName) ????????????
    		//			2.1 ??????????????????????????? launchParamUserName
    		//			2.1 ?????????????????????????????????????????? userName ????????????
    		// ?????? C. ??? fromQueryPage=true(App?????????????????????????????????????????????) & launchParamQueryFromPage=true(App?????????????????????????????????)
    		//		1. ?????? userName(????????????) ??? launchParamUserName(???APP?????????UserName) ????????????
    		//			2.1 ??????????????????????????? launchParamUserName??????????????????????????????
    		//			2.1 ?????????????????????????????????????????? userName ?????????????????????????????????
			// ?????? D. ??? fromQueryPage=false(App????????????????????????????????????????????????) & launchParamQueryFromPage=false(App????????????????????????????????????)
    		//		1. ??? fromWeb=true???????????? userName(????????????) ??? launchParamUserName(???APP?????????UserName) ????????????
    		//			2.1 ??????????????????????????? launchParamUserName??????????????????????????????
    		//			2.1 ?????????????????????????????????????????? userName ?????????????????????????????????

    		// Step 1: ??????????????????????????????
    		if (fromQueryPage != launchParamQueryFromPage) {
    			String msg = fromQueryPage ? "???APP???????????????[??????]????????????????????????[??????/??????]?????????" : "???APP???????????????[??????/??????]????????????????????????[??????]?????????";
				String result = DialogUtil.showConfirm(MainView.this.getScene().getWindow(), msg, new String[] {"???(??????)", "???"});

				if (logger.isDebugEnabled()) {
					logger.debug(msg + " --> " + result);
				}

				if ("???".equals(result)) {
					return;
				}
    		}

			String propUserName = PropertiesCache.getInstance().getProperty(PropertiesCache.PROP_KEY.EBAO_USER_NAME.propName());
			if (logger.isDebugEnabled()) {
				logger.debug("fromWeb={}, userName={}, launchParamUserName={}, propUserName={}, needToRelogin={}", fromWeb, userName, launchParamUserName, propUserName, needToRelogin);
			}

			// Step 2: ????????????????????????????????????????????????????????????????????? UserName ????????????
    		if (fromWeb) {
        		if (propUserName==null || "".equals(propUserName.trim())) {
        			needToRelogin = true;
        		} else if (!userName.equals(propUserName)) {
        			needToRelogin = true;
        			String msg = String.format("???????????????????????????????????? %s ????????????????????????????????? %s ??????????????? %s ???????????????", propUserName, userName, userName);
        			showSnackbarWithClose(msg);
        		}
    		} else {
    			if (propUserName==null || "".equals(propUserName)) {
        			needToRelogin = false;
        		}
    		}

    		// Step 3: ?????? launchParam* ??????
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
				logger.debug("?????? launchParam* ??????");
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
    		// App ???????????????
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
        			String msg = String.format("???????????????????????????????????? %s ????????????????????????????????? %s ??????????????? %s ???????????????", propUserName, launchParamUserName, launchParamUserName);
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

//			String msg = String.format("???????????????????????????????????? %s ????????????????????????????????? %s ??????????????? %s ???????????????", 1, 2, 3);
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
				msg = "?????????[??????/????????????]???????????????????????????????????????????????????????????????????????????????????????[????????????]???";
				String result = DialogUtil.showConfirm(MainView.this.getScene().getWindow(), msg, new String[] {"???(??????)", "???"});
				if ("???".equals(result)) {
					return;
				}
				launchParamQueryFromPage = false;
				if (this.scanConfig!=null) {
		    		this.scanConfig.setFromQueryPage(Boolean.FALSE.toString());
				}
		    	setupUIValues();
			} else {
				msg = "?????????[????????????]????????????????????????????????????????????????";
				DialogUtil.showMessage(MainView.this.getScene().getWindow(), msg);
			}
		});
    }

    private void onAction_btnUpdate() {
		Platform.runLater(() -> {
			// ?????? imagerecordset.xml ??????????????????
			if (recordSetHelper.xmlFileChanged()) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "???????????????????????????????????????????????????????????????????????????");
				loadCachedData(true);
				setupUIValues();
				return;
			}
			int selectedIndex = imageTableView.getSelectionModel().getSelectedIndex();

			storeScannedImage();

			// ?????? XML ??????
    		try {
    			recordSetHelper.marshalToFile(recordSet);
    		} catch (JAXBException e) {
    			String errMsg = String.format("????????????????????? %s ???", ImageRecordHelper.RECORD_SET_FILE_NAME);
    			DialogUtil.showErrorMessageAndWait(MainView.this.getScene().getWindow(), errMsg);
				logger.error(errMsg, e);
    		}

    		// ???????????? XML ??????
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

		// ???????????????????????????
		boolean valid = (selectedIndex < 0) ? false : validateScannedImage(selectedItem);

		if (valid) {

			// ????????????????????????

			String fileType = null == cbbFileType.getSelectionModel().getSelectedItem() ? "" : cbbFileType.getSelectionModel().getSelectedItem().getKey();
	    	String oriFileCode = selectedItem.fileCodeProperty().getValue();
	    	String fileCode = txtFileCode.getText();
	    	String actionReplace = null==cbbActionReplace.getSelectionModel().getSelectedItem() ? null : cbbActionReplace.getSelectionModel().getSelectedItem().getKey();
	    	String actionInsert = null==cbbActionInsert.getSelectionModel().getSelectedItem() ? null : cbbActionInsert.getSelectionModel().getSelectedItem().getKey();
	    	String actionType = "";
	    	if (null != actionReplace && Constant.YN_YES.equals(actionReplace)) {
	    		actionType = "??????";
	    	} else if (null != actionInsert && Constant.YN_YES.equals(actionInsert)) {
	    		actionType = "??????";
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

	    	// PCR 244580 BR-CMN-PIC-019 ????????????????????????
	    	if ( ( scanConfig.isDeptPos() || "LA".equals(scanConfig.getBatchDepTypeValue() ) ) && 
	    		 !fileCode.equals(oriFileCode) ) { // ?????????????????????????????????,??????????????????????????????
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

	    			// ????????????????????????FILE_CODE????????????
	    			String itemFileCode = item.fileCodeProperty().getValue();
	    			item.fileCodeProperty().setValue(fileCode);
	        		if (logger.isDebugEnabled() ) {
	        			logger.debug("index={}, newFileCode={}, itemFileCode={}", i, fileCode, itemFileCode);
	        		}
	        		recordSetHelper.saveTiffData(item, recordList.get(i));
	    		}
	    	}

		}

    	// ??????????????????
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
        	if (ObjectsUtil.isEmpty(companyCode) && ObjectsUtil.isEmpty(personalCode)) { // ??????????????????????????????
        		errorMessage += "??????????????????????????????!\n";
        	} else {
        		if (ObjectsUtil.isNotEmpty(companyCode) && companyCode.length() != 8 ) {
            		errorMessage += "???????????????????????????????????????8???!\n";
        		}
        		if (ObjectsUtil.isNotEmpty(personalCode) && personalCode.length() != 6 ) {
            		errorMessage += "???????????????????????????????????????6???!\n";
        		}
        	}
    	}

    	if (ObjectsUtil.isEmpty(mainFileType)) {
    		errorMessage += "????????? ???????????????\n";
    	}
    	if (ObjectsUtil.isEmpty(fileType)) {
    		errorMessage += "????????? ???????????????\n";
    	}
    	if (ObjectsUtil.isEmpty(fileCode)) {
    		errorMessage += "????????? ????????????\n";
    	}
    	if (ObjectsUtil.isEmpty(filePage) && "N".equals(signature)) {
    		errorMessage += "????????? ??????\n";
    	}
    	if (ObjectsUtil.isNotEmpty(filePage) && "Y".equals(signature)) {
    		errorMessage += "<???????????????>??????????????????!\n";
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

			// ????????????????????????
			String scanOrder = removeItem.scanOrderProperty().getValue();
			String fileCode = removeItem.fileCodeProperty().getValue();
			String fileName = removeItem.fileNameProperty().getValue();
			String fileURL = removeItem.fileURLProperty().getValue();
    		Path file = Paths.get(fileURL);
    		try {
				Files.delete(file);
			} catch (NoSuchFileException e) {
			} catch (IOException e) {
				errorMessage = String.format("??????:%s ????????????:%s???????????????????????? %s ???", scanOrder, fileCode, fileName);
				logger.error(errorMessage, e);
			}

    		if (null == errorMessage) {
    			// ????????????????????? XML ??????
    			List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
    			recordList.remove(selectedIndex);
        		try {
        			recordSetHelper.marshalToFile(recordSet);
        		} catch (JAXBException e) {
    				errorMessage = String.format("????????????????????? %s ???", ImageRecordHelper.RECORD_SET_FILE_NAME);
    				logger.error(errorMessage, e);
        		}
    		}

    		if (null == errorMessage) {
        		// ?????? TableView ??????
    			int rowCount = imageTableView.getItems().size();
    			imageTableView.getSelectionModel().clearSelection();
    			imageTableView.getItems().remove(selectedIndex);
    			// ???????????????
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

	    	// ??????????????????
			String msg = String.format("????????????????????????%s???%s???", from, to);
			String result = DialogUtil.showConfirm(MainView.this.getScene().getWindow(), msg, new String[] {btnOkText, btnCancelText});
			if (btnCancelText.equals(result)) {
	    		imageTableView.requestFocus();
				return;
			}

			// ????????????????????????
	    	if (checkImageFileExist(from, to)) {
				result = DialogUtil.showConfirm(MainView.this.getScene().getWindow(), "?????????????????????????????????????????????", new String[] {btnOkText, btnCancelText});
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

			// ????????????????????????????????????
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

			// ????????????????????????
			String fileCode = removeItem.fileCodeProperty().getValue();
			String fileName = removeItem.fileNameProperty().getValue();
			String fileURL = removeItem.fileURLProperty().getValue();
    		Path file = Paths.get(fileURL);
    		try {
				Files.delete(file);
			} catch (NoSuchFileException e) {
			} catch (IOException e) {
				itemDetail = String.format("??? ??????:%s ????????????:%s ?????????:%s", scanOrder, fileCode, fileName);
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
			errorMessage = "??????????????????????????????\n\n" + errorMessage;
		}

		if (removeCount > 0) {
			try {
				// ????????????????????? XML ??????
				recordSetHelper.marshalToFile(recordSet);
			} catch (JAXBException e) {
				errorMessage = errorMessage + "\n" + String.format("????????????????????? %s ???", ImageRecordHelper.RECORD_SET_FILE_NAME);
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
				errorMessage = String.format("????????????????????????????????? %s ???", copyItem.fileNameProperty().getValue());
				logger.error(errorMessage, e);
			} catch (IOException e) {
				errorMessage = String.format("????????????????????? %s ???", copyItem.fileNameProperty().getValue());
				logger.error(errorMessage, e);
			}

			if (null == errorMessage) {
    			// ????????????????????? XML ??????
    			List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
    			TiffRecord tiffRecord = recordList.get(selectedIndex);
        		try {
        			newTiffRecord = recordSetHelper.cloneTiffRecord(tiffRecord, newFilePath.getFileName().toString());
        			recordList.add(selectedIndex+1, newTiffRecord);
        			recordSetHelper.marshalToFile(recordSet);
        		} catch (IllegalAccessException e) {
    				errorMessage = "???????????????????????????";
    				logger.error(errorMessage, e);
				} catch (InvocationTargetException e) {
    				errorMessage = "???????????????????????????";
    				logger.error(errorMessage, e);
        		} catch (JAXBException e) {
    				errorMessage = String.format("????????????????????? %s ???", ImageRecordHelper.RECORD_SET_FILE_NAME);
    				logger.error(errorMessage, e);
				}
    		}

    		if (null == errorMessage) {
    			// ?????? TableView ??????????????????
				ScannedImage newItem = recordSetHelper.convert(newTiffRecord);
				newItem.indexNoProperty().setValue(copyItem.indexNoProperty().getValue()+1);
    			imageTableView.getSelectionModel().clearSelection();
    			imageTableView.getItems().add(selectedIndex+1, newItem);
    			// ????????????????????????
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
				new FileChooser.ExtensionFilter("TIFF Files", "*.tif", "*.tiff"), //?????? FileChooser ????????? tiff/tif ??????????????????
				new FileChooser.ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg"),  //?????? FileChooser ????????? jpeg/jpg ??????????????????
				new FileChooser.ExtensionFilter("TIFF & JPEG Files", "*.tif", "*.tiff", "*.jpg", "*.jpeg") //?????? FileChooser ????????? tiff/tif & jpeg/jpg ??????????????????
			);
			File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());
			if (null == selectedFile) {
				return;
			}

        	showProcessingPane("???????????????");

        	//?????? TableView ?????????????????????
			ScannedImage latestImage = null;
			int rowCount = imageTableView.getItems().size();
			if (rowCount > 0) {
				latestImage = imageTableView.getItems().get(rowCount-1);
			}

			// ???UI Thread?????????????????????Thread??????
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
		    				errorMessage = String.format("????????????????????? %s ???", ImageRecordHelper.RECORD_SET_FILE_NAME);
		    				logger.error(errorMessage, e);
						}
	    			}

	            	hideProcessingPane();

	            	if (null == errorMessage) {
		    			// ?????? TableView ??????????????????
		    			if (importedRecordList != null) {
		    				for (TiffRecord newTiffRecord : importedRecordList) {
		    					ScannedImage newItem = recordSetHelper.convert(newTiffRecord);
		    					newItem.indexNoProperty().setValue(++latestImageIndexNo);
		    	    			imageTableView.getItems().add(newItem);
		    				}
		        			// ????????????????????????
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
					String message = ( sourceList==null || sourceList.size()==0 ) ? "???????????????????????????????????????????????????" : "?????????????????????";
	    			DialogUtil.showErrorMessageAndWait(MainView.this.getScene().getWindow(), message);
					return;
				}
			}

			String colorMode = null == cbbColorMode.getSelectionModel().getSelectedItem() ? null : cbbColorMode.getSelectionModel().getSelectedItem().getKey();
	    	String duplexMode = null == cbbDuplexMode.getSelectionModel().getSelectedItem() ? null : cbbDuplexMode.getSelectionModel().getSelectedItem().getKey();
	    	if ( ObjectsUtil.isEmpty(colorMode) || ObjectsUtil.isEmpty(duplexMode) ) {
    			DialogUtil.showErrorMessageAndWait(MainView.this.getScene().getWindow(), "???????????????????????????????????????");
    			return;
	    	}

			showProcessingPane("???????????????");

        	//?????? TableView ?????????????????????
			ScannedImage latestImage = null;
			int rowCount = imageTableView.getItems().size();
			if (rowCount > 0) {
				latestImage = imageTableView.getItems().get(rowCount-1);
			}

			// ???UI Thread?????????????????????Thread??????
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
		    				errorMessage = String.format("????????????????????? %s ???", ImageRecordHelper.RECORD_SET_FILE_NAME);
		    				logger.error(errorMessage, e);
						}
	    			}

	            	hideProcessingPane();

	            	if (null == errorMessage) {
		    			// ?????? TableView ??????????????????
		    			if (importedRecordList != null) {
		    				for (TiffRecord newTiffRecord : importedRecordList) {
		    					ScannedImage newItem = recordSetHelper.convert(newTiffRecord);
		    					newItem.indexNoProperty().setValue(++latestImageIndexNo);
		    	    			imageTableView.getItems().add(newItem);
		    				}
		        			// ????????????????????????
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
	            	String errorMessage = "????????????????????? " + t.getSource().getException().getMessage();
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
					String message = ( sourceList==null || sourceList.size()==0 ) ? "???????????????????????????????????????????????????" : "?????????????????????";
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
        		// ?????????????????????????????? Window Handle ?????? Asprise Imaging ???????????????????????????????????????????????????????????????
				//result = ScanUtil.setupSource(MainView.this.getScene().getWindow(), defaultScaner);

    	    	if (!isCreateTempOwnerFrame) {
        			// ????????????1: ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
    				result = ScanUtil.setupSource(defaultScaner);
    	    	} else {
    	        	// ????????????2: ??????????????????????????????????????????JFrame??????????????????????????????????????????????????????????????????????????????????????????????????????...????????????????????????
    	        	ownerFrame = createTempOwnerFrame(false);
    				result = ScanUtil.setupSource(ownerFrame, defaultScaner);
    	    	}

        		if (logger.isDebugEnabled()) {
    				logger.debug(result == null ? "(null)" : result.toJson(true));
        		}
			} catch (TwainException e) {
				DialogUtil.showErrorMessageAndWait(MainView.this.getScene().getWindow(), "???????????????????????? " + e.getMessage());
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
	    	//????????????????????? JFrame ??????, ?????? ScanApp ?????? (????????????????????????????????????????????????????????????????????????????????????)
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

    	// ?????????????????????????????? Window Handle ?????? Asprise Imaging ???????????????????????????????????????????????????????????????
    	//sourceName = ScanUtil.showSelectScanerDialog(MainView.this.getScene().getWindow());

    	if (!isCreateTempOwnerFrame) {
        	// ????????????1: ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
        	sourceName = ScanUtil.showSelectScanerDialog();
    	} else {
        	// ????????????2: ??????????????????????????????????????????JFrame??????????????????????????????????????????????????????????????????????????????????????????????????????...????????????????????????
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
			rotateImage(90); //????????????90???
		});
    }

    private void onAction_btnRotateLeft() {
    	Platform.runLater(() -> {
			if (null == previewImageView.getImage()) 
				return;
			rotateImage(270); //????????????90???
		});
    }

    private void rotateImage(int rotation) {
//    	showSnackbar("???????????????..."); //??????????????????

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
			DialogUtil.showErrorMessage(MainView.this.getScene().getWindow(), "????????????????????????\n"+e.getMessage());
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

//    	hideSnackbar(); //??????????????????
    }

    private void onAction_btnUpload() {
		Platform.runLater(() -> {
		    String updateStatus = System.getProperty(ATTR_CONFIG_UPDATE_STATUS, "");
		    if (!"AppIsUpToDate".equals(updateStatus)) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
				return;
		    }

			List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
			int selectedIndex = imageTableView.getSelectionModel().getSelectedIndex();
			ScannedImage selectedItem = imageTableView.getSelectionModel().getSelectedItem();
			if (selectedIndex<0) {
				DialogUtil.showMessageAndWait(MainView.this.getScene().getWindow(), "???????????????????????????");
				return;
			}

			// ?????? imagerecordset.xml ??????????????????
			if (recordSetHelper.xmlFileChanged()) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "???????????????????????????????????????????????????????????????????????????");
				loadCachedData(true);
				setupUIValues();
				return;
			}

			// ???????????????????????????
			if (imageFileNotExist()) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "????????????????????????????????????????????????\nScan files have been removed from disk! It can not be uploaded!");
				return;
			}

			boolean isUploadAll = false; // ??????????????????????????????

			// ????????????????????? FilePage ????????????
			// ??????????????????TextFormatter ?????????

			// BR-CMN-PIC-016 ?????????????????????, ???Server Side??????
			// BR-CMN-PIC-017 ??????????????????????????????	
			boolean isBatchNoValid = validateBatchNo(isUploadAll);
			if (!isBatchNoValid) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "????????????????????????");
				return;
			}

			// BR-CMN-PIC-XXX ????????????????????????
			boolean isBoxNoValid = validateBoxNo(isUploadAll);
			if (!isBoxNoValid) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "??????????????????");
				return;
			}

			if (scanConfig.isDeptNB()) {
				// PCR_386372 - ?????????????????????????????????????????????Email??????
				boolean sendEmailValid = validateSendEmail(isUploadAll);
				if (!sendEmailValid) {
					return;
				}

				// PCR_268354 - ???????????????????????????????????????(???????????????)
				boolean isRemoteValid = validateIsRemote(isUploadAll);
				if (!isRemoteValid) {
					return;
				}
			}  

			// BR-CMN-PIC-005 ????????????????????????
			boolean isFilePageValid = validateFilePage(isUploadAll);
			if (!isFilePageValid) {
				return;
			}

			selectedItem.stepProperty().set("upload");
			// ??? ScanConfig ???????????? imagerecord
			//fillScanConfig(scanConfig, selectedItem); // UAT-IR-478019 ????????????????????????????????????????????????????????????????????????????????????????????????????????????
			recordSetHelper.saveTiffData(selectedItem, recordList.get(selectedIndex));

			// ?????? XML ??????
    		try {
    			recordSetHelper.marshalToFile(recordSet);
    		} catch (JAXBException e) {
    			String errMsg = String.format("????????????????????? %s ???", ImageRecordHelper.RECORD_SET_FILE_NAME);
    			DialogUtil.showErrorMessageAndWait(MainView.this.getScene().getWindow(), errMsg);
				logger.error(errMsg, e);
				return;
    		}

			// ?????????????????? eBao Server
			if (!checkLogin()) {
				this.showSnackbar("????????????????????????????????????????????????????????? eBao Server???", true, Duration.seconds(5.0));
				return;
			}

			UploadProcessSummary uploadSummary = DialogUtil.showUploadDialog(MainView.this, this.scanConfig, imageTableView.getItems(), selectedIndex);
			if (logger.isDebugEnabled()) {
				logger.debug("dialogTitle={}, dialogMessage={}, cntSuccess={}, cntUpload={}, cntFailed={}", uploadSummary.getDialogTitle(), uploadSummary.getDialogMessage(), uploadSummary.getCntSuccess(), uploadSummary.getCntUpload(), uploadSummary.getCntFailed());
			}

	    	removeUploadedItems();

			String dialogTitle = uploadSummary.getDialogTitle()==null ? "??????" : uploadSummary.getDialogTitle();
			String dialogMessage = uploadSummary.getDialogMessage()==null ? 
					String.format("??????????????????????????????%s??????????????????%s??????????????????%s", uploadSummary.getCntSuccess(), uploadSummary.getCntUpload(), uploadSummary.getCntFailed()) : 
					uploadSummary.getDialogMessage();
			DialogUtil.showMessage(MainView.this.getScene().getWindow(), dialogTitle, dialogMessage, true);
		});
    }

    private void onAction_btnUploadAll() {
		Platform.runLater(() -> {
		    String updateStatus = System.getProperty(ATTR_CONFIG_UPDATE_STATUS, "");
		    if (!"AppIsUpToDate".equals(updateStatus)) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
				return;
		    }

			List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
			if (imageTableView.getItems().size()==0) {
				DialogUtil.showMessageAndWait(MainView.this.getScene().getWindow(), "??????????????????????????????");
				return;
			}

			// ?????? imagerecordset.xml ??????????????????
			if (recordSetHelper.xmlFileChanged()) {
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "???????????????????????????????????????????????????????????????????????????");
				loadCachedData(true);
				setupUIValues();
				return;
			}

			// ???????????????????????????
			int notExistIndex = imageFilesNotExist();
			if (notExistIndex>-1) {
    			String msg = String.format("?????????(NO:%s)???????????????????????????????????????\nScan files(NO:%s) have been removed from disk! Please restart application to reload file list!", notExistIndex+1, notExistIndex+1);
				DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, msg);
				imageTableView.getSelectionModel().select(notExistIndex);
				imageTableView.scrollTo(notExistIndex);
				return;
			}

			// BR-CMN-PIC-006 ????????????????????????????????????
			if (isInsertOrReplaceAction()) {
				return;
			}

			boolean isUploadAll = true; // ??????????????????????????????
			boolean isValid = true;

			// BR-CMN-PIC-017 ??????????????????????????????
			if (isValid) {
				boolean isBatchNoValid = validateBatchNo(isUploadAll);
				if (!isBatchNoValid) {
					DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "????????????????????????");
					isValid = false;
				}
			}

			// BR-CMN-PIC-XXX ????????????????????????
			if (isValid) {
				boolean isBoxNoValid = validateBoxNo(isUploadAll);
				if (!isBoxNoValid) {
					DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "??????????????????");
					isValid = false;
				}
			}

			if (scanConfig.isDeptNB()) {
				// PCR_386372 - ?????????????????????????????????????????????Email??????
				if (isValid) {
					boolean sendEmailValid = validateSendEmail(isUploadAll);
					if (!sendEmailValid) {
						isValid = false;
					}
				}

				// PCR_268354 - ???????????????????????????????????????(???????????????)
				if (isValid) {
					boolean isRemoteValid = validateIsRemote(isUploadAll);
					if (!isRemoteValid) {
						isValid = false;
					}
				}
			}

			// BR-CMN-PIC-005 ????????????????????????
			if (isValid) {
				boolean isFilePageValid = validateFilePage(isUploadAll);
				if (!isFilePageValid) {
					isValid = false;
				}
			}

			for (int i=0; i<imageTableView.getItems().size(); i++) {
				ScannedImage imageItem = imageTableView.getItems().get(i);
				// ??? ScanConfig ???????????? imagerecord
				//fillScanConfig(scanConfig, imageItem); // UAT-IR-478019 ????????????????????????????????????????????????????????????????????????????????????????????????????????????
				recordSetHelper.saveTiffData(imageItem, recordList.get(i));
			}

			// ?????? XML ??????
    		try {
    			recordSetHelper.marshalToFile(recordSet);
    		} catch (JAXBException e) {
    			String errMsg = String.format("????????????????????? %s ???", ImageRecordHelper.RECORD_SET_FILE_NAME);
    			DialogUtil.showErrorMessageAndWait(MainView.this.getScene().getWindow(), errMsg);
				logger.error(errMsg, e);
				isValid = false;
    		}

			if (!isValid) {
				return;
			}

    		// ?????????????????? eBao Server
			if (!checkLogin()) {
				this.showSnackbar("????????????????????????????????????????????????????????? eBao Server???", true, Duration.seconds(5.0));
				return;
			}

			UploadProcessSummary uploadSummary = DialogUtil.showUploadDialog(MainView.this, this.scanConfig, imageTableView.getItems(), null);
			if (logger.isDebugEnabled()) {
				logger.debug("dialogTitle={}, dialogMessage={}, cntSuccess={}, cntUpload={}, cntFailed={}", uploadSummary.getDialogTitle(), uploadSummary.getDialogMessage(), uploadSummary.getCntSuccess(), uploadSummary.getCntUpload(), uploadSummary.getCntFailed());
			}

			removeUploadedItems();

			String dialogTitle = uploadSummary.getDialogTitle()==null ? "??????" : uploadSummary.getDialogTitle();
			String dialogMessage = uploadSummary.getDialogMessage()==null ? 
					String.format("??????????????????????????????%s??????????????????%s??????????????????%s", uploadSummary.getCntSuccess(), uploadSummary.getCntUpload(), uploadSummary.getCntFailed()) : 
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
                        return item==null || "".equals(item) ? "" : "Y".equals(item) ? "???" : "???";
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
                        return item==null || "".equals(item) ? "" : "Y".equals(item) ? "???" : "???";
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

    	//?????? TableView ??????????????????
    	imageTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    	imageTableView.setEditable(false);
    	imageTableView.getSelectionModel().selectedIndexProperty().addListener((obs, oldSelection, newSelection) -> {
    		//Platform.runLater(() -> {
        		if ( null != newSelection && -1 < newSelection.intValue()) {
    				ScannedImage record = imageTableView.getItems().get(newSelection.intValue());
    				setupFormValue(record); //???????????????????????????????????????????????????
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
    	final JFXTooltip btnZoomInTooltip = new JFXTooltip("?????? (Ctrl+=)");
    	final JFXTooltip btnZoomOutTooltip = new JFXTooltip("?????? (Ctrl+-)");
    	final JFXTooltip btnRotateRightTooltip = new JFXTooltip("????????????90??? (Ctrl+0)");
    	final JFXTooltip btnRotateLeftTooltip = new JFXTooltip("????????????90??? (Ctrl+9)");
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

        // ??????
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

        // ??????
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

        // ?????????EMAIL
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

        // ???????????????
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

        // ?????????????????????
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

		// ?????????????????????
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
        // ????????? JavaFX ?????????????????????????????????
        //btnUpdate.disableProperty().bind(Bindings.size(imageTableView.getItems()).lessThan(1));

        btnRemove.setOnAction(action -> {
        	onAction_btnRemove();
        });
        //btnRemove.disableProperty().bind(Bindings.size(imageTableView.getSelectionModel().getSelectedItems()).lessThan(1));

        btnRemovePartial.setOnAction(action -> {
        	onAction_btnRemovePartial();
        });
        // ????????? JavaFX ?????????????????????????????????
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

    	lbDeptId.setText("????????????\n?????????");
    	lbCompanyCode.setText("?????????\n(??????????????????)");
    	lbSendEmail.setText("?????????EMAIL\n(?????????????????????)");

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
		            	// ??????/???????????? (???????????????????????????????????????"NA")
		    	        return change;
		            } else {
		            	// ????????????
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
			    	int maxByteLength = 102; // IR-463380???????????????34????????????
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
    			String loadErrorMessage = String.format("%s ?????????????????????????????????????????????????????????????????? %s ?????????????????????", ImageRecordHelper.RECORD_SET_FILE_NAME, backupFileName);
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
			errorMessages.add(String.format("????????????????????? %s ???", ImageRecordHelper.RECORD_SET_FILE_NAME));
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
	    	btnLoginTooltip.textProperty().setValue("?????? (Ctrl+L)");
    	} else if (this.loginStatus == LoginStatus.STATUS_LOGGED_IN) {
			String username = PropertiesCache.getInstance().getProperty(PropertiesCache.PROP_KEY.EBAO_USER_NAME.propName());
			loginStatusLabelText.set(Constant.TXT_LOGIN_STATUS_WELCOME + " " + username);
	    	loginStatusLabelCss.set(Constant.LOGIN_CSS_NORMAL);
	    	btnLoginTooltip.textProperty().setValue("?????? (Ctrl+L)");
		} else if (this.loginStatus == LoginStatus.STATUS_OFF_LINE) {
			String username = PropertiesCache.getInstance().getProperty(PropertiesCache.PROP_KEY.EBAO_USER_NAME.propName());
			loginStatusLabelText.set(username + ":" + Constant.TXT_LOGIN_STATUS_OFF_LINE);
	    	loginStatusLabelCss.set(Constant.LOGIN_CSS_WARNING);
	    	btnLoginTooltip.textProperty().setValue("?????? (Ctrl+L)");
		} else {
			loginStatusLabelText.set(Constant.TXT_LOGIN_STATUS_NOT_LOG_IN);
	    	loginStatusLabelCss.set(Constant.LOGIN_CSS_WARNING);
	    	btnLoginTooltip.textProperty().setValue("?????? (Ctrl+L)");
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

		// ???????????????????????????
    	String checkStr = checkImageFilesNotExist();
    	if (checkStr!=null) {
			String msg = String.format("?????????(???%s???)?????????????????????????????????????????????????????????????????????????????????APP????????????????????????????????????????????????????????????????????????????????????", checkStr);
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

    	// ?????? scanconfig.xml ?????????

		// ????????????
		txtOrgName.setText(scanConfig.getOrgName());
		// ????????????
		txtDeptName.setText(scanConfig.getDeptName());

		// ???????????????
		cbbMainFileType.getItems().clear();
		cbbMainFileType.getItems().add(new Pair<String, String>("", ""));
		// ???????????????
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

        // ??????
        cbbBoxNumber.getItems().clear();
        cbbBoxNumber.getItems().add("");
        cbbBoxNumber.getItems().addAll(scanConfig.getBoxNos());
        String defBoxNo = scanConfig.getDefBoxNo();
        if (launchParamQueryFromPage) {
            if (!scanConfig.getBoxNos().contains(defBoxNo)) {
                cbbBoxNumber.getItems().add(defBoxNo);
            }
        }
        // ????????????-?????????
        cbbBatchDeptType.getItems().clear();
        cbbBatchDeptType.getItems().add(new Pair<String, String>("", ""));
        cbbBatchDeptType.getItems().addAll(scanConfig.getBatchDepTypeList());
        // ??????
        
        // ??????
        if (cbbActionReplace.getItems().size()==0) {
            cbbActionReplace.getItems().add(new Pair<String, String>("", ""));
            cbbActionReplace.getItems().add(new Pair<String, String>(Constant.YN_YES, Constant.TXT_YN_YES));
            cbbActionReplace.getItems().add(new Pair<String, String>(Constant.YN_NO, Constant.TXT_YN_NO));
        }
        String actionReplaceStr = scanConfig.getActionReplace();
        Pair<String, String> actionReplace = cbbActionReplace.getConverter().fromString(actionReplaceStr);
        cbbActionReplace.getSelectionModel().select(actionReplace);
        // ??????
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
        // ?????????EMAIL
        if (cbbSendEmail.getItems().size()==0) {
            cbbSendEmail.getItems().add(new Pair<String, String>("", ""));
            cbbSendEmail.getItems().add(new Pair<String, String>(Constant.YN_YES, Constant.TXT_YN_YES));
            cbbSendEmail.getItems().add(new Pair<String, String>(Constant.YN_NO, Constant.TXT_YN_NO));
        } else {
        	cbbSendEmail.getSelectionModel().clearSelection();
        }
        // ???????????????
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

		// ????????????-???????????????UNB(?????????)???????????????????????????
        if (cbbDuplexMode.getItems().size()==0) {
            cbbDuplexMode.getItems().add(new Pair<String, String>("", ""));
            cbbDuplexMode.getItems().add(new Pair<String, String>(Constant.DUPLEX_MODE_SINGLE_PAGE, Constant.TXT_DUPLEX_MODE_SINGLE_PAGE));
            cbbDuplexMode.getItems().add(new Pair<String, String>(Constant.DUPLEX_MODE_DOUBLE_PAGE, Constant.TXT_DUPLEX_MODE_DOUBLE_PAGE));
        }
        cbbDuplexMode.getSelectionModel().select( ScanUtil.getScanDuplex() == TwainConstants.TWDX_NONE ? 1 : 2 );
        
        // ????????????-????????????????????????
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
        	// ??????/????????????

    		if (logger.isDebugEnabled()) {
    			logger.debug("Replace/Insert Mode! launchParamBoxNo={}, launchParamBatchDeptType={}, launchParamBatchDate={}, launchParamBatchArea={}, launchParamBatchDocType={}", launchParamBoxNo, launchParamBatchDeptType, launchParamBatchDate, launchParamBatchArea, launchParamBatchDocType);
    			logger.debug("DefBoxNo={}, DefBatchDepType={}, DefBatchDate={}, DefBatchArea={}, DefBatchDocType={}", scanConfig.getDefBoxNo(), scanConfig.getDefBatchDepType(), scanConfig.getDefBatchDate(), scanConfig.getDefBatchArea(), scanConfig.getDefBatchDocType());
    		}

        	// ??????
        	cbbBoxNumber.getSelectionModel().select(cbbBoxNumber.getConverter().fromString(defBoxNo));
        	cbbBoxNumber.setDisable(true);
        	// ????????????-?????????
            String defBatchDepType = scanConfig.getDefBatchDepType();
            cbbBatchDeptType.getSelectionModel().select(cbbBatchDeptType.getConverter().fromString(defBatchDepType));
        	if (cbbBatchDeptType.getSelectionModel().getSelectedIndex()<0) {
        		cbbBatchDeptType.getItems().add(new Pair<String, String>(defBatchDepType, defBatchDepType));
                cbbBatchDeptType.getSelectionModel().select(cbbBatchDeptType.getConverter().fromString(defBatchDepType));
        	}
        	cbbBatchDeptType.setDisable(true);
        	// ????????????-??????
        	txtBatchDate.setText(scanConfig.getDefBatchDate());
        	txtBatchDate.setDisable(true);
        	// ??????
        	txtBatchArea.setText(scanConfig.getDefBatchArea());
        	txtBatchArea.setDisable(true);
        	// ?????????
        	txtBatchDocType.setText(scanConfig.getDefBatchDocType());
        	txtBatchDocType.setDisable(true);
        } else {
        	// ????????????

        	// ??????
        	cbbBoxNumber.setDisable(false);
        	// ????????????-?????????
            cbbBatchDeptType.getSelectionModel().select(cbbBatchDeptType.getConverter().fromString(scanConfig.getBatchDepType()));
        	cbbBatchDeptType.setDisable(false);
        	// ??????
            txtBatchDate.setText(scanConfig.getBatchDate());
        	txtBatchDate.setDisable(false);
        	// ??????
        	txtBatchArea.setText(scanConfig.getBatchArea());
        	txtBatchArea.setDisable(false);
        	// ?????????
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

	    // ??????????????????
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
	    	// UAT-IR-478019 ?????????????????????????????????????????????????????????????????????????????????
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

	    //????????????tiff????????????
	    String fileUrl = record.fileURLProperty().get();
		Image image = null;
		try {
			image = ImageUtil.createFxImage(fileUrl);
		} catch (IOException e) {
			// ???????????????????????????????????????
			//DialogUtil.showErrorMessage(MainView.this.getScene().getWindow(), e.getMessage());
			// ???????????????????????????,??????????????????
			//this.showSnackbar(e.getMessage(), true, Duration.seconds(2.0));
			logger.error(e);

		}

		if (null == image) {
		    // ??????????????????
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

		indexList.add(Integer.MAX_VALUE); // ?????????1????????????
		List<String> indexStrlist = new ArrayList<String>();

		int start = -1, end = -1, idxNo = -1;
		for (int i=0; i<indexList.size(); i++) {
			idxNo = indexList.get(i);
			if (i==0) { // ???1???
				start = idxNo;
				end = idxNo;
				continue;
			} else if (idxNo==(end+1)) { // ?????????
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

		return String.join("???", indexStrlist.toArray(String[]::new));
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

		// Sort ?????????????????????
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

			if ("0".equals(maxPage) || "Y".equals(signature)) { // ????????????=0 || ???????????????,?????????
				lastMaxPage = maxPage;
				if (logger.isDebugEnabled()) {
					logger.debug("maxPage=0 or is signature, Ignore pageNo check!");
				}
				continue;
			} else if ("1".equals(maxPage) && "1".equals(filePage)) { // ????????????=1,??????=1,?????????
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
					logger.debug("???????????????????????????,???FileType/FileCode, AddRecord");
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
					logger.debug("????????????,???FileType,???FileCode(10), AddRecord");
				}
			} else {
				// FileCode/FileType changed  
				needToCheck = true;
				if (logger.isDebugEnabled()) {
					logger.debug("FileType||FileCode??????");
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
							errorRemark = "??????";
						} else if ("Y".equals(errorActionReplace)) {
							errorRemark = "??????";
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

				if ("1".equals(maxPage) && "1".equals(filePage)) { // ????????????=1,??????=1,?????????
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
						errorRemark = "??????";
					} else if ("Y".equals(errorActionReplace)) {
						errorRemark = "??????";
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
			if (null!=result) { // ????????????
				if (Integer.valueOf("-1").equals(result)) {
					// ????????????
					stillUpload =  true;
				} else {
					// ????????????: ???????????????
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
				// ????????????: ??????????????????
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
					errorMessage = String.format("?????? %s ?????????EMAIL ??????????????????", String.join("???", scanOrderMap.keySet()));
				}
			} else {
				isValid = false;
				errorMessage = "?????????EMAIL ????????????";
			}
		} else {
			ScannedImage selectedItem = imageTableView.getSelectionModel().getSelectedItem();
			sendEmail = selectedItem.sendEmailProperty().getValue();
			if (logger.isDebugEnabled()) {
				logger.debug("sendEmail={}", sendEmail);
			}
			if (ObjectsUtil.isEmpty(sendEmail)) {
				isValid = false;
				errorMessage = "?????????EMAIL ????????????";
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
						errorMessageMap.put(fileCode, String.format("?????? %s ??????????????? ??????????????????", String.join("???", scanOrderMap.keySet())));
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
				errorMessage += (i==0 ? "" : "\r\n") + (errorCount==1 ? "" : ("?????? " + fileCode + "???")) + errorMessageMap.get(fileCode);
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
			// ????????????????????????/?????????,?????????
		}

		if (isInsertOrReplace) {
			DialogUtil.showErrorMessagesAndWait(MainView.this.getScene().getWindow(), false, "????????????????????????????????????????????????????????????????????????????????????????????????");
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

		imageItem.orgCodeProperty().set(orgCode); //????????????
		imageItem.orgNameProperty().set(orgName); //????????????
		imageItem.deptIdProperty().set(deptId); //?????????????????????ID
		imageItem.deptNameProperty().set(deptName); //????????????
		imageItem.batchDepTypeValueProperty().set(batchDepTypeValue);//????????????-???????????????
		imageItem.fromQueryPageProperty().set(fromQueryPage); //????????????????????????????????????
		imageItem.bizDeptProperty().set(bizDept); //????????????
		imageItem.isGIDProperty().set(isGID); //???????????????
		imageItem.rocDateProperty().set(rocDate); //????????????
		imageItem.updateRoleProperty().set(updateRole); //????????????????????????????????????
		imageItem.empIdProperty().set(empId); //????????????
		imageItem.stepProperty().set(step);

		if (launchParamQueryFromPage) {
			imageItem.batchDepTypeProperty().set(defBatchDepType);//????????????-????????????
			imageItem.batchDateProperty().set(defBatchDate); //????????????
		} else {
			imageItem.batchDepTypeProperty().set(batchDepType);//????????????-????????????
			imageItem.batchDateProperty().set(batchDate); //????????????
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

			// UAT-IR-479059???????????????????????????????????????
			/*
			Path file = Paths.get(fileURL);
			try {
				Files.delete(file);
			} catch (Exception e) {
				errorMessage = String.format("????????????????????????????????? %s ???", fileURL);
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

		// ??????????????????
		recordSetHelper.resetScanOrder(recordList);
		try {
			// ?????? XML ??????
			recordSetHelper.marshalToFile(recordSet);
		} catch (JAXBException e) {
			errorMessage = String.format("????????????????????? %s ???", ImageRecordHelper.RECORD_SET_FILE_NAME);
			logger.error(errorMessage, e);
		}
		// ???????????? XML ??????
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
//        final JFXTooltip btnUploadTooltip = new JFXTooltip("?????? (Ctrl+U)\n???????????? (Ctrl+Y)\n???????????? (Ctrl+D)");
//        JFXTooltip.install(btnUploadBadge, btnUploadTooltip, Pos.BOTTOM_CENTER);
	}

	public void registerLoginButton(JFXButton button) {
		this.btnLogin = button;
		this.btnLogin.setOnAction(action -> {
    		onAction_btnLogin();
		});
        btnLoginTooltip = new JFXTooltip("?????? (Ctrl+L)");
        JFXTooltip.install(btnLogin, btnLoginTooltip, Pos.BOTTOM_CENTER);
	}

	public void registerRunningModeButton(JFXButton button) {
		this.btnRunningMode = button;
		this.btnRunningMode.setOnAction(action -> {
    		onAction_btnRunningMode();
		});
		final JFXTooltip btnRunningModeTooltip = new JFXTooltip("???????????? (Ctrl+O)");
        JFXTooltip.install(btnRunningMode, btnRunningModeTooltip, Pos.BOTTOM_CENTER);
	}

	public void registerSettingsButton(JFXButton button) {
		this.btnSettings = button;
		this.btnSettings.setOnAction(action -> {
    		onAction_btnSettings();
		});
        final JFXTooltip btnSettingsTooltip = new JFXTooltip("?????? (Ctrl+P)");
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
	    		new JFXSnackbarLayout(message, "??????", action -> snackbar.close() ), 
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

		processText.setText(message != null ? message : "?????????...");
		processPane.setVisible(true);
	}

	private void hideProcessingPane() {
	    Stage stage = (Stage)this.getScene().getWindow();
//		stage.setOpacity(1.0f);

		processPane.setVisible(false);
	}

}
