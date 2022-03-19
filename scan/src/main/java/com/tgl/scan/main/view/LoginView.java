package com.tgl.scan.main.view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.tgl.scan.main.bean.LoginStatus;
import com.tgl.scan.main.bean.ScanConfig;
import com.tgl.scan.main.bean.Server;
import com.tgl.scan.main.bean.Servers;
import com.tgl.scan.main.http.EBaoException;
import com.tgl.scan.main.http.EbaoClient;
import com.tgl.scan.main.service.LoginService;
import com.tgl.scan.main.util.DialogUtil;
import com.tgl.scan.main.util.ObjectsUtil;
import com.tgl.scan.main.util.PropertiesCache;
import com.tgl.scan.main.util.ServersRecordHelper;
import com.tgl.scan.starter.AbstractFxView;

import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import javafx.util.StringConverter;

public class LoginView extends AbstractFxView {

	private static final Logger logger = LogManager.getLogger(LoginView.class);

	@FXML
	private VBox formPane;
	@FXML
	private JFXComboBox<Pair<String, String>> cmbxHost;
    @FXML
	private JFXTextField txtUserName;
    @FXML
	private JFXPasswordField txtPassword;
    @FXML
	private JFXButton btnLogin;
    @FXML
	private JFXButton btnCancel;
    @FXML
    private StackPane processPane;

    private MainView parent;
    private boolean showLogout = false; 
    private String webUserName = null;
    private String serverName = null;
    private String requestToken = null;
    private String processCode = null;
	private ScanConfig scanConfig = null;
	private boolean loggingIn = false;

	public LoginView(MainView parent, int currentLoginStatus, String userName) {
		super();
		this.parent = parent;
		this.showLogout = currentLoginStatus == LoginStatus.STATUS_LOGGED_IN;
		this.webUserName = userName;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cmbxHost.setConverter(new StringConverter<Pair<String,String>>() {
			@Override
			public String toString(Pair<String, String> object) {
				return object==null || ObjectsUtil.isEmpty(object.getKey()) ? "" : String.format("%s - (%s)", object.getKey(), object.getValue());

			}
			@Override
			public Pair<String, String> fromString(String string) {
				return cmbxHost.getItems().stream().filter(ap -> ap.getKey().equals(string)).findFirst().orElse(null);
			}
        });
		Servers eBaoServers = ServersRecordHelper.getInstance().unmarshalFromFile();
		for (Server eBaoServer : eBaoServers.getServerList()) {
			cmbxHost.getItems().add(new Pair<String, String>(eBaoServer.getName(), eBaoServer.getUrl()));
		}
		String testHostStr = PropertiesCache.getInstance().getProperty("ebao.host.test"); // 開發測試用隱藏屬性
		if (ObjectsUtil.isNotEmpty(testHostStr)) {
			String[] tmp = testHostStr.split(",");
			if (null!=tmp && tmp.length==2) {
				cmbxHost.getItems().add(new Pair<String, String>(tmp[0], tmp[1]));
			}
		}

		btnLogin.setOnAction( action -> {
			onAction_btnLogin();
		});
		btnCancel.setOnAction( action -> {
			closeWindow();
		});
	}

