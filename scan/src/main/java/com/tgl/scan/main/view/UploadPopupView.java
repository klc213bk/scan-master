package com.tgl.scan.main.view;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXTooltip;
import com.tgl.scan.starter.AbstractFxView;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

public class UploadPopupView extends AbstractFxView {

	@FXML
    private StackPane btnUploadFileContainer;
	@FXML
    private StackPane btnUploadFilesContainer;
	@FXML
    private StackPane btnUploadStatusContainer;

	public UploadPopupView() {
		super();
//		init();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
        final JFXTooltip btnUploadFileTooltip = new JFXTooltip("Ctrl+U");
        final JFXTooltip btnUploadFilesTooltip = new JFXTooltip("Ctrl+I");
        final JFXTooltip btnUploadStatusTooltip = new JFXTooltip("Ctrl+O");
        JFXTooltip.install(btnUploadFileContainer, btnUploadFileTooltip, Pos.BOTTOM_CENTER);
        JFXTooltip.install(btnUploadFilesContainer, btnUploadFilesTooltip, Pos.BOTTOM_CENTER);
        JFXTooltip.install(btnUploadStatusContainer, btnUploadStatusTooltip, Pos.BOTTOM_CENTER);
	}

}
