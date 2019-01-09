package com.gmail.xfrednet.xfutils.util.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.gmail.xfrednet.xfutils.util.Logger;

public class FileLogger extends Logger {
	
	PrintWriter writer = null;
	File logFile = null;
	
	public static String GetNewLogFileName() {
		String baseFileName = "../log/log_%s.log";
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

		return String.format(baseFileName, timeStamp);
	}
	
	public FileLogger(boolean enableDebugLog) {
		super(enableDebugLog);
		
		this.logFile = new File(GetNewLogFileName());
		try {
			this.logFile.getParentFile().mkdirs();
			this.logFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			writer = new PrintWriter(
					new OutputStreamWriter(
							new FileOutputStream(this.logFile), StandardCharsets.UTF_8));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean endLog() {
		this.writer.flush();
		this.writer.close();
		
		return true;
	}
	
	@Override
	protected void logMessage(String message, int logLevel) {
		this.writer.println(message);
	}

	@Override
	protected void logMessage(String message, Exception e, int logLevel) {
		this.writer.println(message);
		e.printStackTrace(this.writer);
	}

}
