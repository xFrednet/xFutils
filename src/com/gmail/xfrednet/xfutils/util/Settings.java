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
	
	public Settings() {
		reset();
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
	
	// TODO UI implementation
	
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
 