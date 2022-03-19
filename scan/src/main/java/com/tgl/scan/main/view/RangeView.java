package com.tgl.scan.main.view;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.tgl.scan.starter.AbstractFxView;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class RangeView extends AbstractFxView {

    @FXML
    private JFXTextField txtFrom;
    @FXML
    private JFXTextField txtTo;
    @FXML
	private JFXButton btnOK;
    @FXML
	private JFXButton btnCancel;

    private int toScanOrder;
    private int fromScanOrder = -1;

	public RangeView(int toScanOrder) {
		super();
		this.toScanOrder = toScanOrder;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		btnOK.setOnAction( action -> {
			onAction_btnOK();
		});
		btnCancel.setOnAction( action -> {
			closeWindow();
		});
	}

    public void stageShown() {
    	txtFrom.setTextFormatter(
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
    	txtFrom.setText("");
    	txtFrom.requestFocus();
		txtTo.setText(Integer.toString(this.toScanOrder));
		txtTo.setDisable(true);

    	getScene().getWindow().addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
            	keyEvent.consume();
    			closeWindow();
            } else if (keyEvent.getCode() == KeyCode.ENTER) {
            	keyEvent.consume();
            	btnOK.requestFocus();
            	btnOK.fire();
            }
        });
    }

    private void onAction_btnOK() {
		if ( txtFrom.validate() ) {
			String from = txtFrom.getText();
			fromScanOrder = Integer.parseInt(from);
			closeWindow();
		}
    }

    private void closeWindow() {
		Platform.runLater(() -> {
			Stage stage = (Stage) RangeView.this.getScene().getWindow();
			stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
		});
	}

	public int getFromScanOrder() {
		return this.fromScanOrder;
	}

}
