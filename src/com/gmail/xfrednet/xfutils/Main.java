package com.gmail.xfrednet.xfutils;

import com.gmail.xfrednet.xfutils.link.LinkManager;
import com.gmail.xfrednet.xfutils.plugin.PluginManager;
import com.gmail.xfrednet.xfutils.util.Language;
import com.gmail.xfrednet.xfutils.util.Logger;
import com.gmail.xfrednet.xfutils.util.Settings;
import com.gmail.xfrednet.xfutils.util.logger.ConsoleLogger;
import com.gmail.xfrednet.xfutils.util.logger.FileLogger;
import com.gmail.xfrednet.xfutils.util.logger.NoLogLogger;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;


public class Main {

	// Static resources
	public static final Image MAIN_ICON = LoadResourceImage("icon.png");

	// Static Values
	public static Logger  Logger             = new NoLogLogger();
	public static boolean IsDebugEnabled     = false;
	public static boolean ArePluginsEnabled  = true;
	public static boolean AreLinksEnabled    = true;

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

	private static Image LoadResourceImage(String iconName) {
		// Ask the ClassLoader for the resource URL
		URL resURL = ClassLoader.getSystemClassLoader().getResource(iconName);
		if (resURL == null) {
			Logger.logError("Main. LoadResourceIcon: The ClassLoader was unable to find the icon as a resource: " + iconName);
			return null;
		}

		// Try to load the icon
		try {
			return ImageIO.read(resURL);
		} catch (IOException e) {
			e.printStackTrace();
			Logger.logError("Main. LoadResourceIcon: Unable to load the icon from resource: " + iconName, e);
			return null;
		}
	}

	// ####################################################
	// # Main Class #
	// ####################################################
	private static final int MENU_SECTION_PLUGINS = 0;
	private static final int MENU_SECTION_LINKS   = 1;
	private static final int MENU_SECTION_META    = 2;
	
	private Settings settings;
	private Language language;
	
	private TrayIcon trayIcon;
	private JPopupMenu trayMenu;
	private int[] trayMenuSectionEnd;

	// The PluginManager will only be valid if Main.argPluginsEnabled
	// is true. So please make sure to check for null before usage.
	private PluginManager pluginManager;
	private LinkManager linkManager;
	
