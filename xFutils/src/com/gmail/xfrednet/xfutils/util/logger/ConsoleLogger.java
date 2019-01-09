package com.gmail.xfrednet.xfutils.util.logger;

import com.gmail.xfrednet.xfutils.util.Logger;

public class ConsoleLogger extends Logger {

	public ConsoleLogger(boolean enableDebugLog)
	{
		super(enableDebugLog);
	}

	@Override
	protected void logMessage(String message, int logLevel) {
		switch (logLevel) {
		case LOG_LEVEL_INFO:
		case LOG_LEVEL_ALERT:
		case LOG_LEVEL_DEBUG:
			System.out.println(message);
			break;
		case LOG_LEVEL_ERROR:
			System.err.println(message);
			break;
		default:
			logError("ConsoleLogger.logMessage: unknown log level: \"" + logLevel + "\", message: " + message);
		}
	}

	@Override
	protected void logMessage(String message, Exception e, int logLevel) {
		switch (logLevel) {
		case LOG_LEVEL_INFO:
		case LOG_LEVEL_ALERT:
		case LOG_LEVEL_DEBUG:
			System.out.println(message);
			e.printStackTrace(System.out);
			break;
		case LOG_LEVEL_ERROR:
			System.err.println(message);
			e.printStackTrace(System.err);
			break;
		default:
			logError("ConsoleLogger.logMessage: unknown log level: \"" + logLevel + "\", message: " + message + ", Exception: " + e.getMessage());
		}
	}


}
