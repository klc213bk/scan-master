package com.tgl.scan.main.service;

import com.tgl.scan.main.bean.ConnectionInfo;
import com.tgl.scan.main.bean.LoginStatus;
import com.tgl.scan.main.bean.ScanConfig;
import com.tgl.scan.main.http.EBaoException;
import com.tgl.scan.main.http.EbaoClient;
import com.tgl.scan.main.util.ObjectsUtil;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class LoginService extends Service<LoginStatus> {

	private final StringProperty hostName = new SimpleStringProperty();
	private final StringProperty hostUrl = new SimpleStringProperty();
	private final StringProperty userName = new SimpleStringProperty();
	private final StringProperty password = new SimpleStringProperty();

	private String processCode = null;
	private ScanConfig scanConfig = null;

	public final String getHostName() {
		return hostName.get();
	}

	public final void setHostName(String hostName) {
		this.hostName.set(hostName);
	}

	public final String getHostUrl() {
		return hostUrl.get();
	}

	public final void setHostUrl(String hostUrl) {
		this.hostUrl.set(hostUrl);
	}

	public final String getUserName() {
		return userName.get();
	}

	public final void setUserName(String userName) {
		this.userName.set(userName);
	}

	public final String getPassword() {
		return password.get();
	}

	public final void setPassword(String password) {
		this.password.set(password);
	}

	@Override
	protected Task<LoginStatus> createTask() {
		final String _hostName = getHostName();
		if (_hostName == null) {
			throw new IllegalStateException("hostName property value is null");
		}
		final String _hostUrl = getHostUrl();
		if (_hostUrl == null) {
			throw new IllegalStateException("hostUrl property value is null");
		}
		final String _userName = getUserName();
		if (_userName == null) {
			throw new IllegalStateException("userName property value is null");
		}
		final String _password = getPassword();
		if (_password == null) {
			throw new IllegalStateException("password property value is null");
		}

		processCode = LoginStatus.PROC_CODE_NA;

		return new Task<LoginStatus>() {
			@Override
			protected LoginStatus call() throws EBaoException {
	        	EbaoClient eBaoClient = EbaoClient.getInstance()
	                .setHostUrl(_hostUrl)
	                .setUserName(_userName)
	                .setPassword(_password)
	                .init();
				ConnectionInfo connInfo = eBaoClient.connect();
				boolean scanAllowed = eBaoClient.login();
				scanConfig = eBaoClient.getScanConfig();
	        	processCode = LoginStatus.PROC_CODE_SUCCESS;

	    		return new LoginStatus(
    				processCode, 
    				ObjectsUtil.isNotEmpty(connInfo.getServerName()) ? connInfo.getServerName() : _hostName,
    				connInfo.getRequestToken(),
    				scanConfig
    			);
			}
		};
	}

}
