package com.gmail.xfrednet.xfutils.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Logger {

	protected static final int LOG_LEVEL_ERROR = 4;
	protected static final int LOG_LEVEL_ALERT = 3;
	protected static final int LOG_LEVEL_INFO = 2;
	protected static final int LOG_LEVEL_DEBUG = 1;

	private boolean enableDebugLog;

	public Logger(boolean enableDebugLog) {
		this.enableDebugLog = enableDebugLog;
	}
	private static String GetTimestamp() {
		return new SimpleDateFormat("HH:mm:ss").format(new Date());
	}

	abstract protected void logMessage(String message, int logLevel);
	abstract protected void logMessage(String message, Exception e, int logLevel);

	public void logError(String message) {
		logMessage(
			"[ERROR]" + GetTimestamp() + ": " + message,
			LOG_LEVEL_ERROR);
	}
	public void logError(String message, Exception e) {
		logMessage(
			"[ERROR]" + GetTimestamp() + ": " + message,
			e,
			LOG_LEVEL_ERROR);
	}

	public void logAlert(String message) {
		logMessage(
			"[ALERT]" + GetTimestamp() + ": " + message,
			LOG_LEVEL_ALERT);
	}
	public void logAlert(String message, Exception e) {
		logMessage(
				"[ALERT]" + GetTimestamp() + ": " + message,
				e,
				LOG_LEVEL_ALERT);
	}

	public void logInfo(String message) {
		logMessage(
			"[INFO ]" + GetTimestamp() + ": " + message,
			LOG_LEVEL_INFO);
	}
	public void logDebugMessage(String message) {
		if (enableDebugLog) {
			logMessage(
				"[DEBUG]" + GetTimestamp() + ": " + message,
				LOG_LEVEL_DEBUG);
		}
	}

}
