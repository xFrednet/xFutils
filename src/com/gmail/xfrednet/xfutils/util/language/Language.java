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

@SuppressWarnings("WeakerAccess")
public class Language {

	private static final String RESOURCE_BUNDLE_BASE_NAME = "translations\\lang";
	private static final String AVAILABLE_LANGUAGES = "translations\\available_languages.txt";
	
	/**
	 * This is the {@linkplain Locale} object that holds localized information
	 * here it is only used to store the language name
	 * */
	private Locale localeInfo;
	
	/**
	 * These {@linkplain Properties} are used as a map that contain all translations.
	 * */
	private Properties translation;

	/**
	 * This {@linkplain List} contains all bundles that are loaded. These paths
	 * will be used when {@linkplain #changeLanguage(String)} get's called.
	 * */
	private List<String> resourceBundlePaths;
	
	/**
	 * This {@linkplain List} contains all {@linkplain ILanguageListener}s that will
	 * be notified when the language changes.
	 * */
	private List<ILanguageListener> changeListeners;
	
	private JMenu guiSettingsMenu = null;
	
	/**
	 * This initializes the class and loads the default translations.
	 * */
	public Language(String language) {
		this.localeInfo = new Locale(language);
		this.translation = new Properties();

		this.changeListeners = new ArrayList<>();

		this.resourceBundlePaths = new ArrayList<>();
		this.addResource(RESOURCE_BUNDLE_BASE_NAME);
	}
	
	/**
	 * This method changes the language. It is also used to initialize the language.
	 * 
	 * This will reload the entire translations, to add a resource bundle
	 * please call {@linkplain #addResource(String)}.
	 * 
	 * @param language The abbreviation of the new language.
	 * */
	public void changeLanguage(String language) {
		this.localeInfo = new Locale(language);

		// reload all resources for the new language
		for (String path : this.resourceBundlePaths) {
			loadResource(path, this.localeInfo, false);
		}

		// notify all stalkers, I mean listeners
		for (ILanguageListener stalker : this.changeListeners) {
			stalker.onLanguageChange(this);
		}
	}
	/**
	 * This adds the given bundle path to the lost of resources to reload
	 * the pack if the language changes. It than calls
	 * {@linkplain #loadResource(String, Locale, boolean)} to add the
	 * translations.
	 *
	 * <p>This method will try to handle loading issues by loading the
	 * English resource if the language loading failed. A info will be written
	 * to the console. In case of this failing as well, it will log an Error
	 * to the logger and give up.</p>
	 * 
	 * @param bundlePath The path of the bundle that should be loaded.
	 * */
	public boolean addResource(String bundlePath) {
		this.resourceBundlePaths.add(bundlePath);

		return loadResource(bundlePath, this.localeInfo, true);
	}
	/**
	 * This method tries to load the {@linkplain ResourceBundle} for the current
	 * language from the given bundlePath
	 *
	 * <p>This method will try to handle loading issues by loading the
	 * English resource if the language loading failed. A info will be written
	 * to the console. In case of this failing as well, it will log an Error
	 * to the logger and give up.</p>
	 *
	 * @param bundlePath The path of the bundle that should be loaded.
	 * @param locale The {@linkplain Locale} that should be used to load the {@linkplain ResourceBundle}.
	 * @param firstLoad This states if it's the first time(during this runtime) that
	 *        the {@linkplain ResourceBundle} get's loaded. If this is false it will not try to
	 *        load the English {@linkplain ResourceBundle} on failure and just keep the old keys loaded.
	 *
	 * @return This returns true if the loading was successful.
	 * */
	private boolean loadResource(String bundlePath, Locale locale, boolean firstLoad) {
		try {
			ResourceBundle resource = ResourceBundle.getBundle(bundlePath, locale);
			
			resource.keySet().forEach(key -> this.translation.put(key, resource.getString(key)));
			
			Main.Logger.logInfo("Language.init: The language \"" + getString(Keys.LANG_NAME) + "\" Successfully :)");
			return true;
		} catch (Exception e) {
			// Only try to load the English pack if this is the first loading
			if (!firstLoad)
				return false;

			// check if the local is a english local
			Locale engLocale = new Locale("en");
			if (!locale.getLanguage().equals(engLocale.getLanguage())) {
				// try to load the English local if the given local doesn't have a resource
				return loadResource(bundlePath, locale, true);
			} else {
				// Well we've failed and with "we" I mean YOU!!!
				Main.Logger.logAlert(
					"Language.init: Loading the ResourceBundle(" + getLanguage() + ") has failed!",
					e);
			}
			
			return false;
		}
	}

	/**
	 * This look up the translation for the given key. Most default keys can be fount
	 * in the {@link Language.Keys <tt>Keys</tt>} class.
	 *
	 * <p>This method will return "[Missing Translation]" if the translation is
	 * missing this might be caused by a loading error or a wrong key</p>
	 *
	 * @param key The key for the requested String.
	 *
	 * @return Ths returns the String that is mapped to the key or "[Missing Translation]"
	 * if the translation couldn't be found.
	 * */
	public String getString(String key) {
		return this.translation.getProperty(key, "[Missing Translation]");
	}

	/**
	 * This returns the language String used to compare or save languages.
	 *
	 * @return The language identifying string used by {@link Locale}
	 * */
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
			if (langAbbreviation.equals(getLanguage())) {
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

	/**
	 * This class includes all keys for the base translation files
	 * */
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