	public void stageShown() {
		String host = PropertiesCache.getInstance().getProperty(PropertiesCache.PROP_KEY.EBAO_HOST.propName());
		String userName = PropertiesCache.getInstance().getProperty(PropertiesCache.PROP_KEY.EBAO_USER_NAME.propName());

		if (null!=host) {
			cmbxHost.getSelectionModel().select(cmbxHost.getConverter().fromString(host));
		}
		if (cmbxHost.getSelectionModel().isEmpty() && cmbxHost.getItems().size()==1) {
			cmbxHost.getSelectionModel().select(0);
		}
		txtUserName.setText(userName);

		if (webUserName != null) {
			txtUserName.setText(webUserName);
			txtUserName.setDisable(true);
		}

		if (showLogout) {
			formPane.setDisable(true);
			btnLogin.setText("登出");
		}

        getScene().getWindow().addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
        	/* 這種寫法會造成登入被呼叫2次, UAT沒有報錯但PROD出現錯誤
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
    			closeWindow();
            } else if (keyEvent.getCode() == KeyCode.ENTER) {
            	btnLogin.requestFocus();
    			onAction_btnLogin();
            }
        	 */
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
            	keyEvent.consume();
    			btnCancel.fire();
            } else if (keyEvent.getCode() == KeyCode.ENTER) {
            	keyEvent.consume();
            	btnLogin.fire();
            }
        });
	}

	private void onAction_btnLogin() {
		Platform.runLater(() -> {
			if (showLogout) {
				doLogout();
			} else {
				if ( cmbxHost.validate() & 
					 txtUserName.validate() & txtPassword.validate() ) {
					doLogin();
				}
			}
		});
	}

	// 與 UI Thread 區隔，以另一個 Thread 處理
	private void doLogin() {
		String hostName = cmbxHost.getSelectionModel().getSelectedItem().getKey();
		String hostUrl = cmbxHost.getSelectionModel().getSelectedItem().getValue();
		String userName = txtUserName.getText();
		String password = txtPassword.getText();
		serverName = null;
	    requestToken = null;
		scanConfig = null;
		loggingIn = true;

		if (logger.isDebugEnabled()) {
			logger.debug("Logging in... HostName: {}, HostUrl: {}, UserName: {}", hostName, hostUrl, userName);
		}

		processPane.setVisible(true);

		LoginService service = new LoginService();
		service.setHostName(hostName);
		service.setHostUrl(hostUrl);
		service.setUserName(userName);
		service.setPassword(password);
		service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
        		processPane.setVisible(false);
        		loggingIn = false;

        		LoginStatus status = (LoginStatus)t.getSource().getValue();
        		if (logger.isDebugEnabled()) {
        			logger.debug("Logged in! processCode: {}", status.getProcessCode());
        		}

        		processCode = status.getProcessCode();
        		serverName = status.getServerName();
        		requestToken = status.getToken();
        		scanConfig = status.getConfig();
            	storeProperties(hostName, userName);
				closeWindow();
            }
        });
		service.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
        		processPane.setVisible(false);
        		loggingIn = false;

        		EBaoException e = (EBaoException)t.getSource().getException();
            	String errorMessage = e.getMessage();
        		if (logger.isDebugEnabled()) {
        			logger.debug("Login failed! {}", errorMessage);
        		}

            	if (EBaoException.Code.INCORRECT_ID_PASSWORD.equals(e.getCode())) {
					processCode = LoginStatus.PROC_CODE_WRONG_ID_PWD;
				} else 
				if (EBaoException.Code.PERMISSION_DENY.equals(e.getCode())) {
					processCode = LoginStatus.PROC_CODE_PERMISSION_DENY;
					errorMessage = String.format(errorMessage+"是否重新輸入或是關閉影像掃描程式離開？", userName);
				} else 
				if (EBaoException.Code.LOGIN_PAGE_ERROR.equals(e.getCode()) || 
					EBaoException.Code.NO_SCAN_CONFIG_URL.equals(e.getCode()) || 
					EBaoException.Code.NO_SCAN_CONFIG.equals(e.getCode())) {
					processCode = LoginStatus.PROC_CODE_SYSTEM_ERROR;
				} else {
					processCode = LoginStatus.PROC_CODE_ERROR;
				}

				if (LoginStatus.PROC_CODE_PERMISSION_DENY.equals(processCode)) {
					String result = DialogUtil.showConfirm(parent.getScene().getWindow(), errorMessage, new String[] {"重新輸入", "關閉影像掃描程式"});
					if ("關閉影像掃描程式".equals(result)) {
						closeWindow();
					}
				} else {
					DialogUtil.showErrorMessageAndWait(parent.getScene().getWindow(), errorMessage);
					if (LoginStatus.PROC_CODE_SYSTEM_ERROR.equals(processCode)) {
						closeWindow();
					}
				}
            }
        });

        service.start();
	}

	private void doLogout() {
		EbaoClient.getInstance().close();
		processCode = LoginStatus.PROC_CODE_LOG_OUT;
		closeWindow();
	}

	private void storeProperties(String hostName, String userName) {
		PropertiesCache.getInstance().setProperty(PropertiesCache.PROP_KEY.EBAO_HOST.propName(), hostName);
		PropertiesCache.getInstance().setProperty(PropertiesCache.PROP_KEY.EBAO_USER_NAME.propName(), userName);
		try {
			PropertiesCache.getInstance().flush();
		} catch (IOException e) {
			logger.error("無法寫入組態設定檔 " + PropertiesCache.PROPS_FILE_NAME + "！");
		}
	}

	private void closeWindow() {
		Platform.runLater(() -> {
			Stage stage = (Stage) LoginView.this.getScene().getWindow();
			stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
		});
	}

	public LoginStatus getLoginStatus() {
		return new LoginStatus(
			this.processCode, 
			this.serverName,
			this.requestToken, 
			this.scanConfig
		);
	}

	public boolean isLoggingIn() {
		return loggingIn;
	}

}
