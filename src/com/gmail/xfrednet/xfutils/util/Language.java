package com.gmail.xfrednet.xfutils.util;

import java.util.Locale;
import java.util.ResourceBundle;

import com.gmail.xfrednet.xfutils.Main;

import javax.swing.*;

public class Language {

	private static final String RESOURCE_BUNDLE_BASE_NAME = "translations\\lang";
	private static final String AVAILABLE_LANGUAGES = "translations\\available_languages.txt";
	
	public static Language Init(String langName) {
		Language lang = new Language(langName);
		
		if (lang.init()) {
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
	
	private String language;
	private Locale localeInfo;
	private ResourceBundle resource;
	
	private Language(String lang) {
		this.language = lang;
		this.localeInfo = null;
		this.resource = null;
	}
	
	private boolean init() {
		if (this.localeInfo != null) {
			return false;
		}
		
		this.localeInfo = new Locale(this.language);
		try	{
			this.resource = ResourceBundle.getBundle(
					RESOURCE_BUNDLE_BASE_NAME, 
					this.localeInfo);
			
			Main.Logger.logInfo("Language.init: The language \"" + getString(Keys.LANG_NAME) + "\" Successfully :)");
			return true;
		} catch (Exception e) {
			Main.Logger.logAlert(
					"Language.init: Loading the ResourceBoundle(" +
			        this.language + ") has failed!", e);
			
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
		return this.language;
	}
	public Locale getLocale() {
		return this.localeInfo;
	}

	public JMenu createSettingsMenu() {
		JMenu menu = new JMenu(getString(Keys.SETTINGS_LANGUAGE_MENU));
		return menu;
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
