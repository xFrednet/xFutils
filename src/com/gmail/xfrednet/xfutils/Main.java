package com.gmail.xfrednet.xfutils;

import com.gmail.xfrednet.xfutils.util.logger.ConsoleLogger;
import com.gmail.xfrednet.xfutils.util.logger.FileLogger;
import com.gmail.xfrednet.xfutils.util.logger.NoLogLogger;
import com.gmail.xfrednet.xfutils.util.Logger;

public class Main {
	
	static private Logger logger = null;
	static private boolean debugEnabled = false;

	public static void main(String[] args) {
		if (!ProcessArgs(args)) {
			logger.endLog();			
			return; // ProcessArgs has failed
		}
// TODO 04.11.2018 remove this log
		logger.logInfo("main: Me message"); 
		logger.endLog();
	}
	private static boolean ProcessArgs(String[] args) {
		// TODO 04.11.2018 add -noplugin && -nolinks
		for (String arg : args) {
			switch (arg) {
			case "-debug":
				debugEnabled = true;
				break;
			case "-conlog":
				if (logger != null) {
					logger.logError("ProcessArgs: \"-conlog\": Only one log option can be selected at a time.");
				}
				logger = new ConsoleLogger(debugEnabled);
				logger.logInfo("ProcessArgs: The log will be written to the console.");
				break;
			case "-filelog":
				if (logger != null) {
					logger.logError("ProcessArgs: \"-filelog\": Only one log option can be selected at a time.");
				}
				logger = new FileLogger(debugEnabled);
				logger.logInfo("ProcessArgs: The log will be written to a file.");
				break;
			case "-help":
			default:
				System.out.println("Arguments: [-debug][-conlog | -filelog]");
				System.out.println();
				System.out.println("    -conlog:  Writes all logs to the console.");
				System.out.println("    -debug:   Enables debugging information and logs.");
				System.out.println("    -filelog: Writes all logs to a log file.");
				System.out.println("    -help:    Prints this information.");
				return false;
			}
		}

		// logger validation
		if (logger == null) {
			logger = new NoLogLogger();
		}

		// debug info
		if (debugEnabled) {
			logger.logDebugMessage("ProcessArgs: Debugging was enabled!");

			StringBuilder sb = new StringBuilder();
			for (String arg : args) {
				sb.append(arg);
				sb.append(" ");
			}

			logger.logDebugMessage("ProcessArgs: The following arguments where entered: \"" + sb.toString() + "\"");
		}
		return true;
	}
}
