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
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * This is the main class of this entire project it manages all subsystems
 * of this application. it also includes the {@link #main(String[]) <tt>main</tt>}
 * method.
 * */
public class Main {

	/**
	 * This is the main icon of xFutils, I know it's the most beautiful
	 * thing you have ever seen. It was designed by me the one and truly only
	 * xFrednet. Praise me in your love and devotion for this icon.
	 * 
	 * <p>You can use <tt>new ImageIcon(MAIN_ICON);</tt> to create a 
	 * {@link java.awt.Icon <tt>Icon</tt>} from this image</p>
	 * */
	public static final Image MAIN_ICON = LoadResourceImage("icon.png");

	// Static Values
	/**
	 * The current instance of the {@link com.gmail.xfrednet.xfutils.util.Logger <tt>Logger</tt>}
	 * class. It is used for logging all events. The logger that is used to initialize this 
	 * value can be defined by arguments in {@linkplain #ProcessArgs(String[])}.
	 * */
	public static Logger  Logger             = new NoLogLogger();
	/**
	 * This value indicates if debug was enabled for this application.
	 * Debugging can be enabled with the <tt>-debug</tt> argument and is 
	 * set by {@linkplain #ProcessArgs(String[])}
	 * */
	public static boolean IsDebugEnabled     = false;
	/**
	 * This value indicates if plugins should be enabled for this application.
	 * Plugins can be disabled with the <tt>-noplugins</tt> argument and is 
	 * set by {@linkplain #ProcessArgs(String[])}
	 * */
	public static boolean ArePluginsEnabled  = true;
	/**
	 * This value indicates if links should be enabled for this application.
	 * Links can be disabled with the <tt>-nolinks</tt> argument and is 
	 * set by {@linkplain #ProcessArgs(String[])}
	 * */
	public static boolean AreLinksEnabled    = true;

	/**
	 * This is the current instance of this class it is and should only
	 * be used by the {@linkplain #TerminateApp()} function.*/
	private static Main instance             = null;
	
	// ##########################################
	// # main #
	// ##########################################
	/**
	 * The main method and therefore the entry point of this project.
	 * The main method starts to process the arguments, if {@linkplain #ProcessArgs} returns
	 * true it will continue to initialize the instance of this class and add a shutdownhook
	 * at the end that calls the {@linkplain #TerminateApp()} function when the Application terminates
	 * 
	 * @param args:
	 *      Arguments that can be given by the user. The arguments are processed by {@linkplain #ProcessArgs}.
	 * */
	public static void main(String[] args) {
		if (!ProcessArgs(args)) {
			Logger.endLog();
			return; // ProcessArgs has failed
		}
		
		instance = new Main();
		if (!instance.init()) {
			Logger.logError("main: Something failed durring the initialize of Main. Goodbye");
			Logger.endLog();
			System.exit(1); // TODO create predefined errors
		}
		
		// Add ShutdownHook to make sure everything terminates correctly
		Runtime.getRuntime().addShutdownHook(new Thread(() -> Main.TerminateApp()));
	}
	/**
	 * ProcessArgs processes the arguments that
	 * are given to the {@linkplain #main(String[])} method. It should be called right at
	 * the start of the application because it can initialize values that effect the inner
	 * working of this Application.
	 * 
	 * @param args:
	 *        The arguments that should be processed, these should be the unmodified arguments
	 *        given to the {@linkplain #main(String[])} method.
	 *        
	 * @return It returns <tt>true</tt> if the application should continue to start.
	 *         The application should terminate in case of it returning <tt>false</tt>. 
	 * */
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
	/**
	 * This function calls the {@linkplain #terminate()} method of the {@linkplain Main} class.
	 * After {@linkplain Main} was terminated it will call the 
	 * {@link com.gmail.xfrednet.xfutils.util.Logger#endLog() <tt>endLog()</tt>} method of the 
	 * current {@link com.gmail.xfrednet.xfutils.util.Logger <tt>Logger</tt>} instance.
	 * 
	 * <p>This function is called by a ShutdownHook, it will be called automatically
	 * when the application is shutdown in any way. (The exception to the rule is 
	 * terminating it with a debugger)</p>
	 * */
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
	
	/** This Function loads a {@link java.awt.Image <tt>Image</tt>} with
	 * the given name from resource.
	 * 
	 * @param  imageName The name of the image file.
	 * 	
	 * @return The loaded {@link java.awt.Image <tt>Image</tt>} or <tt>null</tt>
	 *         if something failed during the loading process.
	 * */
	private static Image LoadResourceImage(String imageName) {
		// Ask the ClassLoader for the resource URL
		URL resURL = ClassLoader.getSystemClassLoader().getResource(imageName);
		if (resURL == null) {
			Logger.logError("Main. LoadResourceIcon: The ClassLoader was unable to find the icon as a resource: " + imageName);
			return null;
		}

		// Try to load the icon
		try {
			return ImageIO.read(resURL);
		} catch (IOException e) {
			e.printStackTrace();
			Logger.logError("Main. LoadResourceImage: Unable to load the image from resource: " + imageName, e);
			return null;
		}
	}

	// ####################################################
	// # Main Class #
	// ####################################################
	private static final int MENU_SECTION_PLUGINS = 0;
	private static final int MENU_SECTION_LINKS   = 1;
	private static final int MENU_SECTION_META    = 2;
	
	/**
	 * This is the current instance of the 
	 * {@link com.gmail.xfrednet.xfutils.util.Settings <tt>Settings</tt>} class. It
	 * is initialized by {@linkplain #init()}.
	 * */
	private Settings settings = null;
	/**
	 * This is the current instance of the 
	 * {@link com.gmail.xfrednet.xfutils.util.Language <tt>Language</tt>} class. It
	 * is initialized by {@linkplain #init()}.
	 * */
	private Language language = null;

	/**
	 * This is the icon of the {@link java.awt.TrayIcon <tt>TrayIcon</tt>} class.
	 * The TrayIcon is the java main way to interface with the system tray in windows
	 */
	private TrayIcon trayIcon = null;

	private JPopupMenu trayMenu;
	private int[] trayMenuSectionEnd;

	/**
	 * This is the current instance of the 
	 * {@link com.gmail.xfrednet.xfutils.plugin.PluginManager <tt>PluginManager</tt>} class. It
	 * is initialized by {@linkplain #initPluginManager()}.
	 * 
	 * <p>The {@link com.gmail.xfrednet.xfutils.plugin.PluginManager <tt>PluginManager</tt>} will
	 * only be valid if {@linkplain #ArePluginsEnabled} is <tt>true</tt>. Please check if
	 * this value is null before using it</p>
	 * */
	private PluginManager pluginManager = null;
	/**
	 * This is the current instance of the 
	 * {@link com.gmail.xfrednet.xfutils.link.LinkManager <tt>LinkManager</tt>} class. It
	 * is initialized by {@linkplain #initLinkManager()}.
	 * 
	 * <p>The {@link com.gmail.xfrednet.xfutils.link.LinkManager <tt>LinkManager</tt>} will
	 * only be valid if {@linkplain #AreLinksEnabled} is <tt>true</tt>. Please check if
	 * this value is null before using it</p>
	 * */
	private LinkManager linkManager = null;
	
	/**
	 * The constructor of the {@link com.gmail.xfrednet.xfutils.Main <tt>Main</tt>} class.
	 * It simply initializes most values with null. To initialize the Members for use call
	 * {@linkplain #init()}
	 * */
	private Main() {
		this.trayMenu = null;
		this.trayMenuSectionEnd = new int[] {0, 1, 2};
	}
	
	// ##########################################
	// # init
	// ##########################################
	/**
	 * This method initializes all members of this class. The initialized
	 * TrayIcon is also added to the SystemTray.
	 * 
	 * <p>This method will call all other init method within this class and
	 * its members, these should not be called separately again</p>
	 * 
	 * @return This method returns false if something really critical fails. The
	 *         error error that caused this will also be logged to the console.
	 * */
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
	/**
	 * This method initializes the Traymenu, it is called automatically
	 * by {@linkplain #init()}
	 * */
	private void initTrayMenu() {
		this.trayMenu = new JPopupMenu();

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

	/**
	 * This method initializes the PluginManager if plugins are enabled, 
	 * it is called automatically by {@linkplain #init()}
	 * */
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
	/**
	 * This method initializes the LinkManager if links are enabled, 
	 * it is called automatically by {@linkplain #init()}
	 * */
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
	/**
	 * This finishes all tasks and passes the terminate call to it's members.
	 * 
	 * <p>It is called by the {@linkplain #TerminateApp()} function and
	 * should not be called by any other sources</p>
	 * */
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
