package com.gmail.xfrednet.xfutils.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.swing.*;

import com.gmail.xfrednet.xfutils.Main;

public class Settings {
	
	public static void SaveResettedSettingsToFile() {
		Settings settings = new Settings();
		settings.reset();
		if (settings.save()) {
			Main.Logger.logInfo("Settings.SaveResettedSettingsToFile: The settings resetted settings are saved");
		}
	}
	
	public static final String SETTINGS_FILE = "settings.txt";
	public static final String LOADING_ERROR_STRING = "[LOADING ERROR]";
	
	private boolean showTrayMenuLabels;
	private String language;
	
	private JMenu guiSettingsMenu;
	private JCheckBoxMenuItem guiShowTrayMenuLabelsItem;
	private JMenuItem guiResetItem;
	// TODO add scale option
	
	public Settings() {
		reset();
		
		this.guiSettingsMenu = null;
		this.guiShowTrayMenuLabelsItem = null;
		this.guiResetItem = null;
	}
	
	public void reset() {
		this.showTrayMenuLabels = true;
		this.language = "en";
	}
	
	public boolean save() {
		Properties saveProperties = new Properties();
		
		// Store data to properties
		SaveBool(saveProperties, "showTrayMenuLabels", showTrayMenuLabels);
		SaveString(saveProperties, "language", language);
		
		// Write the properties to file
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(SETTINGS_FILE), StandardCharsets.UTF_8);
			
			saveProperties.store(writer, "The magical awesome settings of xFutils");
			
			// Finish writing
			writer.close();
			Main.Logger.logInfo("Settings.save: The settings were saved successfully");
			return true;
		} catch (IOException e) {
			// Log if an error was thrown
			Main.Logger.logAlert("Settings.save: has failed to load the settings.", e);
			return false;
		}
	}
	public boolean load() {
		// Check if the file ist alive
		File settingsFile = new File(SETTINGS_FILE);
		if (!settingsFile.exists()) {
			Main.Logger.logInfo(
					"Settings.load: The settings file could not be found. File name: " +
					SETTINGS_FILE);
			return false;
		}
		
		// create properties
		Properties loadProperties = new Properties();
		
		// Try to read the settings file
		try {
			Reader reader = new InputStreamReader(new FileInputStream(settingsFile));
			
			loadProperties.load(reader);
			
			reader.close();
		} catch (IOException e) {
			Main.Logger.logAlert(
					"Settings.load: An exception was thrown during the loading process.", 
					e);
			return false;
		}
		
		this.showTrayMenuLabels = LoadBool(loadProperties, "showTrayMenuLabels");
		this.language = LoadString(loadProperties, "language");
		
		if (Main.Logger.isDebugLogEnabled()) {
			Main.Logger.logDebugMessage("Settings.load: The following settings were loaded:");
			Main.Logger.logProperties(loadProperties);
		}
		
		Main.Logger.logInfo("Settings.load: The settings were loaded successfully :)");
		return true;
	}
	
	public JMenu getSettingsMenu(Main main) {
		Language translation = main.getLanguage();
		this.guiSettingsMenu = new JMenu(translation.getString(Language.Keys.MENU_ITEM_SETTINGS));
		
		// showTrayMenuLabels
		this.guiShowTrayMenuLabelsItem = new JCheckBoxMenuItem(translation.getString(Language.Keys.SETTINGS_SHOW_TRAYMENU_LABELS));
		this.guiShowTrayMenuLabelsItem.setState(this.showTrayMenuLabels);
		this.guiShowTrayMenuLabelsItem.addActionListener(l -> {
			// Assign new value and save the change
			this.showTrayMenuLabels = this.guiShowTrayMenuLabelsItem.getState();
			save();
		});
		this.guiSettingsMenu.add(this.guiShowTrayMenuLabelsItem);

		// language
		this.guiSettingsMenu.add(translation.createSettingsMenu(this, main));

		// reset
		this.guiResetItem = new JMenuItem(translation.getString(Language.Keys.SETTINGS_RESET));
		this.guiResetItem.addActionListener(l -> {
			// reset and save
			reset();
			save();
		});
		this.guiSettingsMenu.add(this.guiResetItem);
		
		// return
		return this.guiSettingsMenu;
	}
	public void updateGUI(Language translation) {
		if (this.guiSettingsMenu != null) {
			this.guiSettingsMenu.setText(translation.getString(Language.Keys.MENU_ITEM_SETTINGS));
			translation.updateGUI();
			this.guiResetItem.setText(translation.getString(Language.Keys.SETTINGS_RESET));
		}
	}
	
	public void setLanguage(String langName, Main main) {
		this.language = langName;
		main.updateLanguage();
	}
	
	public boolean AreTrayMenuLabelsShown() {
		return this.showTrayMenuLabels;
	}
	public String getLanguage() {
		return this.language;
	}
	
	private static void SaveString(Properties prop, String key, String value) {
		prop.setProperty(key, value);
	}
	private static String LoadString(Properties prop, String key) {
		String value = prop.getProperty(key, LOADING_ERROR_STRING);
		
		if (value.equals(LOADING_ERROR_STRING)) {
			Main.Logger.logInfo("Settings.LoadString: Unable to load the value of the key '" + key + "'");
		}
		
		return value;
	}
	
	private static void SaveBool(Properties prop, String key, boolean value) {
		SaveString(prop, key, ((value) ? "true" : "false"));
	}
	private static boolean LoadBool(Properties prop, String key) {
		String value = LoadString(prop, key);
		
		if (value.equals("true")) {
			return true;
		} else if (value.equals("false")) {
			return false;
		}
		
		Main.Logger.logInfo(
				"Settings.LoadBool: Unable to load the boolean value for the key " + 
				"'" + key + "', the String value is: '" + value + "'");
		return false;
	}
}
 