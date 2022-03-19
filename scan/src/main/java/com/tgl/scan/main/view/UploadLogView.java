package com.tgl.scan.main.view;

import java.io.BufferedWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jfoenix.controls.JFXButton;
import com.tgl.scan.main.http.EBaoException;
import com.tgl.scan.main.log.UploadLog;
import com.tgl.scan.main.util.DialogUtil;
import com.tgl.scan.main.util.ObjectsUtil;
import com.tgl.scan.starter.AbstractFxView;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class UploadLogView extends AbstractFxView {

	private static final Logger logger = LogManager.getLogger(UploadLogView.class);

	@FXML
	private ScrollPane uploadLogScrollPane;
	@FXML
	private TextArea uploadLogTextArea;
    @FXML
	private JFXButton btnClear;
    @FXML
	private JFXButton btnClose;

    private MainView parent = null;
    private String titleText = null;
    private StringProperty titleProperty = new SimpleStringProperty();

    public UploadLogView(MainView parent) {
		super();
		this.parent = parent;
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		btnClear.setOnAction( action -> {
			onAction_btnClear();
		});
		btnClose.setOnAction( action -> {
			closeWindow();
		});
    }

    public void stageShown() {
		Stage stage = (Stage) UploadLogView.this.getScene().getWindow();
		this.titleText = stage.getTitle();
		setTitle(this.titleText);
		stage.titleProperty().bind(titleProperty);
		uploadLogScrollPane.prefHeightProperty().bind(UploadLogView.this.getScene().heightProperty());
		loadLogContent();

        getScene().getWindow().addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
            	keyEvent.consume();
    			closeWindow();
            }
        });
    }

	private void onAction_btnClear() {
		emptyLogContent();
	}

    private void setTitle(String title) {
		titleProperty.setValue(this.titleText + (ObjectsUtil.isEmpty(title) ? "" : (" - " + title)));
    }

    private void loadLogContent() {
        Task<String> task = new Task<String>() {
            @Override protected String call() throws Exception {
            	StringBuffer sb = new StringBuffer();

                Path path = Paths.get(UploadLog.UPLOAD_LOG_FILE_FULL_NAME);
                //read file to byte array
                byte[] bytes = Files.readAllBytes(path);
//                //read file to String list
//                @SuppressWarnings("unused")
//        		List<String> allLines = Files.readAllLines(path, StandardCharsets.UTF_8);
            	return new String(bytes);
            }
        };
        task.setOnRunning(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
        		setTitle("載入中");
            }
        });
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
            	String content = (String)t.getSource().getValue();
            	uploadLogTextArea.setText(content);
        		setTitle("");
            }
        });
        task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
        		EBaoException e = (EBaoException)t.getSource().getException();
            	String errorMessage = e.getMessage();
        		if (logger.isDebugEnabled()) {
                    logger.debug("UploadService.onFailed(), errorMessage:{}", errorMessage);
        		}
        		setTitle("");
        		DialogUtil.showErrorMessage(parent.getScene().getWindow(), errorMessage);
        	}
        });

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    private void emptyLogContent() {
        Task<Void> task = new Task<Void>() {
            @Override protected Void call() throws Exception {
            	Path path = Paths.get(UploadLog.UPLOAD_LOG_FILE_FULL_NAME);
            	BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
            	writer.write("");
                writer.flush();

                return null;
            }
        };
        task.setOnRunning(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
        		setTitle("清除中");
            }
        });
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
            	uploadLogTextArea.setText("");
        		setTitle("");
            }
        });
        task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
        		EBaoException e = (EBaoException)t.getSource().getException();
            	String errorMessage = e.getMessage();
        		if (logger.isDebugEnabled()) {
                    logger.debug("UploadService.onFailed(), errorMessage:{}", errorMessage);
        		}
        		setTitle("");
        		DialogUtil.showErrorMessage(parent.getScene().getWindow(), errorMessage);
        	}
        });

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

	private void closeWindow() {
		Platform.runLater(() -> {
			Stage stage = (Stage) UploadLogView.this.getScene().getWindow();
			stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
		});
	}

}
