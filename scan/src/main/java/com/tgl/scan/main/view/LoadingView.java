package com.tgl.scan.main.view;

import java.net.URL;
import java.util.ResourceBundle;

import com.tgl.scan.starter.AbstractFxView;

import javafx.animation.FadeTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class LoadingView extends AbstractFxView {

	private String text;
	private Image image;

	private FadeTransition fade;

	@FXML
	private Label label;

	@FXML
	private ImageView imageView;

	/*
	public LoadingView(String text, Image image) {
		this.text = text;
		this.image = image;

		if (text != null)
			label.setText(text);

		if (image == null)
			image = new Image(getClass().getResourceAsStream("spinner.gif"));

		imageView.setImage(image);

		fade = new FadeTransition(Duration.millis(200), this);
		fade.setFromValue(0);
		fade.setToValue(1);
		setOpacity(0);

		mouseTransparentProperty().bind(opacityProperty().isEqualTo(0));
	}
	*/
	public LoadingView(String text, Image image) {
		super();
		this.text = text;
		this.image = image;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (text != null)
			label.setText(text);

		if (image == null)
			image = new Image(getClass().getResourceAsStream("spinner.gif"));

		imageView.setImage(image);

		fade = new FadeTransition(Duration.millis(200), this);
		fade.setFromValue(0);
		fade.setToValue(1);
		setOpacity(0);

		mouseTransparentProperty().bind(opacityProperty().isEqualTo(0));
    }

	public LoadingView(String text) {
		this(text, null);
	}

	public LoadingView() {
		this(null);
	}

	public void show() {
		fade.playFromStart();
	}

	public void hide() {
		fade.setRate(-1);
		fade.playFromStart();
	}

	public ObjectProperty<Image> imageProperty() {
		return imageView.imageProperty();
	}

	public final Image getImage() {
		return imageProperty().get();
	}

	public final void setImage(Image image) {
		imageProperty().set(image);
	}

	public StringProperty textProperty() {
		return label.textProperty();
	}

	public final String getText() {
		return textProperty().get();
	}

	public final void setText(String text) {
		textProperty().set(text);
	}

}
