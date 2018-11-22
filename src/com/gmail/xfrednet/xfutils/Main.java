package com.gmail.xfrednet.xfutils;

import com.gmail.xfrednet.xfutils.util.logger.ConsoleLogger;
import com.gmail.xfrednet.xfutils.util.logger.FileLogger;
import com.gmail.xfrednet.xfutils.util.logger.NoLogLogger;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import com.gmail.xfrednet.xfutils.plugin.PluginManager;
import com.gmail.xfrednet.xfutils.util.Language;
import com.gmail.xfrednet.xfutils.util.Logger;
import com.gmail.xfrednet.xfutils.util.Settings;

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
		
		File link = new File("link.lnk");
		boolean ex = link.exists();
		boolean canEx = link.canExecute();
		
		
		// Add ShutdownHook to make sure everything terminates correctly
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Main.TerminateApp();
			}
		});
		
		TerminateApp();
	}
	private static boolean ProcessArgs(String[] args) {
		// This instance prevents the application form running
		// into a null pointer
		Logger = new NoLogLogger();
		
		for (String arg : args) {
			switch (arg) {
			case "-debug":
				IsDebugEnabled = true;
				break;
			case "-conlog":
				if (!(Logger instanceof NoLogLogger)) {
					Logger.logError("ProcessArgs: \"-conlog\": Only one log option can be selected at a time.");
					break;
				}
				Logger = new ConsoleLogger(IsDebugEnabled);
				Logger.logInfo("ProcessArgs: The log will be written to the console.");
				break;
			case "-filelog":
				if (!(Logger instanceof NoLogLogger)) {
					Logger.logError("ProcessArgs: \"-filelog\": Only one log option can be selected at a time.");
					break;
				}
				Logger = new FileLogger(IsDebugEnabled);
				Logger.logInfo("ProcessArgs: The log will be written to a file.");
				break;
			case "-noplugins":
				ArePluginsEnabled = false;
				Logger.logInfo("ProcessArgs: \"-noplugins\": Plugins will be disabled.");
				break;
			case "-nolinks":
				AreLinksEnabled = false;
				Logger.logInfo("ProcessArgs: \"-nolinks\": Links will be disabled.");
				break;
			case "-resetsettings":
				Settings.SaveResettedSettingsToFile();
				break;
			case "-help":
			default:
				System.out.println("Arguments: [-debug][-conlog | -filelog][-noplugins][-nolinks][-resetsettings]");
				System.out.println();
				System.out.println("    -debug:         Enables debugging information and logs.");
				System.out.println("    -conlog:        Writes all logs to the console.");
				System.out.println("    -filelog:       Writes all logs to a log file.");
				System.out.println("    -help:          Prints this information.");
				System.out.println("    -nolinks:       Disables link loading, from this application.");
				System.out.println("    -noplugins:     Disables plugin loading, from this application.");
				System.out.println("    -resetsettings: Resets the current settings to their defauls and saves them.");
				return false;
			}
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
	
	private Settings settings;
	private Language language;
	
	private TrayIcon trayIcon;
	private PopupMenu trayMenu;

	// The PluginManager will only be valid if Main.argPluginsEnabled
	// is true. So please make sure to check for null before usage.
	private PluginManager pluginManager;
	
	private Main() {
		this.settings = null;
		this.language = null;
		
		this.trayIcon = null;
		this.trayMenu = null;
		
		this.pluginManager = null;
	}
	
	// ##########################################
	// # init
	// ##########################################
	private boolean init() {
		// Settings
		this.settings = new Settings();
		if (!this.settings.load()) {
			this.settings.reset();
			if (this.settings.save()) {
				Logger.logAlert("Main.init: Unable to save the settings after creating new ones!");
			}
		}
		
		// Language
		this.language = Language.Init(this.settings.getLanguage());
		
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
		
		// Return le tr�e
		return true;
	}
	private void initTrayMenu() {
		this.trayMenu = new PopupMenu();
		
		this.trayMenu.addSeparator(); // Plugins section
		this.trayMenu.addSeparator(); // Links section
		
		if (this.settings.AreTrayMenuLabelsShown()) {			
			MenuItem pluginsLabel = new MenuItem(this.language.getString(Language.Keys.MENU_LABEL_PLUGINS));
			pluginsLabel.setEnabled(false); // make it a label
			addMenuItem(pluginsLabel, MENU_SECTION_PLUGINS);
			
			MenuItem linksLabel = new MenuItem(this.language.getString(Language.Keys.MENU_LABEL_LINKS));
			linksLabel.setEnabled(false); // make it a label
			addMenuItem(linksLabel, MENU_SECTION_LINKS);
			
			MenuItem metaLabel = new MenuItem(this.language.getString(Language.Keys.MENU_LABEL_META));
			metaLabel.setEnabled(false); // make it a label
			addMenuItem(metaLabel, MENU_SECTION_META);
		}
		
		// "Exit"-item
		MenuItem exitItem = new MenuItem(this.language.getString(Language.Keys.MENU_ITEM_EXIT));
		exitItem.addActionListener(e -> {
			Logger.logDebugMessage("Menu.Exit-Item: I was activated!");
			System.exit(0);
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
		Logger.logDebugMessage("The TrayIcon has a PopupMenu now. (Try it now for free: just 1�)");
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
				return index; // Return the index if the right section has ended
			}
		}
		
		return itemCount;
	}
}
