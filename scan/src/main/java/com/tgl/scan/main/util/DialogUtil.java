package com.tgl.scan.main.util;

import java.util.ArrayList;
import java.util.List;

import com.jfoenix.animation.alert.CenterTransition;
import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXDialogLayout;
import com.tgl.scan.main.bean.LoginStatus;
import com.tgl.scan.main.bean.PageWarning;
import com.tgl.scan.main.bean.ScanConfig;
import com.tgl.scan.main.bean.ScannedImage;
import com.tgl.scan.main.bean.UploadProcessSummary;
import com.tgl.scan.main.view.LoginView;
import com.tgl.scan.main.view.MainView;
import com.tgl.scan.main.view.PageWarningView;
import com.tgl.scan.main.view.RangeView;
import com.tgl.scan.main.view.UploadLogView;
import com.tgl.scan.main.view.UploadView;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class DialogUtil {

	public DialogUtil() {
	}

	public static void showMessage(Window window, String message) {
		showMessage(window, "訊息", message, false);
	}

	public static void showMessageAndWait(Window window, String message) {
		showMessage(window, "訊息", message, true);
	}

	public static void showErrorMessage(Window window, String message) {
		showMessage(window, "錯誤", message, false);
	}

	public static void showErrorMessageAndWait(Window window, String message) {
		showMessage(window, "錯誤", message, true);
	}

	public static void showErrorMessages(Window window, boolean insertBlankLine, String... messages) {
		StringBuffer sb = new StringBuffer();
		for (String message : messages) {
			sb.append(message);
			sb.append(insertBlankLine ? "\n\n" : "\n");
		}
		showMessage(window, "錯誤", sb.toString(), false);
	}

	public static void showErrorMessagesAndWait(Window window, boolean insertBlankLine, String... messages) {
		StringBuffer sb = new StringBuffer();
		for (String message : messages) {
			sb.append(message);
			sb.append(insertBlankLine ? "\n\n" : "\n");
		}
		showMessage(window, "錯誤", sb.toString(), true);
	}

	public static void showMessage(Window window, String heading, String message, boolean showAndWait) {
		JFXAlert alert = new JFXAlert(window==null ? null : window);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initStyle(StageStyle.TRANSPARENT);
        alert.setOverlayClose(false);
        JFXDialogLayout layout = new JFXDialogLayout();
        HBox headerPane = new HBox();
        headerPane.setSpacing(10);
        ImageView icon = null;
        String buttonText = "確定";
        if ("訊息".equals(heading)) {
            icon = new ImageView(MainView.class.getClassLoader().getResource("images/icon-info.png").toString());
        } else if ("錯誤".equals(heading) || heading.indexOf("錯誤")>=0) {
            icon = new ImageView(MainView.class.getClassLoader().getResource("images/icon-warning.png").toString());
            buttonText = "關閉";
        }
        if (icon!=null) {
            icon.setFitHeight(20);
            icon.setFitWidth(20);
            headerPane.getChildren().add(icon);
        }
        headerPane.getChildren().add(new Label(heading));
        layout.setHeading(headerPane);
        layout.setBody(new Label(message));
        JFXButton closeButton = new JFXButton(buttonText);
        closeButton.setMinWidth(80);
        closeButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> alert.hideWithAnimation());
        layout.setActions(closeButton);
        alert.setContent(layout);
        if (showAndWait) {
            alert.showAndWait();
        } else {
            alert.show();
        }
	}

	public static String showConfirm(Window window, String message, String... buttonTexts) {
		return showConfirm(window, "詢問", message, buttonTexts);
	}

	public static String showConfirm(Window window, String heading, String message, String... buttonTexts) {
		JFXAlert<String> alert = new JFXAlert<String>(window==null ? null : window);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initStyle(StageStyle.TRANSPARENT);
        alert.setOverlayClose(false);
        JFXDialogLayout layout = new JFXDialogLayout();
        HBox headerPane = new HBox();
        headerPane.setSpacing(10);
        ImageView icon = null;
        if ("詢問".equals(heading)) {
            icon = new ImageView(MainView.class.getClassLoader().getResource("images/icon-question.png").toString());
        } else if ("錯誤".equals(heading) || heading.indexOf("錯誤")>=0) {
            icon = new ImageView(MainView.class.getClassLoader().getResource("images/icon-warning.png").toString());
        }
        if (icon!=null) {
            icon.setFitHeight(20);
            icon.setFitWidth(20);
            headerPane.getChildren().add(icon);
        }
        headerPane.getChildren().add(new Label(heading));
        layout.setHeading(headerPane);
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(new Label(message));
        layout.setBody(stackPane);

    	// Buttons get added into the actions section of the layout.
        List<JFXButton> buttons = new ArrayList<JFXButton>();
        for (String btnText : buttonTexts) {
        	JFXButton button = new JFXButton(btnText);
        	button.setMinWidth(80);
        	button.setOnAction(addEvent -> {
        		// When the button is clicked, we set the result accordingly
        		alert.setResult(btnText);
        		alert.hideWithAnimation();
        	});
        	buttons.add(button);
        }
        layout.setActions(buttons);

        alert.setContent(layout);
        alert.showAndWait();

        return alert.getResult();
	}

	public static int showRemoveRangeDialog(Window parent, int currentScanOrder) {
    	RangeView view = new RangeView(currentScanOrder);
    	Stage rangeStage = new Stage();
    	JFXDecorator decorator = new JFXDecorator(rangeStage, view, false, false, false);
        ImageView icon = new ImageView(MainView.class.getClassLoader().getResource("images/icon-question.png").toString());
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        decorator.setGraphic(icon);

        Scene scene = new Scene(decorator);
		scene.getStylesheets().addAll(parent.getScene().getStylesheets());
		rangeStage.setTitle("整批刪除");
		rangeStage.setMinWidth(300);
		rangeStage.setMinHeight(300);
		rangeStage.setResizable(false);
		rangeStage.setScene(scene);
		rangeStage.initModality(Modality.WINDOW_MODAL);
		rangeStage.initOwner(parent);
		rangeStage.setOnShown(event -> {
			rangeStage.setX(parent.getX() + (parent.getWidth() - rangeStage.getWidth()) / 2);
			rangeStage.setY(parent.getY() + (parent.getHeight() - rangeStage.getHeight()) / 2);
	        view.stageShown();
		});
		rangeStage.setOnCloseRequest(event -> {
			event.consume();

			CenterTransition animation = new CenterTransition(decorator, view);
            animation.setOnFinished(finish -> {
            	rangeStage.hide();
            });
			animation.jumpTo(animation.getCycleDuration());
			animation.setRate(-1);
			animation.play();
		});

		CenterTransition animation = new CenterTransition(decorator, view);
		animation.play();
		rangeStage.showAndWait();

		return view.getFromScanOrder();
    }

	public static LoginStatus showLoginDialog(MainView parent, int loginStatus, String userName) {
		LoginView view = new LoginView(parent, loginStatus, userName);
    	Stage loginStage = new Stage();
    	JFXDecorator decorator = new JFXDecorator(loginStage, view, false, false, false);
        ImageView icon = new ImageView(MainView.class.getClassLoader().getResource("images/icon-key.png").toString());
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        decorator.setGraphic(icon);

        Stage parentStage = (Stage)parent.getScene().getWindow();
        Scene scene = new Scene(decorator);
		scene.getStylesheets().addAll(parent.getScene().getStylesheets());
		loginStage.setTitle(loginStatus == LoginStatus.STATUS_LOGGED_IN ? "Logout" : "Login");
		loginStage.setMinWidth(350);
		loginStage.setMinHeight(300);
		loginStage.setResizable(false);
		loginStage.setScene(scene);
		loginStage.initModality(Modality.WINDOW_MODAL);
		loginStage.initOwner(parent.getScene().getWindow());
		loginStage.setOnShown(event -> {
	        loginStage.setX(parentStage.getX() + (parentStage.getWidth() - loginStage.getWidth()) / 2);
	        loginStage.setY(parentStage.getY() + (parentStage.getHeight() - loginStage.getHeight()) / 2);
//	        parentStage.setOpacity(0.85f);
	        view.stageShown();
		});
		loginStage.setOnCloseRequest(event -> {
			event.consume();

			if ( view.isLoggingIn() ) {
				return;
			}

			CenterTransition animation = new CenterTransition(decorator, view);
            animation.setOnFinished(finish -> {
            	loginStage.hide();
            });
			animation.jumpTo(animation.getCycleDuration());
			animation.setRate(-1);
			animation.play();
		});

		CenterTransition animation = new CenterTransition(decorator, view);
		animation.play();
		loginStage.showAndWait();
//        parentStage.setOpacity(1.0f);

		return view.getLoginStatus();
    }

	public static Integer showPageNoWarningDialog(MainView parent, ObservableList<PageWarning> data) {
		PageWarningView view = new PageWarningView(parent, data);
    	Stage pageWarningStage = new Stage();
    	JFXDecorator decorator = new JFXDecorator(pageWarningStage, view, false, false, false);
        ImageView icon = new ImageView(MainView.class.getClassLoader().getResource("images/icon-warning.png").toString());
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        decorator.setGraphic(icon);

        Stage parentStage = (Stage)parent.getScene().getWindow();
        Scene scene = new Scene(decorator);
		scene.getStylesheets().addAll(parent.getScene().getStylesheets());
		pageWarningStage.setTitle("請注意:以下影像頁數不符!");
		pageWarningStage.setMinWidth(500);
		pageWarningStage.setMinHeight(400);
		pageWarningStage.setWidth(700);
		pageWarningStage.setHeight(400);
		pageWarningStage.setScene(scene);
		pageWarningStage.initModality(Modality.WINDOW_MODAL);
		pageWarningStage.initOwner(parent.getScene().getWindow());
		pageWarningStage.setOnShown(event -> {
			pageWarningStage.setX(parentStage.getX() + (parentStage.getWidth() - pageWarningStage.getWidth()) / 2);
	        pageWarningStage.setY(parentStage.getY() + (parentStage.getHeight() - pageWarningStage.getHeight()) / 2);
//	        parentStage.setOpacity(0.85f);
	        view.stageShown();
		});
		pageWarningStage.setOnCloseRequest(event -> {
			event.consume();

			CenterTransition animation = new CenterTransition(decorator, view);
            animation.setOnFinished(finish -> {
            	pageWarningStage.hide();
            });
			animation.jumpTo(animation.getCycleDuration());
			animation.setRate(-1);
			animation.play();
		});

		CenterTransition animation = new CenterTransition(decorator, view);
		animation.play();
		pageWarningStage.showAndWait();
//        parentStage.setOpacity(1.0f);

		return view.getSelectedIndexNo();
	}

	public static UploadProcessSummary showUploadDialog(MainView parent, ScanConfig scanConfig, ObservableList<ScannedImage> imageList, Integer selectedIndex) {
		UploadView view = new UploadView(parent, scanConfig, imageList, selectedIndex);
    	Stage uploadStage = new Stage();
    	JFXDecorator decorator = new JFXDecorator(uploadStage, view, false, false, false);
        ImageView icon = new ImageView(MainView.class.getClassLoader().getResource(selectedIndex == null ? "images/icon-action-upload-all.png" : "images/icon-action-upload.png").toString());
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        decorator.setGraphic(icon);

        Stage parentStage = (Stage)parent.getScene().getWindow();
        Scene scene = new Scene(decorator);
		scene.getStylesheets().addAll(parent.getScene().getStylesheets());
		uploadStage.setTitle("上傳");
		uploadStage.setMinWidth(500);
		uploadStage.setMinHeight(400);
		uploadStage.setWidth(700);
		uploadStage.setHeight(400);
		uploadStage.setScene(scene);
		uploadStage.initModality(Modality.WINDOW_MODAL);
		uploadStage.initOwner(parent.getScene().getWindow());
		uploadStage.setOnShown(event -> {
			uploadStage.setX(parentStage.getX() + (parentStage.getWidth() - uploadStage.getWidth()) / 2);
	        uploadStage.setY(parentStage.getY() + (parentStage.getHeight() - uploadStage.getHeight()) / 2);
//	        parentStage.setOpacity(0.85f);
	        view.stageShown();
		});
		uploadStage.setOnCloseRequest(event -> {
			event.consume();

			CenterTransition animation = new CenterTransition(decorator, view);
            animation.setOnFinished(finish -> {
            	uploadStage.hide();
            });
			animation.jumpTo(animation.getCycleDuration());
			animation.setRate(-1);
			animation.play();
		});

		CenterTransition animation = new CenterTransition(decorator, view);
		animation.play();
		uploadStage.showAndWait();
//        parentStage.setOpacity(1.0f);

		return view.getUploadProcessSummary();
	}

	public static void showUploadLogDialog(MainView parent) {
		UploadLogView view = new UploadLogView(parent);
    	Stage uploadLogStage = new Stage();
    	JFXDecorator decorator = new JFXDecorator(uploadLogStage, view, false, false, false);
        ImageView icon = new ImageView(MainView.class.getClassLoader().getResource("images/icon-info.png").toString());
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        decorator.setGraphic(icon);

        Stage parentStage = (Stage)parent.getScene().getWindow();
        Scene scene = new Scene(decorator);
		scene.getStylesheets().addAll(parent.getScene().getStylesheets());
		uploadLogStage.setTitle("上傳記錄資訊");
		uploadLogStage.setMinWidth(500);
		uploadLogStage.setMinHeight(400);
		uploadLogStage.setWidth(500);
		uploadLogStage.setHeight(500);
		uploadLogStage.setScene(scene);
		uploadLogStage.initModality(Modality.WINDOW_MODAL);
		uploadLogStage.initOwner(parent.getScene().getWindow());
		uploadLogStage.setOnShown(event -> {
			uploadLogStage.setX(parentStage.getX() + (parentStage.getWidth() - uploadLogStage.getWidth()) / 2);
	        uploadLogStage.setY(parentStage.getY() + (parentStage.getHeight() - uploadLogStage.getHeight()) / 2);
//	        parentStage.setOpacity(0.85f);
	        view.stageShown();
		});
		uploadLogStage.setOnCloseRequest(event -> {
			event.consume();

			CenterTransition animation = new CenterTransition(decorator, view);
            animation.setOnFinished(finish -> {
            	uploadLogStage.hide();
            });
			animation.jumpTo(animation.getCycleDuration());
			animation.setRate(-1);
			animation.play();
		});

		CenterTransition animation = new CenterTransition(decorator, view);
		animation.play();
		uploadLogStage.showAndWait();
//        parentStage.setOpacity(1.0f);

		return;
	}

}
