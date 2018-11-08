package com.gmail.xfrednet.xfutils.util.logger;

import com.gmail.xfrednet.xfutils.util.Logger;

public class NoLogLogger extends Logger {

	public NoLogLogger() {
		super(false);
	}

	@Override
	protected void logMessage(String message, int logLevel) {
		
	}

	@Override
	protected void logMessage(String message, Exception e, int logLevel) {
		
	}

}
