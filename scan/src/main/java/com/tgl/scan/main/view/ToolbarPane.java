package com.tgl.scan.main.view;

import com.jfoenix.controls.JFXBadge;
import com.jfoenix.controls.JFXButton;
import com.tgl.scan.starter.StarterConst;

import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.SVGPath;

public class ToolbarPane extends BorderPane {

	public static Pane RUNNING_MODE_STANDARD = createStandardGraphicPane();
	public static Pane RUNNING_MODE_REPLACE_INSERT = createReplaceInsertGraphicPane();

	private JFXBadge uploadBadge;
	private JFXButton uploadBadgeButton;
	private Label loginStatusLabel;
	private JFXButton loginButton;
	private JFXButton runningModeButton;
	private JFXButton settingsButton;
	private Label serverLabel;

	public ToolbarPane() {
		init();
	}

	private void init() {
        HBox leftItems = new HBox();
        leftItems.setSpacing(10);
        leftItems.setAlignment(Pos.CENTER_LEFT);
        this.setLeft(leftItems);

        ImageView icon = new ImageView(ToolbarPane.class.getClassLoader().getResource("images/icon-scanner.png").toString());
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        leftItems.getChildren().add(icon);

        Label tglLabel = new Label();
        tglLabel.setStyle("-fx-padding: 2 5 2 5;");
        tglLabel.getStyleClass().add("custom-jfx-decorator-label");
        tglLabel.setText("全球人壽 > " + StarterConst.APP_TITLE);
        leftItems.getChildren().add(tglLabel);

        HBox centerItems = new HBox();
        centerItems.setStyle("-fx-padding: 0 50 0 50;");
        centerItems.setSpacing(10);
        centerItems.setAlignment(Pos.CENTER);
        this.setCenter(centerItems);

        this.uploadBadgeButton = createUploadBadgeButton();
        StackPane uploadBadgePane = new StackPane();
        uploadBadgePane.setStyle("-fx-padding: 2 12 2 2;");
        uploadBadgePane.getChildren().add(uploadBadgeButton);
        this.uploadBadge = new JFXBadge();
        this.uploadBadge.getStyleClass().add("icons-badge");
        this.uploadBadge.setPosition(Pos.TOP_RIGHT);
        this.uploadBadge.setText("99");
        this.uploadBadge.setControl(uploadBadgePane);
        centerItems.getChildren().add(this.uploadBadge);

        this.loginStatusLabel = new Label();
        this.loginStatusLabel.setStyle("-fx-padding: 0 0 0 50;");
        this.loginStatusLabel.getStyleClass().add("custom-jfx-decorator-label");
        this.loginStatusLabel.setText("歡迎001713");
        centerItems.getChildren().add(this.loginStatusLabel);

        this.loginButton = createLoginButton();
        centerItems.getChildren().add(this.loginButton);

        this.runningModeButton = createRunningModeButton();
        centerItems.getChildren().add(this.runningModeButton);

        this.settingsButton = createSettingsButton();
//        centerItems.getChildren().add(this.settingsButton);

        HBox rightItems = new HBox();
        rightItems.setSpacing(10);
        rightItems.setAlignment(Pos.CENTER_RIGHT);
        this.setRight(rightItems);

        this.serverLabel = new Label();
        serverLabel.setStyle("-fx-font-size: 8px; -fx-padding: 2; -fx-border-color: WHITE;");
        serverLabel.getStyleClass().add("custom-jfx-decorator-label");
        rightItems.getChildren().add(serverLabel);
	}

	private JFXButton createUploadBadgeButton() {
        SVGPath svgath = new SVGPath();
        svgath.setContent("m17,9c-0.115,0 -0.231,0.005 -0.351,0.015c-0.825,-2.377 -3.062,-4.015 -5.649,-4.015c-3.309,0 -6,2.691 -6,6c0,0.042 0,0.084 0.001,0.126c-1.724,0.445 -3.001,2.013 -3.001,3.874c0,2.206 1.794,4 4,4l5,0l0,-4.586l-1.293,1.293c-0.195,0.195 -0.451,0.293 -0.707,0.293s-0.512,-0.098 -0.707,-0.293c-0.391,-0.391 -0.391,-1.023 0,-1.414l2.999,-2.999c0.093,-0.093 0.203,-0.166 0.326,-0.217c0.244,-0.101 0.52,-0.101 0.764,0c0.123,0.051 0.233,0.124 0.326,0.217l2.999,2.999c0.391,0.391 0.391,1.023 0,1.414c-0.195,0.195 -0.451,0.293 -0.707,0.293s-0.512,-0.098 -0.707,-0.293l-1.293,-1.293l0,4.586l4,0c2.757,0 5,-2.243 5,-5s-2.243,-5 -5,-5z");
        Pane graphicPane = new Pane();
        graphicPane.getChildren().add(svgath);

        JFXButton button = new JFXButton();
        button.getStyleClass().add("custom-jfx-decorator-ripple-button");
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setGraphic(graphicPane);
        button.setPrefSize(28, 28);

        return button;
	}

