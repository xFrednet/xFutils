package com.gmail.xfrednet.xfutils;

import com.gmail.xfrednet.xfutils.util.logger.ConsoleLogger;
import com.gmail.xfrednet.xfutils.util.logger.FileLogger;
import com.gmail.xfrednet.xfutils.util.logger.NoLogLogger;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.gmail.xfrednet.xfutils.plugin.PluginManager;
import com.gmail.xfrednet.xfutils.util.Logger;

public class Main {
	
	public static Logger logger              = null;
	private static boolean debugEnabled      = false;
	private static boolean argPluginsEnabled = false;
	private static boolean argLinksEnabled   = false;
	
	// This value should only be used by the terminate function
	private static Main instance             = null;
	
	// ##########################################
	// # main #
	// ##########################################
	public static void main(String[] args) {
		if (!ProcessArgs(args)) {
			logger.endLog();
			return; // ProcessArgs has failed
		}
		
		Main main = new Main();
		if (!main.init()) {
			logger.logError("main: Something failed durring the initialize of Main. Goodbye");
			System.exit(1); // TODO create predefined errors
		}
		
		// Add ShutdownHook to make sure everything terminates correctly
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Main.TerminateApp();
			}
		});
		
	}
	private static boolean ProcessArgs(String[] args) {
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
			case "-noplugins":
				argPluginsEnabled = false;
				if (logger != null) {
					logger.logInfo("ProcessArgs: \"-noplugins\": Plugins will be disabled.");
				}
				break;
			case "-nolinks":
				argLinksEnabled = false;
				if (logger != null) {
					logger.logInfo("ProcessArgs: \"-nolinks\": Links will be disabled.");
				}
				break;
			case "-help":
			default:
				System.out.println("Arguments: [-debug][-conlog | -filelog][-noplugins][-nolinks]");
				System.out.println();
				System.out.println("    -debug:     Enables debugging information and logs.");
				System.out.println("    -conlog:    Writes all logs to the console.");
				System.out.println("    -filelog:   Writes all logs to a log file.");
				System.out.println("    -help:      Prints this information.");
				System.out.println("    -nolinks:   Disables link loading, from this application.");
				System.out.println("    -noplugins: Disables plugin loading, from this application.");
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
	private static void TerminateApp() {
		instance.terminate();
		logger.endLog();
	}
	
	// ####################################################
	// # Main Class #
	// ####################################################
	TrayIcon trayIcon;
	
	private Main() {
		this.trayIcon = null;
	}
	
	private boolean init() {
		if (!SystemTray.isSupported()) {
			logger.logError("Main: The current system does not support a system tray.");
			return false;
		}
		
		try {
			Image icon = ImageIO.read((ClassLoader.getSystemClassLoader().getResource("icon.png")));
			this.trayIcon = new TrayIcon(icon);
			SystemTray.getSystemTray().add(trayIcon);
			
			logger.logInfo("Main.init: Added the TrayIcon to the SystemTray.");
			return true;
		} catch (Exception e) {
			logger.logError("Main.init: Something failed during the initialization!", e);
			return false;
		}
	}
	private void terminate() {
		SystemTray.getSystemTray().remove(trayIcon);
		logger.logInfo("Main.terminate: Removed the TrayIcon from the SystemTray");
	}
	
}
