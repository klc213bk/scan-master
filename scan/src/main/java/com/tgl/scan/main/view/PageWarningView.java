package com.tgl.scan.main.view;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jfoenix.controls.JFXButton;
import com.tgl.scan.main.bean.PageWarning;
import com.tgl.scan.starter.AbstractFxView;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class PageWarningView extends AbstractFxView {

	private static final Logger logger = LogManager.getLogger(PageWarningView.class);

	public static final String BUTTON_TEXT_CANCEL = "返回檢查";
	public static final String BUTTON_TEXT_UPLOAD = "仍需上傳";

	// readonly table view
	@FXML
    private TableView<PageWarning> pageWarningTableView;
    @FXML
    private TableColumn<PageWarning, String> scanOrderColumn;
    @FXML
    private TableColumn<PageWarning, String> fileCodeColumn;
    @FXML
    private TableColumn<PageWarning, String> mainFileTypeColumn;
    @FXML
    private TableColumn<PageWarning, String> fileTypeColumn;
    @FXML
    private TableColumn<PageWarning, String> companyCodeColumn;
    @FXML
    private TableColumn<PageWarning, String> personalCodeColumn;
    @FXML
    private TableColumn<PageWarning, String> filePageColumn;
    @FXML
    private TableColumn<PageWarning, String> scanTimeColumn;
    @FXML
    private TableColumn<PageWarning, String> remarkColumn;
	@FXML
	private JFXButton btnCancel;
    @FXML
	private JFXButton btnUpload;

    private MainView parent;
    private ObservableList<PageWarning> data = null;
    private Integer selectedIndexNo = null;

	public PageWarningView(MainView parent, ObservableList<PageWarning> data) {
		super();
		this.parent = parent;
		this.data = data;
	}

    @Override
	public void initialize(URL location, ResourceBundle resources) {

    	scanOrderColumn.setCellValueFactory(cellData -> cellData.getValue().scanOrderProperty());
    	scanOrderColumn.setSortable(false);
    	fileCodeColumn.setCellValueFactory(cellData -> cellData.getValue().fileCodeProperty());
    	fileCodeColumn.setSortable(false);
    	mainFileTypeColumn.setCellValueFactory(cellData -> cellData.getValue().mainFileTypeProperty());
    	mainFileTypeColumn.setSortable(false);
    	fileTypeColumn.setCellValueFactory(cellData -> cellData.getValue().fileTypeProperty());
    	fileTypeColumn.setSortable(false);
    	companyCodeColumn.setCellValueFactory(cellData -> cellData.getValue().companyCodeProperty());
    	companyCodeColumn.setSortable(false);
    	personalCodeColumn.setCellValueFactory(cellData -> cellData.getValue().personalCodeProperty());
    	personalCodeColumn.setSortable(false);
    	filePageColumn.setCellValueFactory(cellData -> cellData.getValue().filePageProperty());
    	filePageColumn.setSortable(false);
    	scanTimeColumn.setCellValueFactory(cellData -> cellData.getValue().scanTimeProperty());
    	scanTimeColumn.setSortable(false);
    	remarkColumn.setCellValueFactory(cellData -> cellData.getValue().remarkProperty());
    	remarkColumn.setSortable(false);

		//加入 TableView 點選事件動作
    	pageWarningTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    	pageWarningTableView.setEditable(false);

        btnCancel.setText(BUTTON_TEXT_CANCEL);
		btnCancel.setOnAction( action -> {
			onAction_btnCancel();
		});
		btnUpload.setText(BUTTON_TEXT_UPLOAD);
		btnUpload.setOnAction( action -> {
			onAction_btnUpload();
		});
	}

    public void stageShown() {
    	pageWarningTableView.prefHeightProperty().bind(PageWarningView.this.getScene().heightProperty());
    	pageWarningTableView.getItems().addAll(this.data);
        //Set the right policy
    	pageWarningTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    	pageWarningTableView.getColumns().stream().forEach( (column) -> {
            //Minimal width = columnheader
            Text t = new Text(column.getText());
            double max = t.getLayoutBounds().getWidth();
            for ( int i = 0; i < pageWarningTableView.getItems().size(); i++ ) {
                //cell must not be empty
                if ( column.getCellData(i) != null ) {
                    t = new Text(column.getCellData(i).toString());
                    double calcwidth = t.getLayoutBounds().getWidth();
                    //remember new max-width
                    if ( calcwidth > max ) {
                        max = calcwidth;
                    }
                }
            }
            //set the new max-widht with some extra space
            column.setPrefWidth( max + 10.0d );
        } );

        getScene().getWindow().addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
            	keyEvent.consume();
            	btnCancel.requestFocus();
            	onAction_btnCancel();
            }
        });
    }

    private void onAction_btnCancel() {
		PageWarning pageWarning = pageWarningTableView.getSelectionModel().getSelectedItem();
		if (null != pageWarning) {
			selectedIndexNo = pageWarning.indexNoProperty().getValue();
		}
		closeWindow();
    }

    private void onAction_btnUpload() {
		selectedIndexNo = Integer.valueOf("-1");
		closeWindow();
    }

    private void closeWindow() {
		Platform.runLater(() -> {
			Stage stage = (Stage) PageWarningView.this.getScene().getWindow();
			stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
		});
	}

	public Integer getSelectedIndexNo() {
		return selectedIndexNo;
	}

}