	private JFXButton createLoginButton() {
        Ellipse ellipse = new Ellipse();
        ellipse.setRadiusX(5);
        ellipse.setRadiusY(6);
        ellipse.setCenterX(12);
        ellipse.setCenterY(8);
        SVGPath svgath = new SVGPath();
        svgath.setContent("m21.8,19.1c-0.9,-1.8 -2.6,-3.3 -4.8,-4.2c-0.6,-0.2 -1.3,-0.2 -1.8,0.1c-1,0.6 -2,0.9 -3.2,0.9s-2.2,-0.3 -3.2,-0.9c-0.5,-0.2 -1.2,-0.3 -1.8,0c-2.2,0.9 -3.9,2.4 -4.8,4.2c-0.7,1.3 0.4,2.8 1.9,2.8l15.8,0c1.5,0 2.6,-1.5 1.9,-2.9z");
        Pane graphicPane = new Pane();
        graphicPane.getChildren().add(ellipse);
        graphicPane.getChildren().add(svgath);
        graphicPane.setScaleX(0.7);
        graphicPane.setScaleY(0.7);

        JFXButton button = new JFXButton();
        button.getStyleClass().add("custom-jfx-decorator-ripple-button");
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setGraphic(graphicPane);
        button.setPrefSize(28, 28);

        return button;
	}

	private JFXButton createSettingsButton() {
        SVGPath svgath = new SVGPath();
        svgath.setContent("m21.521,10.146c-0.41,-0.059 -0.846,-0.428 -0.973,-0.82l-0.609,-1.481c-0.191,-0.365 -0.146,-0.935 0.1,-1.264l0.99,-1.318c0.246,-0.33 0.227,-0.854 -0.047,-1.162l-1.084,-1.086c-0.309,-0.272 -0.832,-0.293 -1.164,-0.045l-1.316,0.988c-0.33,0.248 -0.898,0.293 -1.264,0.101l-1.48,-0.609c-0.395,-0.126 -0.764,-0.562 -0.82,-0.971l-0.234,-1.629c-0.057,-0.409 -0.441,-0.778 -0.85,-0.822c0,0 -0.255,-0.026 -0.77,-0.026c-0.514,0 -0.769,0.026 -0.769,0.026c-0.41,0.044 -0.794,0.413 -0.852,0.822l-0.233,1.629c-0.058,0.409 -0.427,0.845 -0.82,0.971l-1.48,0.609c-0.366,0.191 -0.934,0.147 -1.264,-0.101l-1.318,-0.989c-0.33,-0.248 -0.854,-0.228 -1.163,0.045l-1.084,1.086c-0.272,0.309 -0.294,0.832 -0.046,1.162l0.988,1.318c0.249,0.33 0.293,0.899 0.102,1.264l-0.611,1.482c-0.125,0.393 -0.562,0.762 -0.971,0.82l-1.629,0.231c-0.408,0.059 -0.777,0.442 -0.82,0.853c0,0 -0.027,0.255 -0.027,0.77s0.027,0.77 0.027,0.77c0.043,0.411 0.412,0.793 0.82,0.852l1.629,0.232c0.408,0.059 0.846,0.428 0.971,0.82l0.611,1.48c0.191,0.365 0.146,0.936 -0.102,1.264l-0.988,1.318c-0.248,0.33 -0.308,0.779 -0.132,0.994c0.175,0.217 0.677,0.752 0.678,0.754s0.171,0.156 0.375,0.344s1.042,0.449 1.372,0.203l1.317,-0.99c0.33,-0.246 0.898,-0.293 1.264,-0.1l1.48,0.609c0.394,0.125 0.763,0.562 0.82,0.971l0.233,1.629c0.058,0.408 0.441,0.779 0.852,0.822c0,0 0.255,0.027 0.769,0.027c0.515,0 0.77,-0.027 0.77,-0.027c0.409,-0.043 0.793,-0.414 0.85,-0.822l0.234,-1.629c0.057,-0.408 0.426,-0.846 0.82,-0.971l1.48,-0.611c0.365,-0.191 0.934,-0.146 1.264,0.102l1.318,0.99c0.332,0.246 0.854,0.227 1.164,-0.047l1.082,-1.084c0.273,-0.311 0.293,-0.834 0.047,-1.164l-0.99,-1.318c-0.246,-0.328 -0.291,-0.898 -0.1,-1.264l0.609,-1.48c0.127,-0.393 0.562,-0.762 0.973,-0.82l1.627,-0.232c0.41,-0.059 0.779,-0.441 0.822,-0.852c0,0 0.027,-0.255 0.027,-0.77s-0.027,-0.77 -0.027,-0.77c-0.043,-0.41 -0.412,-0.794 -0.822,-0.853l-1.626,-0.231zm-9.521,4.854c-1.657,0 -3,-1.344 -3,-3c0,-1.657 1.343,-3 3,-3c1.657,0 3,1.344 3,3c0,1.656 -1.344,3 -3,3z");
        Pane btnLoginGraphicPane = new Pane();
        btnLoginGraphicPane.getChildren().add(svgath);
        btnLoginGraphicPane.setScaleX(0.7);
        btnLoginGraphicPane.setScaleY(0.7);

        JFXButton button = new JFXButton();
        button.getStyleClass().add("custom-jfx-decorator-ripple-button");
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setGraphic(btnLoginGraphicPane);
        button.setPrefSize(28, 28);

        return button;
	}