	private Main() {
		this.settings = null;
		this.language = null;
		
		this.trayIcon = null;
		this.trayMenu = null;
		this.trayMenuSectionEnd = new int[] {0, 1, 2};
		
		this.pluginManager = null;
		this.linkManager = null;
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
			this.trayIcon = new TrayIcon(MAIN_ICON);
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

		// init the LinkManager
		initLinkManager();
		
		// Return le trùe
		return true;
	}
	private void initTrayMenu() {
		this.trayMenu = new JPopupMenu();
		this.trayIcon.addActionListener(l -> {
			Main.Logger.logAlert(l.toString());
		});
		
		this.trayMenu.addSeparator(); // Plugins section
		this.trayMenu.addSeparator(); // Links section
		
		if (this.settings.AreTrayMenuLabelsShown()) {
			// TODO make labels nonlocal and update labels on language change
			JMenuItem pluginsLabel = new JMenuItem(this.language.getString(Language.Keys.MENU_LABEL_PLUGINS));
			pluginsLabel.setEnabled(false); // make it a label
			addMenuItem(pluginsLabel, MENU_SECTION_PLUGINS);
			
			JMenuItem linksLabel = new JMenuItem(this.language.getString(Language.Keys.MENU_LABEL_LINKS));
			linksLabel.setEnabled(false); // make it a label
			addMenuItem(linksLabel, MENU_SECTION_LINKS);
			
			JMenuItem metaLabel = new JMenuItem(this.language.getString(Language.Keys.MENU_LABEL_META));
			metaLabel.setEnabled(false); // make it a label
			addMenuItem(metaLabel, MENU_SECTION_META);
		}
		
		// Settings menu
		addMenuItem(this.settings.getSettingsMenu(this), MENU_SECTION_META);
		
		// "Exit"-item
		JMenuItem exitItem = new JMenuItem(this.language.getString(Language.Keys.MENU_ITEM_EXIT));
		exitItem.addActionListener(e -> {
			Logger.logDebugMessage("Menu.Exit-Item: I was activated!");
			System.exit(0);
		});
		addMenuItem(exitItem, MENU_SECTION_META);
		
		// Add trayMenu to trayIcon
		this.trayIcon.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					Main.this.showTrayMenu(e.getX(), e.getY());
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			
		});
		
		Logger.logDebugMessage("The TrayIcon has a PopupMenu now. (Try it now for free: just 1�)");
	}

	private void initPluginManager() {
		if (!ArePluginsEnabled)
			return;
		
		this.pluginManager = new PluginManager(Logger);
		
		this.pluginManager.initPlugins();
		List<JMenuItem> pluginItems = this.pluginManager.getPluginMenuElements();
		for (JMenuItem menuItem : pluginItems) {
			addMenuItem(menuItem, MENU_SECTION_PLUGINS);
		}

		Main.Logger.logInfo("Main.initPluginManager: The PluginManager was successfully initialized");
	}
	private void initLinkManager() {
		if (!AreLinksEnabled)
			return;

		// create and init the LinkManager
		this.linkManager = new LinkManager();
		if (!this.linkManager.init(this.language)) {
			Main.Logger.logError("Main.initLinkManager: Something went wrong during the initialisation of the LinkManager");
			this.linkManager = null;
			return;
		}

		// Add the JMenuItems from the LinkManager
		JMenuItem[] linkItems = this.linkManager.getMenuItems();
		for (JMenuItem item : linkItems) {
			addMenuItem(item, MENU_SECTION_LINKS);
		}

		Main.Logger.logInfo("Main.initLinkManager: The LinkManager was successfully initialized");
	}
	
	// ##########################################
	// # TrayMenu stuff and things
	// ##########################################
	
	private void showTrayMenu(int x, int y) {
		// So the following, the TrayIcon does not work well with
		// a JPopupMenu, and with not well I mean not at all. The main problem
		// is that menu wouldn't close it self.
		// So what did I do? well I create a JDialog without any decoration, that has a
		// alpha value of 0. The JPopupMenu, now behaves like a good boy, the only problem
		// is disposing the dialog after the JPopupMenu is done. The easy fix? Add a
		// PopupMenuListener that disposes the dialog when the menu goes invisible
		
		// Create the dialog
		JDialog dialog = new JDialog();
		dialog.setFocusable(true);
		dialog.setBounds(x, y, 10, 10);
		dialog.setUndecorated(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		dialog.setOpacity(0.0f);
		
		// Show the menu
		// The x and y values for the tray menu are now dialog relative
		this.trayMenu.show(dialog, 0, 0);
		this.trayMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				Main.Logger.logDebugMessage("trayMenu.PopupMenuListener: The menu will become invisible, the dialog will be disposed");
				dialog.dispose();
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {}
		});
		Main.Logger.logInfo("showTrayMenu: The trayMenu should be visible now!");

		// So, am I proud of this code, well I'm proud I found a well 
		// working solution for my problem
	}
	// This method adds the @JMenuItem at the end of the section.
	// A new section starts with a separator.
	private void addMenuItem(JMenuItem item, int sectionNo) {
		// Validation
		if (sectionNo < 0 || sectionNo >= this.trayMenuSectionEnd.length) {
			sectionNo = ((sectionNo < 0) ? 
					0 : 
					this.trayMenuSectionEnd.length - 1);
		}
		
		// Add the item
		this.trayMenu.insert(item, this.trayMenuSectionEnd[sectionNo]);
		
		// Count this and all following indices up
		for (int index = sectionNo; index < this.trayMenuSectionEnd.length; index++) {
			this.trayMenuSectionEnd[index]++;
		}
	}
	
	// ##########################################
	// # terminate
	// ##########################################
	private void terminate() {
		if (this.trayMenu.isVisible()) {
			this.trayMenu.setVisible(false);
		}
		this.trayMenu = null;
		
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
	// # Utility
	// ##########################################
	public Language getLanguage() {
		return this.language;
	}
	public void updateLanguage() {
		String oldLang = this.language.getLanguage();
		
		// Check if the new language could be loaded
		if (!this.language.loadResource(this.settings.getLanguage())) {
			this.language.loadResource(oldLang);
			return;
		}
		
		this.settings.updateGUI(this.language);
	}
}
