package com.gmail.xfrednet.xfutils.util;

import java.util.Locale;
import java.util.ResourceBundle;

import com.gmail.xfrednet.xfutils.Main;

public class Language {

	private static final String RESOURCE_BUNDLE_BASE_NAME = "translations\\lang";
	
	public static Language Init(String langName) {
		Language lang = new Language(langName);
		if (lang.init()) {
			return lang;
		} else {
			// Try to load the English property file before giving up
			if (langName != "en") {	
				Main.Logger.logAlert(
						"Language.Init: was unable to load the .property file for the language: " +
						 langName);
				Main.Logger.logAlert("    -> Language.Init will try to load the English property file");
				return Init("en");
			} else {
				Main.Logger.logError("Language.Init: Was unable to load a .property language file, see previous alert!!!");
				return null;
			}
		}
	}
	
	String language;
	Locale localeInfo;
	ResourceBundle resource;
	
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
			
			Main.Logger.logInfo("Language.init: The language \"" + getString("lang_name") + "\" Successfully :)");
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
	
	public class Key {
		public static final String LANG_NAME = "lang_name";
		
		public static final String MENU_LABEL_PLUGINS = "menu_label_plugins";
		public static final String MENU_LABEL_LINKS = "menu_label_links";
		public static final String MENU_LABEL_META = "menu_label_meta";
		
		public static final String MENU_ITEM_EXIT = "menu_item_exit";
	}
}