	private JFXButton createRunningModeButton() {
        JFXButton button = new JFXButton();
        button.getStyleClass().add("custom-jfx-decorator-ripple-button");
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setGraphic(RUNNING_MODE_STANDARD);
        button.setPrefSize(28, 28);

        return button;
	}

	public JFXBadge getUploadBadge() {
		return this.uploadBadge;
	}

	public JFXButton getUploadBadgeButton() {
		return this.uploadBadgeButton;
	}

	public JFXButton getLoginButton() {
		return this.loginButton;
	}

	public JFXButton getRunningModeButton() {
		return this.runningModeButton;
	}

	public JFXButton getSettingsButton() {
		return this.settingsButton;
	}

	public Label getLoginStatusLabel() {
		return this.loginStatusLabel;
	}

	public Label getServerLabel() {
		return this.serverLabel;
	}

	private static Pane createStandardGraphicPane() {
		SVGPath svgath1 = new SVGPath();
		SVGPath svgath2 = new SVGPath();
        svgath1.setContent("m15.32895,5.72656a0.92434,0.88281 0 0 0 0.92434,0.88281l3.63266,0a0.18487,0.17656 0 0 0 0.12941,-0.30016l-4.37214,-4.1757a0.18487,0.17656 0 0 0 -0.31428,0.12359l0,3.46945z");
        svgath2.setContent("m16.25329,8.375a2.77303,2.64844 0 0 1 -2.77303,-2.64844l0,-4.14922a0.2773,0.26484 0 0 0 -0.2773,-0.26484l-7.11743,0a2.77303,2.64844 0 0 0 -2.77303,2.64844l0,15.89063a2.77303,2.64844 0 0 0 2.77303,2.64844l12.01645,0a2.77303,2.64844 0 0 0 2.77303,-2.64844l0,-11.21172a0.2773,0.26484 0 0 0 -0.2773,-0.26484l-4.34441,0z");
        Pane graphicPane = new Pane();
        graphicPane.getChildren().add(svgath1);
        graphicPane.getChildren().add(svgath2);
        graphicPane.setScaleX(0.7);
        graphicPane.setScaleY(0.7);
        return graphicPane;
	}

	private static Pane createReplaceInsertGraphicPane() {
		SVGPath svgath1 = new SVGPath();
		SVGPath svgath2 = new SVGPath();
        svgath1.setContent("m15.99343,7.06251l3.54217,0a0.18026,0.17292 0 0 0 0.12618,-0.29396l-4.26322,-4.08948a0.18026,0.17292 0 0 0 -0.30645,0.12104l0,3.39781a0.90132,0.86458 0 0 0 0.90132,0.86458z");
        svgath2.setContent("m20.22962,8.79167l-4.23619,0a2.70395,2.59375 0 0 1 -2.70395,-2.59375l0,-4.06354a0.27039,0.25938 0 0 0 -0.27039,-0.25938l-6.94013,0a2.70395,2.59375 0 0 0 -2.70395,2.59375l0,15.56251a2.70395,2.59375 0 0 0 2.70395,2.59375l11.71711,0a2.70395,2.59375 0 0 0 2.70395,-2.59375l0,-10.98021a0.27039,0.25938 0 0 0 -0.27039,-0.25938zm-5.58816,6.48438l-2.25329,0l0,2.16146a0.90132,0.86458 0 0 1 -1.80263,0l0,-2.16146l-2.25329,0a0.90132,0.86458 0 0 1 0,-1.72917l2.25329,0l0,-2.16146a0.90132,0.86458 0 0 1 1.80263,0l0,2.16146l2.25329,0a0.90132,0.86458 0 0 1 0,1.72917z");
        Pane graphicPane = new Pane();
        graphicPane.getChildren().add(svgath1);
        graphicPane.getChildren().add(svgath2);
        graphicPane.setScaleX(0.7);
        graphicPane.setScaleY(0.7);
        return graphicPane;
	}

}
