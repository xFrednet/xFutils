package com.gmail.xfrednet.xfutils;

import com.gmail.xfrednet.xfutils.util.logger.ConsoleLogger;
import com.gmail.xfrednet.xfutils.util.logger.FileLogger;
import com.gmail.xfrednet.xfutils.util.logger.NoLogLogger;

import java.awt.Image;
import java.awt.MenuContainer;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.MenuElement;

import com.gmail.xfrednet.xfutils.plugin.PluginManager;
import com.gmail.xfrednet.xfutils.util.Logger;

public class Main {
	
	public static Logger  Logger             = null;
	public static boolean IsDebugEnabled     = false;
	public static boolean ArePluginsEnabled  = false;
	public static boolean AreLinksEnabled    = false;
	
	// This value should only be used by the TerminateApp function
	private static Main instance             = null;
	
	// ##########################################
	// # main #
	// ##########################################
	public static void main(String[] args) {
		if (!ProcessArgs(args)) {
			Logger.endLog();
			return; // ProcessArgs has failed
		}
		
		instance = new Main();
		if (!instance.init()) {
			Logger.logError("main: Something failed durring the initialize of Main. Goodbye");
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
				IsDebugEnabled = true;
				break;
			case "-conlog":
				if (Logger != null) {
					Logger.logError("ProcessArgs: \"-conlog\": Only one log option can be selected at a time.");
				}
				Logger = new ConsoleLogger(IsDebugEnabled);
				Logger.logInfo("ProcessArgs: The log will be written to the console.");
				break;
			case "-filelog":
				if (Logger != null) {
					Logger.logError("ProcessArgs: \"-filelog\": Only one log option can be selected at a time.");
				}
				Logger = new FileLogger(IsDebugEnabled);
				Logger.logInfo("ProcessArgs: The log will be written to a file.");
				break;
			case "-noplugins":
				ArePluginsEnabled = false;
				if (Logger != null) {
					Logger.logInfo("ProcessArgs: \"-noplugins\": Plugins will be disabled.");
				}
				break;
			case "-nolinks":
				AreLinksEnabled = false;
				if (Logger != null) {
					Logger.logInfo("ProcessArgs: \"-nolinks\": Links will be disabled.");
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
		if (Logger == null) {
			Logger = new NoLogLogger();
		}

		// debug info
		if (IsDebugEnabled) {
			Logger.logDebugMessage("ProcessArgs: Debugging was enabled!");

			StringBuilder sb = new StringBuilder();
			for (String arg : args) {
				sb.append(arg);
				sb.append(" ");
			}

			Logger.logDebugMessage("ProcessArgs: The following arguments where entered: \"" + sb.toString() + "\"");
		}
		return true;
	}
	private static void TerminateApp() {
		if (instance != null) {
			instance.terminate();
			instance = null;
		}
		if (Logger != null) {
			Logger.endLog();
			Logger = null;
		}
	}
	
	// ####################################################
	// # Main Class #
	// ####################################################
	private static final int MENU_SECTION_PLUGINS = 1;
	private static final int MENU_SECTION_LINKS  = 2;
	private static final int MENU_SECTION_META   = 3;
	
	private TrayIcon trayIcon;
	private PopupMenu trayMenu;

	// The PluginManager will only be valid if Main.argPluginsEnabled
	// is true. So please make sure to check for null before usage.
	private PluginManager pluginManager;
	
	private Main() {
		this.trayIcon = null;
		this.trayMenu = null;
		
		this.pluginManager = null;
	}
	
	// ##########################################
	// # init
	// ##########################################
	private boolean init() {
		// Test if the TrayIcon is support
		if (!SystemTray.isSupported()) {
			Logger.logError("Main: The current system does not support a system tray.");
			return false;
		}
		
		// Create and add TrayIcon
		try {
			Image icon = ImageIO.read(ClassLoader.getSystemClassLoader().getResource("icon.png"));
			this.trayIcon = new TrayIcon(icon);
			SystemTray.getSystemTray().add(trayIcon);
			
			Logger.logInfo("Main.init: Added the TrayIcon to the SystemTray.");
		} catch (Exception e) {
			Logger.logError("Main.init: Something failed during the initialization!", e);
			return false;
		}
		
		// Menu init
		initTrayMenu();
		
		// init Plugins
		initPluginManager();
		
		// Return le trúe
		return true;
	}
	private void initTrayMenu() {
		this.trayMenu = new PopupMenu();
		
		// TODO add settings: showLabels
		// Plugins section
		MenuItem pluginsLabel = new MenuItem("Plugins");
		pluginsLabel.setEnabled(false); // make it a label
		addMenuItem(pluginsLabel, MENU_SECTION_PLUGINS);
		this.trayMenu.addSeparator();
		
		// Links section
		MenuItem linksLabel = new MenuItem("Links");
		linksLabel.setEnabled(false); // make it a label
		addMenuItem(linksLabel, MENU_SECTION_LINKS);
		this.trayMenu.addSeparator();
		
		// Meta section
		MenuItem metaLabel = new MenuItem("Meta");
		metaLabel.setEnabled(false); // make it a label
		addMenuItem(metaLabel, MENU_SECTION_META);
		
		// "Exit"-item
		MenuItem exitItem = new MenuItem("Exit"); // TODO create language class
		exitItem.addActionListener(e -> {
			Logger.logDebugMessage("'Exit'-Item: I was activated!");
			System.exit(0);
			Logger.logDebugMessage("'Exit'-Item: heyyyyy");
		});
		addMenuItem(exitItem, MENU_SECTION_META);
		
		addMenuItem(new MenuItem("S2.0"), 2);
		addMenuItem(new MenuItem("S3.0"), 3);
		addMenuItem(new MenuItem("S3.1"), 3);
		addMenuItem(new MenuItem("S3.2"), 3);
		addMenuItem(new MenuItem("S2.1"), 2);
		addMenuItem(new MenuItem("S2.2"), 2);
		addMenuItem(new MenuItem("S2.3"), 2);
		addMenuItem(new MenuItem("S2.4"), 2);
		addMenuItem(new MenuItem("S3.3"), 3);
		
		// Add trayMenu to trayIcon
		this.trayIcon.setPopupMenu(this.trayMenu);
		Logger.logDebugMessage("The TrayIcon has a PopupMenu now. (Try it now for free: just 1€)");
	}
	private void initPluginManager() {
		if (!ArePluginsEnabled)
			return;
		
		this.pluginManager = new PluginManager(Logger);
		
		this.pluginManager.initPlugins();
		List<MenuItem> pluginItems = this.pluginManager.getPluginMenuElements();
		for (MenuItem menuItem : pluginItems) {
			addMenuItem(menuItem, MENU_SECTION_PLUGINS);
		}
	}
	// TODO initLinkManager
	
	// ##########################################
	// # terminate
	// ##########################################
	private void terminate() {
		if (this.pluginManager != null) {
			this.pluginManager.terminatePlugins();
			this.pluginManager = null;
			Logger.logInfo("Main.terminate: Terminated the PluginManager instance.");
		}
		
		this.trayIcon = null;
		// The TrayIcon will removed automatically by the SystenmTray.
		// Calling the remove function from the ShutdownHook causes the 
		// Application to idle until the end of dawn.
	}

	// ##########################################
	// # Add MenuItems
	// ##########################################
	// This method adds the @MenuItem at the end of the section
	// A new section starts with a separator
	private void addMenuItem(MenuItem item, int sectionNo) {
		int sectionEnd = getSeparatorIndex(sectionNo);
		this.trayMenu.insert(item, sectionEnd);
	}
	private int getSeparatorIndex(int separatorNo) {
		// Loop through the items
		int itemCount = this.trayMenu.getItemCount();
		int separatorCount = 0;
		for (int index = 0; index < itemCount; index++) {
			// Check if the menuItem is a Separator (The label is "-" for separators)
			MenuItem item = this.trayMenu.getItem(index);
			if (!item.getLabel().equals("-")) {
				continue;
			}
			
			separatorCount++;
			if (separatorCount == separatorNo) {
				return index; // Return the index if the items are the same
			}
		}
		
		return itemCount;
	}
}
