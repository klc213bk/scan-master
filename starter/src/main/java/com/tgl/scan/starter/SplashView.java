package com.tgl.scan.starter;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.update4j.Archive;
import org.update4j.Configuration;
import org.update4j.FileMetadata;
import org.update4j.UpdateContext;
import org.update4j.UpdateOptions;
import org.update4j.UpdateOptions.ArchiveUpdateOptions;
import org.update4j.UpdateResult;
import org.update4j.inject.InjectSource;
import org.update4j.inject.Injectable;
import org.update4j.inject.UnsatisfiedInjectionException;
import org.update4j.service.UpdateHandler;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SplashView extends AbstractFxView implements UpdateHandler, Injectable {

	// 啟動程序 StarterDelegate.main() --> StarterDelegate.init() --> StarterDelegate.start() --> initialize() --> SplashView()

	private static final System.Logger logger = System.getLogger(SplashView.class.getSimpleName());

    private static final String ATTR_SCAN_CONFIG_URL = "com.tgl.scan.config.url";
    private static final String REMOTE_CONFIG_URL = System.getProperty(ATTR_SCAN_CONFIG_URL);
    private static final String ATTR_CONFIG_CONN_TIMEOUT = "com.tgl.config.conn.timeout";
    private static final String FILENAME_CONFIG_XML = "updater-scanapp.xml";
    private static final String ATTR_CONFIG_UPDATE_STATUS = "com.tgl.config.updatestatus";
	private static final String ATTR_PARA_REQ_TOKEN = "com.tgl.scan.parameter.syskey_request_token";
	private static final String ATTR_PARA_USER_NAME = "com.tgl.scan.parameter.userName";
	private static final String ATTR_PARA_FROM_WEB = "com.tgl.scan.parameter.fromWeb";
	private static final String ATTR_PARA_FROM_QUERY_PAGE = "com.tgl.scan.parameter.fromQueryPage";
	private static final String ATTR_PARA_BOX_NO = "com.tgl.scan.parameter.boxNo";
	private static final String ATTR_PARA_BATCH_DEPT_TYPE = "com.tgl.scan.parameter.batchDeptType";
	private static final String ATTR_PARA_BATCH_DATE = "com.tgl.scan.parameter.batchDate";
	private static final String ATTR_PARA_BATCH_AREA = "com.tgl.scan.parameter.batchArea";
	private static final String ATTR_PARA_BATCH_DOC_TYPE = "com.tgl.scan.parameter.batchDocType";

    private static final String CONFIG_LIB_DIR = "config";

	private static final int UPDATE_STATUS_NA = 0;
	private static final int UPDATE_STATUS_CONFIG_LOADING = 1;
	private static final int UPDATE_STATUS_REMOTE_CONFIG_LOADED = 2;
	private static final int UPDATE_STATUS_LOCAL_CONFIG_LOADED = 3;
	private static final int UPDATE_STATUS_LOAD_CONFIG_FAILED = 4;
	private static final int UPDATE_STATUS_LOAD_TWO_CONFIG_FAILED = 5;
	private static final int UPDATE_STATUS_LOAD_TWO_LOCAL_CONFIG_ONLY = 6;
	private static final int UPDATE_STATUS_CHECKING = 7;
	private static final int UPDATE_STATUS_CHECK_UPDATE_FAILED = 8;
	private static final int UPDATE_STATUS_IS_UP_TO_UPDATE = 9;
	private static final int UPDATE_STATUS_NEED_TO_UPDATE = 10;
	private static final int UPDATE_STATUS_DOWNLOADING = 11;
	private static final int UPDATE_STATUS_DOWNLOADED = 12;
	private static final int UPDATE_STATUS_DOWNLOAD_FAILED = 13;

	private Configuration remoteConfig;
	private Configuration localConfig;
	private Configuration oldLocalConfig;

	private String[] args;

	@FXML
	private HBox paneProgressBar;
	@FXML
	private StackPane paneSubProgress;
	@FXML
	private Label lbProgressText;
	@FXML
	private HBox panelReloadOrExit;
	@FXML
	private HBox panelRecheckOrSkip;
	@FXML
	private HBox panelExit;
	@FXML
	private HBox panelSkipOrExit;
	@FXML
	private HBox panelDownloadOrSkip;
	@FXML
	private HBox panelRetryOrSkip;
	@FXML
	private HBox panelStopDownload;
	@FXML
	private StackPane progressContainer;
	@FXML
	private Pane primary;
	@FXML
	private Hyperlink hlErrorMsg;

	@FXML
	@InjectSource
	private TextField txtLaunchParameters;

	@InjectSource
	private List<Image> images = StarterDelegate.images;

	@InjectSource
	private TextField singleInstanceMessage;

	private int downloadCount;
	private int currentDownloadIndex;
	private String currentDownloadFileName;

    private IntegerProperty updateStatus;
    private StringProperty updateStatusText;
	private DoubleProperty primaryPercent;
	private BooleanProperty running;
	private volatile boolean abort;

	@InjectSource
	private Stage primaryStage;

	public SplashView(Stage primaryStage, String[] args) {
		super();
		logger.log(INFO, "args=" + (null==args ? "null" : args.toString()));

		this.primaryStage = primaryStage;
		this.args = args;
	}

	private String getPropertiesString(ObservableMap<Object, Object> map) {
		if ( null == map || map.size() == 0 ) return "";
		StringBuffer sb = new StringBuffer();
        sb.append("\n");
        for ( Map.Entry<Object, Object> entry : map.entrySet() ) {
        	sb.append("System property " + entry.getKey() + " : " + entry.getValue() + "\n");
        }
        return sb.toString();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		logger.log(INFO, getPropertiesString(getProperties()));

		String paraFromWeb = System.getProperty(ATTR_PARA_FROM_WEB, "false");
		String paraFromQueryPage = System.getProperty(ATTR_PARA_FROM_QUERY_PAGE, "false");
		String reqToken = System.getProperty(ATTR_PARA_REQ_TOKEN, "");
		String paraUserName = System.getProperty(ATTR_PARA_USER_NAME, "");
		String paraBoxNo = System.getProperty(ATTR_PARA_BOX_NO, "");
		String paraBatchDeptType = System.getProperty(ATTR_PARA_BATCH_DEPT_TYPE, "");
		String paraBatchDate = System.getProperty(ATTR_PARA_BATCH_DATE, "");
		String paraBatchArea = System.getProperty(ATTR_PARA_BATCH_AREA, "");
		String paraBatchDocType = System.getProperty(ATTR_PARA_BATCH_DOC_TYPE, "");
		String parameters = paraFromWeb + "|" + paraFromQueryPage + "|" + reqToken + "|" + paraUserName + "|" + 
				paraBoxNo + "|" + paraBatchDeptType + "|" + paraBatchDate + "|" + paraBatchArea + "|" + paraBatchDocType;

		logger.log(INFO, "\nparameters=" + parameters + 
				"\nfromWeb=" + paraFromWeb + 
				"\nfromQueryPage=" + paraFromQueryPage + 
				"\requestToken=" + reqToken + 
				"\nuserName=" + paraUserName + 
				"\nboxNo=" + paraBoxNo + 
				"\nbatchDeptType=" + paraBatchDeptType + 
				"\nbatchDate=" + paraBatchDate + 
				"\nbatchArea=" + paraBatchArea + 
				"\nbatchDocType=" + paraBatchDocType);

		this.txtLaunchParameters.setText(parameters);

		updateStatusText = new SimpleStringProperty("");

		updateStatus = new SimpleIntegerProperty(UPDATE_STATUS_NA);
		updateStatus.addListener((observable, oldValue, newValue) -> {
			panelReloadOrExit.setVisible(false);
			panelRecheckOrSkip.setVisible(false);
			panelExit.setVisible(false);
			panelSkipOrExit.setVisible(false);
			panelDownloadOrSkip.setVisible(false);
			panelRetryOrSkip.setVisible(false);
			panelStopDownload.setVisible(false);
			progressContainer.setVisible(false);
			paneSubProgress.setMaxWidth(0);

			switch (newValue.intValue()) {
				case UPDATE_STATUS_CONFIG_LOADING:
					logger.log(INFO, newValue.intValue() + ", Start to load version config...");
					updateStatusText.set("載入版本資訊...");
					break;
				case UPDATE_STATUS_REMOTE_CONFIG_LOADED:
					logger.log(INFO, newValue.intValue() + ", Remote config loaded.");
					updateStatusText.set("載入遠端版本資訊。");
					checkUpdates();
					break;
				case UPDATE_STATUS_LOCAL_CONFIG_LOADED:
					logger.log(INFO, newValue.intValue() + ", Unable to load remote config, change to read local.");
					updateStatusText.set("無法取得遠端版本資訊，改讀取本機版本資訊。您可以");
					panelRecheckOrSkip.setVisible(true);
					paneSubProgress.setMaxWidth(panelRecheckOrSkip.maxWidthProperty().getValue());
					break;
				case UPDATE_STATUS_LOAD_CONFIG_FAILED:
					logger.log(INFO, newValue.intValue() + ", Failed to get app version config.");
					updateStatusText.set("無法取得版本資訊！請確認網路連線正常後");
					panelReloadOrExit.setVisible(true);
					paneSubProgress.setMaxWidth(panelReloadOrExit.maxWidthProperty().getValue());
					break;
				case UPDATE_STATUS_LOAD_TWO_CONFIG_FAILED:
					logger.log(INFO, newValue.intValue() + ", Failed to load remote config, and can not read local config too! Can not start ScanApp.");
					updateStatusText.set("無法取得遠端及本機版本資訊！請");
					panelExit.setVisible(true);
					paneSubProgress.setMaxWidth(panelExit.maxWidthProperty().getValue());
					break;
				case UPDATE_STATUS_LOAD_TWO_LOCAL_CONFIG_ONLY:
					logger.log(INFO, newValue.intValue() + ", Failed to load remote config, and can not read local config too! Skip or exit.");
					updateStatusText.set("無法取得遠端版本資訊，改讀取本機版本資訊。您可以暫時");
					panelSkipOrExit.setVisible(true);
					paneSubProgress.setMaxWidth(panelSkipOrExit.maxWidthProperty().getValue());
					break;
				case UPDATE_STATUS_CHECKING:
					logger.log(INFO, newValue.intValue() + ", Start to check versio...");
					updateStatusText.set("檢查是否有新版本...");
					break;
				case UPDATE_STATUS_CHECK_UPDATE_FAILED:
					logger.log(INFO, newValue.intValue() + ", Failed to check app version!");
					updateStatusText.set("版本檢查失敗！您可以");
					panelRecheckOrSkip.setVisible(true);
					paneSubProgress.setMaxWidth(panelRecheckOrSkip.maxWidthProperty().getValue());
					break;
				case UPDATE_STATUS_IS_UP_TO_UPDATE:
					logger.log(INFO, newValue.intValue() + ", Scan app is up to date, Start now...");
					updateStatusText.set("已為最新版本，程式啟動中...");
					launchScanApp(true);
					break;
				case UPDATE_STATUS_NEED_TO_UPDATE:
					logger.log(INFO, newValue.intValue() + ", Scan app has a new verion.");
					updateStatusText.set("有新版本。您可以立即");
					panelDownloadOrSkip.setVisible(true);
					paneSubProgress.setMaxWidth(panelDownloadOrSkip.maxWidthProperty().getValue());
					break;
				case UPDATE_STATUS_DOWNLOADING:
					logger.log(INFO, newValue.intValue() + ", Download.");
					updateStatusText.set("下載更新。");
					panelStopDownload.setVisible(true);
					paneSubProgress.setMaxWidth(panelStopDownload.maxWidthProperty().getValue());
					progressContainer.setVisible(true);
					doUpdate();
					break;
				case UPDATE_STATUS_DOWNLOADED:
					logger.log(INFO, newValue.intValue() + ", Downloaded! Start scan app...");
					updateStatusText.set("已更新為最新版本，程式啟動中...");
					launchScanApp(true);
					break;
				case UPDATE_STATUS_DOWNLOAD_FAILED:
					logger.log(INFO, newValue.intValue() + ", Failed to update Scan App!");
					updateStatusText.set("更新失敗！因為");
					panelRetryOrSkip.setVisible(true);
					paneSubProgress.setMaxWidth(panelRetryOrSkip.maxWidthProperty().getValue());
					break;
				default:
					logger.log(INFO, newValue.intValue());
					updateStatusText.set("");
					break;
			}

		});

		lbProgressText.textProperty().bind(updateStatusText);

		running = new SimpleBooleanProperty(false);
		running.addListener((obs, ov, nv) -> {
			if (!nv) {
				primaryPercent.set(0);
			}
		});

		primaryPercent = new SimpleDoubleProperty(0);
		primary.maxWidthProperty().bind(progressContainer.widthProperty().multiply(primaryPercent));
	}

	public void stageShown() {
		checkConfig();
	}

	private void checkConfig() {
		logger.log(INFO, "");
		updateStatus.set(UPDATE_STATUS_CONFIG_LOADING);

		Task<Integer> checkUpdateTask = new Task<>() {

			@Override
			protected Integer call() throws Exception {
				int result;

			    String updateStatus = System.getProperty(ATTR_CONFIG_UPDATE_STATUS);
				logger.log(INFO, "checkUpdateTask.call(), updateStatus={0}", updateStatus);

				if ("StarterNotUpdate".equals(updateStatus)) {
					// Starter 無法取得遠端版本資訊，僅載入本機版本資訊
					localConfig = getLocalConfig(CONFIG_LIB_DIR + "/" + FILENAME_CONFIG_XML);

					if (localConfig==null) {
	                	logger.log(INFO, "localConfig is null");

	                	// 不正常，此情境可以是 updater-scanapp.xml 被人手動刪除了
						result = UPDATE_STATUS_LOAD_TWO_CONFIG_FAILED; // 無法取得遠端及本機版本資訊！請[關閉程式]確認網路連線正常後再重新開啟程式。 Exit
					} else {
						List<FileMetadata> localConfigFiles = localConfig.getFiles();
	                	logger.log(INFO, "localConfigFiles.isEmpty()={0}", localConfigFiles.isEmpty());
	                	for (FileMetadata file : localConfigFiles) {
		                	logger.log(INFO, "localConfig file: {0}, isClasspath={1}, isModulepath={2}", file.getPath(), file.isClasspath(), file.isModulepath());
	                	}

	                	result = UPDATE_STATUS_LOAD_TWO_LOCAL_CONFIG_ONLY; // 無法取得遠端版本資訊，改讀取本機版本資訊。您可以暫時[略過]並啟動目前版本的程式，或是[關閉程式]。 SkipOrExit
					}
				} else {
					// Starter 載入遠端版本資訊

					String localConfigFileName = CONFIG_LIB_DIR + "/" + FILENAME_CONFIG_XML;
					if (Files.exists(Paths.get(localConfigFileName + ".old"))) {
						localConfigFileName += ".old";
					}
					localConfig = getLocalConfig(localConfigFileName);
					if (localConfig==null) {
	                	logger.log(INFO, "localConfig is null");
					} else {
						List<FileMetadata> localConfigFiles = localConfig.getFiles();
	                	logger.log(INFO, "localConfigFiles.isEmpty()={0}", localConfigFiles.isEmpty());
	                	for (FileMetadata file : localConfigFiles) {
		                	logger.log(INFO, "localConfig file: {0}, isClasspath={1}, isModulepath={2}", file.getPath(), file.isClasspath(), file.isModulepath());
	                	}
					}

					remoteConfig = getRemoteConfig(REMOTE_CONFIG_URL + "/" + FILENAME_CONFIG_XML);

					if (remoteConfig==null) {
	                	logger.log(INFO, "remoteConfig is null");
						if (localConfig==null) {
							// 此情境應該不會發生，因為 Bootstrap.jar 已經把 updater-scanapp.xml 下載到本機端，因此，當 Starter 有載入遠端版本資訊時，localConfig 應該會有值
							result = UPDATE_STATUS_LOAD_CONFIG_FAILED; // 無法取得版本資訊！請確認網路連線正常後 ReloadOrExit
						} else {
		                	result = UPDATE_STATUS_LOCAL_CONFIG_LOADED; // 無法取得遠端版本資訊，改讀取本機版本資訊。您可以 RecheckOrSkip
						}
					} else {
						List<FileMetadata> remoteConfigFiles = remoteConfig.getFiles();
	                	logger.log(INFO, "remoteConfigFiles.isEmpty()={0}", remoteConfigFiles.isEmpty());
	                	for (FileMetadata file : remoteConfigFiles) {
		                	logger.log(INFO, "remoteConfig file: {0}, isClasspath={1}, isModulepath={2}", file.getPath(), file.isClasspath(), file.isModulepath());
	                	}

	                	result = UPDATE_STATUS_REMOTE_CONFIG_LOADED; // 載入遠端版本資訊。checkUpdates
					}
				}

				return result;
			}

		    private Configuration getRemoteConfig(String remote) {
		    	logger.log(INFO, "remote=" + remote);
		        try (Reader in = openConnection(new URL(remote))) {
		        	return Configuration.read(in);
		        } catch (Exception e) {
		        	logger.log(ERROR, "Load remote config Error!", e);
		        }

		        return null;
		    }

		    private Reader openConnection(URL url) throws IOException {
		    	int timeout = 5;
		    	String timeoutStr = System.getProperty(ATTR_CONFIG_CONN_TIMEOUT);
		    	try {
	                timeout = Integer.parseInt(timeoutStr);
		    	} catch(Exception e) {
		    	}

		    	logger.log(INFO, "url=" + url.toString() + ", timeout=" + timeout);

		        URLConnection connection = url.openConnection();

		        // Some downloads may fail with HTTP/403, this may solve it
		        connection.addRequestProperty("User-Agent", "Mozilla/5.0");
		        connection.setConnectTimeout(timeout * 1000);
		        connection.setReadTimeout(timeout * 1000 * 2);

		        return new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
		    }

		    private Configuration getLocalConfig(String local) {
		    	logger.log(INFO, "local=" + local);
		        try (Reader in = Files.newBufferedReader(Paths.get(local))) {
		        	return Configuration.read(in);
		        } catch (NoSuchFileException e) {
	            	logger.log(ERROR, "Local config file not found!", e);
		        } catch (Exception e) {
		        	logger.log(ERROR, "Read local config Error!", e);
		        }

		        return null;
		    }

		};
		checkUpdateTask.setOnSucceeded(evt -> {
			int result = checkUpdateTask.getValue();
			updateStatus.set(result);
		});

		run(checkUpdateTask);
	}

	private void checkUpdates() {
		logger.log(INFO, "");
		updateStatus.set(UPDATE_STATUS_CHECKING);

		Task<Boolean> checkUpdateTask = new Task<>() {
			@Override
			protected Boolean call() throws Exception {
				return remoteConfig.requiresUpdate();
			}
		};
		checkUpdateTask.setOnSucceeded(evt -> {
			boolean result = checkUpdateTask.getValue();
			logger.log(INFO, "checkUpdateTask.getValue()="+result);
			if (result) {
				logger.log(INFO, "Has a new version!");
				updateStatus.set(UPDATE_STATUS_NEED_TO_UPDATE);
			} else {
				logger.log(INFO, "Is up to date, start now...");
				updateStatus.set(UPDATE_STATUS_IS_UP_TO_UPDATE);
			}
		});
		checkUpdateTask.setOnFailed(evt -> {
			logger.log(INFO, "Failed to update! Ignore update and start app...");
			updateStatus.set(UPDATE_STATUS_CHECK_UPDATE_FAILED);
		});

		run(checkUpdateTask);
	}

	private void launchScanApp(boolean isUpToDate) {
		Task<Process> launchTask = new Task<>() {
			@Override
			protected Process call() throws Exception {
				logger.log(INFO, "launchTask.call()");

				if (isUpToDate) {
					System.setProperty(ATTR_CONFIG_UPDATE_STATUS, "AppIsUpToDate");
					remoteConfig.launch(SplashView.this);
				} else {
					System.setProperty(ATTR_CONFIG_UPDATE_STATUS, "AppSkipUpdate");
					localConfig.launch(SplashView.this);
				}

				/*
				String userHome = System.getProperty("user.dir");
				String javaExeDir = System.getProperty("sun.boot.library.path");
				String exepath = javaExeDir + File.separator + "javaw ";
				String exeargs = "--module-path lib --add-exports javafx.base/com.sun.javafx.event=com.jfoenix --add-exports javafx.graphics/com.sun.javafx.scene=com.jfoenix --add-opens java.base/java.lang.reflect=com.jfoenix --show-module-resolution --illegal-access=warn -m scan/com.tgl.scan.main.AppLauncher";
				String command = exepath + exeargs;
				Runtime runtime = Runtime.getRuntime();
				logger.log(INFO, "command=" + command + ", userHome=" + userHome);
				Process process = runtime.exec(command, null , new File(userHome));
				*/

				return null;
			}
		};
		launchTask.setOnFailed(evt -> {
			logger.log(INFO, "launchTask.onFailed(), errorMessage="+evt.getSource().getException().getMessage());
			exitScanApp();
		});
		run(launchTask);
	}

	private void doUpdate() {
		running.set(true);

		Task<Integer> updateTask = new Task<>() {
			@Override
			protected Integer call() throws Exception {
				logger.log(INFO, "updateHandler --> updateTask.call()");

				Path zip = Paths.get("scan-update.zip");
			    ArchiveUpdateOptions options = UpdateOptions.archive(zip);
			    options.updateHandler(SplashView.this);
			    UpdateResult result = remoteConfig.update(options);
			    boolean success = result.getException() == null;
	            logger.log(INFO, "Update scan app, success=" + success);

			    int returnValue = UPDATE_STATUS_DOWNLOADED;
			    if (success) {
	            	logger.log(INFO, "Read zip.");
				    Archive.read(zip).install();
	                logger.log(INFO, "Installed.");

	                String localConfigFileName = CONFIG_LIB_DIR + "/" + FILENAME_CONFIG_XML;
	                syncLocalConfig(remoteConfig, localConfigFileName);
	                if (localConfig != null) {
	                	boolean isRequiresUpdate = remoteConfig.requiresUpdate();
	                	logger.log(INFO, "remoteConfig.requiresUpdate()={0}", isRequiresUpdate);

	                	List<FileMetadata> remoteConfigFiles = remoteConfig.getFiles();
	                	logger.log(INFO, "remoteConfigFiles.isEmpty()={0}", remoteConfigFiles.isEmpty());
	                	for (FileMetadata file : remoteConfigFiles) {
		                	logger.log(INFO, "remoteConfig file: {0}, isClasspath={1}, isModulepath={2}", file.getPath(), file.isClasspath(), file.isModulepath());
	                	}

	                	List<FileMetadata> oldFiles = remoteConfig.getOldFiles(localConfig, true);
	                	logger.log(INFO, "remoteConfig.getOldFiles(localConfig, true).isEmpty()={0}", oldFiles.isEmpty());
	                	for (FileMetadata file : oldFiles) {
		                	logger.log(INFO, "Old file: {0}, isClasspath={1}, isModulepath={2}", file.getPath(), file.isClasspath(), file.isModulepath());
	                	}

	                	List<FileMetadata> localConfigFiles = localConfig.getFiles();
	                	logger.log(INFO, "localConfigFiles.isEmpty()={0}", localConfigFiles.isEmpty());
	                	for (FileMetadata file : localConfigFiles) {
		                	logger.log(INFO, "localConfig file: {0}, isClasspath={1}, isModulepath={2}", file.getPath(), file.isClasspath(), file.isModulepath());
	                	}

	                	logger.log(INFO, "Delete old files.");
	                	remoteConfig.deleteOldFiles(localConfig);

	            		try {
		                	logger.log(INFO, "Delete local backup config.");
	            			Files.deleteIfExists(Paths.get(localConfigFileName + ".old"));
	            		} catch (IOException e) {
	                    	logger.log(ERROR, "Delete local backup config Error!\n{0}", e.getMessage());
	            		}
	                }
			    } else {
				    Throwable t = result.getException();
					String errorMessage = t.getMessage();
					if (t instanceof AbortException) {
						errorMessage = "使用者強制停止下載！";
					}
					logger.log(INFO, "Update failed: "+errorMessage);
					hlErrorMsg.setTooltip(new Tooltip(errorMessage));
				    returnValue = UPDATE_STATUS_DOWNLOAD_FAILED;
				}
				return returnValue;
			}

		    private void syncLocalConfig(Configuration remoteConfig, String local) {
		    	logger.log(INFO, "Write remoteConfig to " + local);
		        Path localPath = Paths.get(local);
		        try {
		            if (localPath.getParent() != null)
		                Files.createDirectories(localPath.getParent());

		            try (Writer out = Files.newBufferedWriter(localPath)) {
		                remoteConfig.write(out);
		            }
		        } catch (IOException e) {
		        	logger.log(ERROR, "Sync local config Error!", e);
		        }
		    }
		};
		updateTask.setOnSucceeded(evt -> {
			int result = updateTask.getValue();
			updateStatus.set(result);
		});
		updateTask.setOnFailed(evt -> {
			logger.log(INFO, "updateTask.onFailed(), message:"+evt.getSource().getException().getMessage());
			updateStatus.set(UPDATE_STATUS_DOWNLOAD_FAILED);
		});
		run(updateTask);
	}

	private void run(Runnable runnable) {
		Thread runner = new Thread(runnable);
		runner.setDaemon(true);
		runner.start();
	}

    public static void injectBidirectional(Injectable obj1, Injectable obj2) throws UnsatisfiedInjectionException, IllegalAccessException, InvocationTargetException {
    	injectBidirectional(obj1, obj2);
	}
	
	public static void injectUnidirectional(Injectable source, Injectable target) throws IllegalAccessException, UnsatisfiedInjectionException, InvocationTargetException {
		injectUnidirectional(source, target);
	}

	private void exitScanApp() {
		this.primaryStage.hide();
	}

	@FXML
	void onAction_btnReload(ActionEvent event) {
		checkConfig();
	}

	@FXML
	void onAction_btnExit(ActionEvent event) {
		exitScanApp();
	}

	@FXML
	void onAction_btnRecheck(ActionEvent event) {
		checkUpdates();
	}

	@FXML
	void onAction_btnRetry(ActionEvent event) {
		updateStatus.set(UPDATE_STATUS_DOWNLOADING);
	}

	@FXML
	void onAction_btnStop(ActionEvent event) {
		abort = true;
	}

	@FXML
	void onAction_btnSkip(ActionEvent event) {
        launchScanApp(false);
	}

	@FXML
	void onAction_btnUpdate(ActionEvent event) {
		updateStatus.set(UPDATE_STATUS_DOWNLOADING);
	}

	/*
	 * UpdateHandler methods
	 */

	@Override
	public void init(UpdateContext context) throws Throwable {
		logger.log(INFO, "updateHandler.init()");
		Platform.runLater(() -> updateStatusText.set("準備中..."));
    }

	@Override
	public void startCheckUpdates() throws Throwable {
		logger.log(INFO, "updateHandler.startCheckUpdates()");
		Platform.runLater(() -> updateStatusText.set("檢查需下載的檔案..."));
		downloadCount = 0;
    }

	@Override
	public void startCheckUpdateFile(FileMetadata file) throws Throwable {
		logger.log(INFO, "updateHandler.startCheckUpdateFile(), file="+file.getPath().getFileName());
	}

	@Override
	public void doneCheckUpdateFile(FileMetadata file, boolean requires) throws Throwable {
		logger.log(INFO, "updateHandler.doneCheckUpdateFile(), file="+file.getPath().getFileName() + ", requires="+requires);
		if (requires) {
			downloadCount++;
		}
    }

	@Override
	public void doneCheckUpdates() throws Throwable {
		logger.log(INFO, "updateHandler.doneCheckUpdates(), " + downloadCount + " files need to update!");
		Platform.runLater(() -> updateStatusText.set("共計需下載 " + downloadCount + " 個檔案."));
    }

	@Override
	public void startDownloads() throws Throwable {
		logger.log(INFO, "updateHandler.startDownloads()");
		currentDownloadIndex = 0;
		currentDownloadFileName = "";
    }
	
	@Override
	public void startDownloadFile(FileMetadata file) throws Throwable {
		currentDownloadFileName = file.getPath().getFileName().toString();
		currentDownloadIndex++;
		String uri = file.getUri().toString();
		logger.log(INFO, "updateHandler.startDownloadFile(), file="+currentDownloadFileName+", uri="+uri+"  "+currentDownloadIndex+"/"+downloadCount);
		Platform.runLater(() -> updateStatusText.set(String.format("檔案%s-%s，%s 下載中...", downloadCount, currentDownloadIndex, currentDownloadFileName)));
    }
	
	@Override
	public void doneDownloadFile(FileMetadata file, Path path) throws Throwable {
		logger.log(INFO, "updateHandler.doneDownloadFile(), file="+file.getPath().getFileName());
    }
	
	@Override
	public void doneDownloads() throws Throwable {
		logger.log(INFO, "updateHandler.doneDownloads()");
    }

	@Override
	public void updateDownloadFileProgress(FileMetadata file, float frac) throws AbortException {
		if (abort) {
			throw new AbortException();
		}
	}

	@Override
	public void updateDownloadProgress(float frac) throws InterruptedException {
		Platform.runLater(() -> primaryPercent.set(frac));
//		Thread.sleep(20);
	}

	@Override
	public void failed(Throwable t) {
		logger.log(INFO, "updateHandler.failed(), Failed:"+t.getMessage());
//		Platform.runLater(() -> {
//			if (t instanceof AbortException)
//				updateStatusText.set("使用者強制停止下載！");
//			else
//				updateStatusText.set("下載失敗: " + t.getClass().getSimpleName() + ": " + t.getMessage());
//		});
	}

	@Override
	public void succeeded() {
		logger.log(INFO, "updateHandler.succeeded()");
		Platform.runLater(() -> updateStatusText.set("下載完成！"));
	}

	@Override
	public void stop() {
		logger.log(INFO, "updateHandler.stop()");
		Platform.runLater(() -> running.set(false));
		abort = false;
	}

}
