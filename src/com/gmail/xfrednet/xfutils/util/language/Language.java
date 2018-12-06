package com.gmail.xfrednet.xfutils.util.language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import com.gmail.xfrednet.xfutils.Main;
import com.gmail.xfrednet.xfutils.util.Settings;

import javax.swing.*;

public class Language {

	private static final String RESOURCE_BUNDLE_BASE_NAME = "translations\\lang";
	private static final String AVAILABLE_LANGUAGES = "translations\\available_languages.txt";
	
	public static Language Init(String langName) {
		Language lang = new Language();
		
		if (lang.loadResource(langName)) {
			// Return the object if init() was successful 
			return lang;
		} else {
			// Try to load the English property file before giving up
			if (langName != "en") {	
				Main.Logger.logAlert(
						"Language.Init: was unable to load the .property file for the language: " +
						 langName);
				Main.Logger.logAlert("    -> Language.Init will try to load the English property file");
				
				// To understand recursion you have to understand recursion
				return Init("en");
			} else {
				Main.Logger.logError("Language.Init: Was unable to load a .property language file, see previous alert!!!");
				return null;
			}
		}
	}
	
	/**
	 * This is the {@linkplain Locale} object that holds localized information
	 * here it is only used to store the language name
	 * */
	private Locale localeInfo = null;
	
	/**
	 * These {@linkplain Properties} are used as a map that contain all translations.
	 * */
	private Properties translation = null;

	/**
	 * This {@linkplain List} contains all bundles that are loaded. These paths
	 * will be used when {@linkplain #changeLanguage(String)} get's called.
	 * */
	private List<String> resourceBundlePaths = null;
	
	/**
	 * This {@linkplain List} contains all {@linkplain ILanguageListener}s that will
	 * be notified when the language changes.
	 * */
	private List<ILanguageListener> changeListeners = null;
	
	private JMenu guiSettingsMenu = null;
	
	/**
	 * This initializes some values. Call {@linkplain #loadLanguage(String)}
	 * to actually load the class
	 * */
	private Language() {
		this.resourceBundlePaths = new ArrayList<>();
		this.resourceBundlePaths.add(RESOURCE_BUNDLE_BASE_NAME);
		
		this.changeListeners = new ArrayList<>();
	}
	
	/**
	 * This method changes the language. It is also used to initialize the language.
	 * 
	 * This will reload the entire translations, to add a resource bundle
	 * please call {@linkplain #addResource(String)}.
	 * 
	 * @param langAbb The abbreviation of the new language.
	 * */
	public void changeLanguage(String language) {
		this.localeInfo = new Locale(language);
		
		
	}
	/**
	 * This tries to add the language specific {@linkplain ResourceBundle}
	 * from the given bundle path.
	 * 
	 * <p>This method will try to handle loading issues by loading the
	 * English resource if the language loading failed. A info will be written
	 * to the console. In case of this failing as well, it will log an Error
	 * to the logger and give up.</p>
	 * 
	 * @param bundlePath The path of the bundle that should be loaded.
	 * @param locale The {@linkplain Locale} that should be used to load the right resource.
	 * */
	public boolean addResource(String bundlePath) {
		return addResource(bundlePath, this.localeInfo);
	}
	private boolean loadResource(String bundlePath, Locale locale) {
		try	{
			ResourceBundle resource = ResourceBundle.getBundle(
					RESOURCE_BUNDLE_BASE_NAME, 
					locale);
			
			resource.keySet().stream().forEach(key -> this.translation.put(key, resource.getString(key)));
			
			Main.Logger.logInfo("Language.init: The language \"" + getString(Keys.LANG_NAME) + "\" Successfully :)");
			return true;
		} catch (Exception e) {
			// check id the local is a english local
			Locale engLocale = new Locale("en");
			if (!locale.getLanguage().equals(engLocale.getLanguage())) {
				// try to load the English local if the given local doesn't have a resource
				return loadResource(bundlePath, locale);
			} else {
				// Well we've failed and with "we" I mean YOU!!!
				Main.Logger.logAlert(
						"Language.init: Loading the ResourceBoundle(" +
								this.languageAbbreviation + ") has failed!", e);
			}
			
			return false;
		}
	}
	
	public String getString(String key) {
		try {
			return this.resource.getString(key);			
		} catch (Exception e) {
			Main.Logger.logAlert("Language.getString: Unable to get the translation for the key: " + key, e);
			return "[Missing Translation]";
		}
	}
	
	public String getLanguage() {
		return this.localeInfo.getLanguage();
	}

	
	public JMenu createSettingsMenu(Settings settings, Main main) {
		this.guiSettingsMenu = new JMenu(getString(Keys.SETTINGS_LANGUAGE_MENU));
		
		// Load the available languages from file. Note, that a language has to be
		// added to the "available_languages.txt" in order to be loaded to the settings
		Properties languages = new Properties();
		try {
			languages.load(ClassLoader.getSystemResourceAsStream(AVAILABLE_LANGUAGES));
		} catch (IOException e) {
			Main.Logger.logAlert("Language.createSettingsMenu: Unable to open the available language file.");
			this.guiSettingsMenu.setText(getString("[Error: IOException]"));
			return this.guiSettingsMenu;
		}
		
		// the languages are loaded now :)
		ButtonGroup langStation = new ButtonGroup();
		Set<String> langNames = languages.stringPropertyNames();
		for (String langKey : langNames) {
			JRadioButtonMenuItem langItem = new JRadioButtonMenuItem(langKey);
			
			String langAbbreviation = languages.getProperty(langKey);
			if (langAbbreviation.equals(this.languageAbbreviation)) {
				langItem.setSelected(true);
			}
			
			// This action listener is only activated when the MenuItem gets selected
			langItem.addActionListener(l -> {
				settings.setLanguage(langAbbreviation, main);
				settings.save();
			});
			
			langStation.add(langItem);
			this.guiSettingsMenu.add(langItem);
		}
		
		return this.guiSettingsMenu;
	}
	public void updateGUI() {
		if (this.guiSettingsMenu != null) {
			this.guiSettingsMenu.setText(getString(Keys.SETTINGS_LANGUAGE_MENU));
		}
	}

	
	public class Keys {
		public static final String LANG_NAME = "lang_name";
		
		public static final String MENU_LABEL_PLUGINS = "menu_label_plugins";
		public static final String MENU_LABEL_LINKS = "menu_label_links";
		public static final String MENU_LABEL_META = "menu_label_meta";
		
		public static final String MENU_ITEM_EMPTY = "menu_item_empty";
		public static final String MENU_ITEM_ADD_LINK = "menu_item_add_link";
		public static final String MENU_ITEM_EXIT = "menu_item_exit";

		public static final String MENU_ITEM_SETTINGS = "menu_item_settings";
		public static final String SETTINGS_LANGUAGE_MENU = "settings_language_menu";
		public static final String SETTINGS_SHOW_TRAYMENU_LABELS = "settings_show_traymenu_labels";
		public static final String SETTINGS_RESET = "settings_reset_settings";
	}
}
