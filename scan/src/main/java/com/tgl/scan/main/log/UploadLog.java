package com.tgl.scan.main.log;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;

import com.tgl.scan.main.bean.ScannedImage;

public class UploadLog {

	private static final Logger logger = LogManager.getLogger(UploadLog.class);

	public static final String UPLOAD_LOG_FILE_FULL_NAME = getUploadLoggerFilename();

	public static String getUploadLoggerFilename() {
		if (logger.isDebugEnabled()) {
			logger.debug("");
		}
		String filename = null;

		try {
			LoggerContext ctx = (LoggerContext) LogManager.getContext(true);
			Configuration cfg = ctx.getConfiguration();
			for (String key : cfg.getAppenders().keySet()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Appender: key={}, appender={}", cfg.getAppenders().get(key).toString());
				}
			}
			FileAppender uploadlogAppender = (FileAppender) cfg.getAppender("UPLOADLOG");
			if (uploadlogAppender!=null) {
				filename = uploadlogAppender.getFileName();
			}
		} catch (Exception e) {
			logger.error("Failed to get uploadlog appender filename! ", e);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("uploadlogAppender={}", filename);
		}
		return filename;
	}

	public static void appendSuccess(ScannedImage imageItem) {
		append("success", imageItem, null);
	}

	public static void appendFailed(ScannedImage imageItem, String errorMessage) {
		append("failed", imageItem, errorMessage);
	}

	public static void append(String result, ScannedImage imageItem, String errorMessage) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String currTime = sdf.format(new Date());

		StringBuffer sb = new StringBuffer();
		sb.append("------------------------------------------------------------------------------------------------------\r\n");
		sb.append("[Time]\r\n");
		sb.append("  " + currTime + "  \r\n");
		sb.append("[Record Data]\r\n");
		sb.append(imageItem.toLogString());
		sb.append("[Upload Result]\r\n");
		sb.append("  " + result + "\r\n");
		if (errorMessage != null) {
			sb.append("  " + errorMessage + "\r\n");
		}

		String logContent = sb.toString();
		logger.info(logContent);
	}

	public static void appendSummary(int cntUpload, int cntSuccess, int cntFailed) {
		StringBuffer sb = new StringBuffer();
		sb.append("======================================================================================================\r\n");
		sb.append("[Summary]\r\n");
		sb.append("  Upload : " + cntUpload + "\r\n");
		sb.append("  Success : " + cntSuccess + "\r\n");
		sb.append("  Failed : " + cntFailed + "\r\n");
		sb.append("\r\n\r\n");

		String logContent = sb.toString();
		logger.info(logContent);
	}

}
