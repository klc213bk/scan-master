package com.tgl.scan.main;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.update4j.FileMetadata;
import org.update4j.LaunchContext;
import org.update4j.Property;
import org.update4j.SingleInstanceManager;
import org.update4j.inject.InjectTarget;
import org.update4j.inject.PostInject;
import org.update4j.service.Launcher;

import com.asprise.imaging.core.Imaging;
import com.jfoenix.controls.JFXDecorator;
import com.tgl.scan.main.http.EbaoClient;
import com.tgl.scan.main.util.DialogUtil;
import com.tgl.scan.main.util.PropertiesCache;
import com.tgl.scan.main.view.LoadingView;
import com.tgl.scan.main.view.MainView;
import com.tgl.scan.main.view.ToolbarPane;
import com.tgl.scan.starter.SplashView;
import com.tgl.scan.starter.StarterConst;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class AppLauncher extends Application implements Launcher {

	private static final Logger logger = LogManager.getLogger(AppLauncher.class);

	private static final Path LOCK_DIR = Paths.get(System.getProperty("user.home"), "."+StarterConst.APP_SCHEME_ID);

	@InjectTarget
	private TextField txtLaunchParameters;

	@InjectTarget
	private Stage primaryStage;

	@InjectTarget
	private List<Image> images;

	private SplashView splash;
	private LoadingView loading;
	private static Stage stage;
	private static MainView view;

	@PostInject
	private void getSplashView(SplashView view) {
		splash = view;
	}

	@Override
	public long version() {
		return 0;
	}

	// For Develop use only!
	public static void main(String[] args) {
    	if (logger.isDebugEnabled()) {
    		logger.debug("Run dev mode!");
    		logger.debug("args={}", (null==args ? "null" : Arrays.toString(args)));
    	}
        launch(args);
    }

	// For Develop use only!
	@Override
	public void start(Stage primaryStage) throws Exception {
    	if (logger.isDebugEnabled()) {
    		logger.debug("");
    	}
		AppLauncher.this.primaryStage = primaryStage;
//		initDevStage();
		initStage(true);
	}

	private String getContextString(LaunchContext context) {
		if ( null == context ) return "";
		StringBuffer sb = new StringBuffer();

        String launcher = context.getConfiguration().getLauncher();
        sb.append("\n");
        sb.append("Configuration launcher=" + launcher + "\n");

		List<Property> properties = context.getConfiguration().getProperties();
        for ( Property property : properties ) {
        	sb.append("Configuration property " + property.getKey() + " : " + property.getValue() + "\n");
        }
        
        List<FileMetadata> files = context.getConfiguration().getFiles();
        for ( FileMetadata file : files ) {
        	sb.append("Configuration file " + file.getPath().toString() + "\n");
        }

        Set<Module> modules = context.getModuleLayer().modules();
        for ( Module module : modules ) {
        	sb.append("ModuleLayer module " + module.toString() + "\n");
        }

        Package[] packages = context.getClassLoader().getDefinedPackages();
        for ( Package pkg : packages ) {
        	sb.append("ClassLoader " + pkg.toString() + "\n");
        }

		Map<String, String> propertiesMap = new TreeMap(System.getProperties());
		for (String key : propertiesMap.keySet()) {
        	sb.append("System property " + key + "=" + propertiesMap.get(key) + "\n");
		}

		Map<String, String> environmentMap = new TreeMap(System.getenv());
		for (String key : environmentMap.keySet()) {
        	sb.append("System environment " + key + "=" + environmentMap.get(key) + "\n");
		}

        return sb.toString();
	}

	@Override
	public void run(LaunchContext context) {
		if (logger.isDebugEnabled()) {
    		logger.debug("context={}", getContextString(context));
    	}

    	// Single instance
		try {
	    	if (logger.isDebugEnabled()) {
	    		logger.debug("Create lock directory! {}", LOCK_DIR.toString());
	    	}
			Files.createDirectories(LOCK_DIR);
		} catch (IOException e) {
			logger.error("無法建立暫存資料夾 " + LOCK_DIR, e);
		}

		SingleInstanceManager.execute(
			List.of(txtLaunchParameters.getText()), 
			args -> {
				Platform.runLater(() -> {
					view.parseLaunchParameters("RunningMode|true|" + args.get(0)); // App 第二次被啟動
				});
			}, 
			LOCK_DIR
		);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	    	if (logger.isDebugEnabled()) {
	    		logger.debug("Add shutdownHook()");
	    	}

	    	EbaoClient ebaoClient = EbaoClient.getInstance();
			ebaoClient.close();
	    	if (logger.isDebugEnabled()) {
	    		logger.debug("eBao Connection closed!");
	    	}

	    	try {
				Thread.sleep(5000);
				if (isEmptyDirectory(LOCK_DIR)) {
					Files.deleteIfExists(LOCK_DIR);
			    	if (logger.isDebugEnabled()) {
			    		logger.debug("Lock directory deleted!");
			    	}
				}
			} catch (Exception e) {
				logger.error("Failed to delete  " + LOCK_DIR, e);
			}
		}));

		Platform.runLater(() -> {
	    	if (logger.isDebugEnabled()) {
	    		logger.debug("Show loading...");
	    	}
			stage = new Stage();
			loading = new LoadingView("Rendering Nodes");
			splash.getChildren().add(loading);
			loading.show();
		});

		initStage(false);
	}

	private void initStage(boolean devMode) {
    	if (logger.isDebugEnabled()) {
    		logger.debug("");
    	}

    	ToolbarPane toolbarPane = new ToolbarPane();
		view = new MainView();
        view.registerUploadBadgeButton(toolbarPane.getUploadBadgeButton());
        view.registerLoginButton(toolbarPane.getLoginButton());
        view.registerRunningModeButton(toolbarPane.getRunningModeButton());
        view.registerSettingsButton(toolbarPane.getSettingsButton());
        view.registerLoginStatusLabel(toolbarPane.getLoginStatusLabel());
        view.registerUploadBadge(toolbarPane.getUploadBadge());
        view.registerServerLabel(toolbarPane.getServerLabel());

        JFXDecorator decorator = null;
		if (devMode) {
			decorator = new JFXDecorator(primaryStage, view, false, true, true);
			decorator.setGraphic(toolbarPane);
			Scene scene = new Scene(decorator);
			scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
				public void handle(final KeyEvent keyEvent) {
					view.setOnKeyPressed(keyEvent);
				}
			} );
			scene.getStylesheets().add(SplashView.class.getResource("fonts.css").toExternalForm());
			scene.getStylesheets().add(SplashView.class.getResource("prod".equals(StarterConst.APP_TYPE) ? "colors.css" : "colors-test.css").toExternalForm());
			scene.getStylesheets().add(SplashView.class.getResource("root.css").toExternalForm());
			scene.getStylesheets().add(SplashView.class.getResource("test.css").toExternalForm());

			decorator.applyCss();
			decorator.layout();

			Platform.runLater(() -> {
		    	if (logger.isDebugEnabled()) {
		    		logger.debug("Show stage... devMode");
		    	}

//		    	primaryStage.setTitle("TGL-Scan");
		    	primaryStage.setMinWidth(650);
				primaryStage.setMinHeight(500);
				primaryStage.setScene(scene);
				primaryStage.setOnShown(event -> {
					stageOnShown(primaryStage, view);
				});
				primaryStage.setOnCloseRequest(event -> {
					onCloseRequest_stage(event, primaryStage, view);
				});
				primaryStage.show();
				toolbarPane.prefWidthProperty().bind(primaryStage.widthProperty());
			});
		} else {
			decorator = new JFXDecorator(stage, view, false, true, true);
			decorator.setGraphic(toolbarPane);
			StackPane stackPane = new StackPane(decorator);
			Scene scene = new Scene(stackPane);
			scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
				public void handle(final KeyEvent keyEvent) {
					view.setOnKeyPressed(keyEvent);
				}
			} );
			scene.getStylesheets().addAll(primaryStage.getScene().getStylesheets());

			decorator.applyCss();
			decorator.layout();

			Platform.runLater(() -> {
		    	if (logger.isDebugEnabled()) {
		    		logger.debug("Show stage... runningMode");
		    	}

//		    	stage.setTitle("TGL-Scan");
		    	stage.setMinWidth(650);
				stage.setMinHeight(500);
				stage.getIcons().addAll(images);
				stage.setScene(scene);
				stage.setOnShown(event -> {
					stageOnShown(stage, view);
				});
				stage.setOnCloseRequest(event -> {
					onCloseRequest_stage(event, stage, view);
				});
		    	if (logger.isDebugEnabled()) {
		    		logger.debug("Stage {} show, state {} hide!", stage, primaryStage);
		    	}
				stage.show();
				toolbarPane.prefWidthProperty().bind(stage.widthProperty());
				primaryStage.hide();
			});
		}
	}

	private static boolean isEmptyDirectory(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			try (DirectoryStream<Path> dir = Files.newDirectoryStream(path)) {
				return !dir.iterator().hasNext();
			}
		}
		return false;
	}

	private void onCloseRequest_stage(WindowEvent event, Stage stage, MainView view) {
    	if (logger.isDebugEnabled()) {
    		logger.debug("");
    	}

		String result = DialogUtil.showConfirm(primaryStage, "請確認佇列區影像皆上傳，確定退出？", new String[] {"是(退出)", "　否　"});
		if ("是(退出)".equals(result)) {
			int x = (int)stage.getX();
			int y = (int)stage.getY();
			int width = (int)stage.getWidth();
			int height = (int)stage.getHeight();
			float dividerPosition = view.getDividerPosition();

	    	if (logger.isDebugEnabled()) {
	    		logger.debug("Write properties.");
	    	}

	    	PropertiesCache.getInstance().setProperty(PropertiesCache.PROP_KEY.UI_WIN_X.propName(), Integer.toString(x));
			PropertiesCache.getInstance().setProperty(PropertiesCache.PROP_KEY.UI_WIN_Y.propName(), Integer.toString(y));
			PropertiesCache.getInstance().setProperty(PropertiesCache.PROP_KEY.UI_WIN_WIDTH.propName(), Integer.toString(width));
			PropertiesCache.getInstance().setProperty(PropertiesCache.PROP_KEY.UI_WIN_HEIGHT.propName(), Integer.toString(height));
			PropertiesCache.getInstance().setProperty(PropertiesCache.PROP_KEY.UI_DIVIDER_POS.propName(), Float.toString(dividerPosition));
			try {
				PropertiesCache.getInstance().flush();
			} catch (IOException e) {
				logger.error("Failed to write properties file " + PropertiesCache.PROPS_FILE_NAME + " !");
			}

	    	if (logger.isDebugEnabled()) {
	    		logger.debug("Close http connection.");
	    	}
			EbaoClient.getInstance().close();

			if (Imaging.isTwainLoaded()) {
		    	if (logger.isDebugEnabled()) {
		    		logger.debug("Unload twain.");
		    	}
				Imaging.unloadTwain();
			}

	    	if (logger.isDebugEnabled()) {
	    		logger.debug("Exit.");
	    	}
		} else {
			event.consume();
		}
	}

	private void stageOnShown(Stage stage, MainView view) {
		String _x = PropertiesCache.getInstance().getProperty(PropertiesCache.PROP_KEY.UI_WIN_X.propName());
		String _y = PropertiesCache.getInstance().getProperty(PropertiesCache.PROP_KEY.UI_WIN_Y.propName());
		String _width = PropertiesCache.getInstance().getProperty(PropertiesCache.PROP_KEY.UI_WIN_WIDTH.propName());
		String _height = PropertiesCache.getInstance().getProperty(PropertiesCache.PROP_KEY.UI_WIN_HEIGHT.propName());
		String _dividerPosition = PropertiesCache.getInstance().getProperty(PropertiesCache.PROP_KEY.UI_DIVIDER_POS.propName());

    	if (logger.isDebugEnabled()) {
    		logger.debug("properties.x=" + _x);
    		logger.debug("properties.y=" + _y);
    		logger.debug("properties.width=" + _width);
    		logger.debug("properties.height=" + _height);
    	}

		try {
			int x = Integer.parseInt(_x);
			int y = Integer.parseInt(_y);
			int width = Integer.parseInt(_width);
			int height = Integer.parseInt(_height);
			stage.setX(x);
			stage.setY(y);
			stage.setWidth(width);
			stage.setHeight(height);
		} catch (Exception e) {
			logger.error("Property position must be numeric! Message: " + e.getMessage());
		}

		String launchParameters = txtLaunchParameters!=null 
			? "RunningMode|false|" + txtLaunchParameters.getText()	// Running Mode
			: "DevMode|false|false|false|||||||"					// Dev Mode
			;
		view.stageShown(launchParameters);

		float dividerPosition = 0.5f;
		try {
			dividerPosition = Float.parseFloat(_dividerPosition);
		} catch (Exception e) {
			logger.error("Property dividerPosition must be numeric! Message: " + e.getMessage());
		}
		view.setDividerPosition(dividerPosition);
	}

}