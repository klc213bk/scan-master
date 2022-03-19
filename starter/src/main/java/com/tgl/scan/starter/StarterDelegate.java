package com.tgl.scan.starter;

import static java.lang.System.Logger.Level.INFO;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.update4j.DynamicClassLoader;
import org.update4j.inject.InjectTarget;
import org.update4j.service.Delegate;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class StarterDelegate extends Application implements Delegate {

	// 啟動程序 main() --> init() --> start() --> SplashView.initialize() --> SplashView()

	private static final System.Logger logger = System.getLogger(StarterDelegate.class.getSimpleName());

	@InjectTarget
    public List<String> args;

    private String[] appArguments;

	// 由 Update4j 啟動時會呼叫此方法
	@Override
	public void main(List<String> arguments) throws Throwable {
		Thread.currentThread().setContextClassLoader(new DynamicClassLoader());
		System.setProperty("update4j.suppress.warning", "warn");

		appArguments = arguments==null ? null : arguments.toArray(new String[0]);

        logger.log(INFO, "Run starter...");
        logger.log(INFO, "\n"+(args==null ? "" : (String.join(",", args))) + 
        		"appArguments -> " + (null==appArguments ? "null" : Arrays.toString(appArguments)) + 
        		getSysPropertiesString(System.getProperties()) + 
        		getSysEnvString(System.getenv()));
		launch(appArguments);
	}

	// 由開發環境啟動時會呼叫此方法
	public static void main(String[] args) {
        logger.log(INFO, "Run starter...");
		logger.log(INFO, "static main()\n" + 
				"args -> " + (null==args ? "null" : Arrays.toString(args)) + 
				getSysPropertiesString(System.getProperties()) + 
				getSysEnvString(System.getenv()));
		launch(args);
	}

	public static String getSysPropertiesString(java.util.Properties properties) {
		if ( null == properties ) return "";
		StringBuffer sb = new StringBuffer();
        sb.append("\n");
		Map<String, String> map = new TreeMap(properties);
		for (String key : map.keySet()) {
        	sb.append("System property " + key + "=" + map.get(key) + "\n");
		}
        return sb.toString();
	}

	private static String getSysEnvString(java.util.Map<String,String> envMap) {
		if ( null == envMap ) return "";
		StringBuffer sb = new StringBuffer();
        sb.append("\n");
		Map<String, String> map = new TreeMap(envMap);
		for (String key : map.keySet()) {
        	sb.append("System environment " + key + "=" + map.get(key) + "\n");
		}
        return sb.toString();
	}

	public static List<Image> images;

	@Override
	public long version() {
		return 0;
	}

	@Override
	public void init() {
		logger.log(INFO, "hostServices.documentBase=" + getHostServices().getDocumentBase() + ", hostServices.codeBase=" + getHostServices().getCodeBase());

		List<String> sizes = List.of("tiny", "small", "medium", "large", "xlarge");
		images = sizes.stream()
						.map(s -> ("/icons/tgl-scan-" + s + ".png"))
						.map(s -> getClass().getResource(s).toExternalForm())
						.map(Image::new)
						.collect(Collectors.toList());
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		logger.log(INFO, "Show splash view.");

		SplashView startup = new SplashView(primaryStage, appArguments);

		Scene scene = new Scene(startup);
		scene.getStylesheets().addAll(
			getClass().getResource("fonts.css").toExternalForm(),
			getClass().getResource("prod".equals(StarterConst.APP_TYPE) ? "colors.css" : "colors-test.css").toExternalForm(),
			getClass().getResource("root.css").toExternalForm(),
			getClass().getResource("test.css").toExternalForm()
		);

		primaryStage.getIcons().addAll(images);
		primaryStage.setScene(scene);
		primaryStage.setAlwaysOnTop(true);
		primaryStage.initStyle(StageStyle.UNDECORATED);
		primaryStage.setOnShown(event -> {
	        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
	        primaryStage.setX((primScreenBounds.getWidth()-primaryStage.getWidth())/2);
	        primaryStage.setY((primScreenBounds.getHeight()-primaryStage.getHeight())/2);
	        startup.stageShown();
		});
		primaryStage.setOnHidden(event -> {
			Logger rootLogger = Logger.getLogger("");
			for (Handler handler : rootLogger.getHandlers()) {
				handler.close();
				System.out.println("Starter.Handler "+ handler.toString() + " closed!");
			}
		});
		primaryStage.show();
	}

}
