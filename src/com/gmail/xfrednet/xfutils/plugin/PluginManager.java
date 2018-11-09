package com.gmail.xfrednet.xfutils.plugin;

import com.gmail.xfrednet.xfutils.util.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class PluginManager {

	private static final String PLUGIN_DIR = "plugins\\";

	Logger logger;
	List<IPlugin> plugins;

	private File[] getAvailablePlugins() {
		File pluginDir = new File(PLUGIN_DIR);
		
		logger.logInfo("getAvailablePlugins: The Plugin-directory is: " + pluginDir.getAbsolutePath());
		
		if (!validatePluginDir(pluginDir)) {
			logger.logError("getAvailablePlugins: The plugins can't be loaded because the plugin directory has failed.");
			return new File[0];
		}
		
		File[] files = pluginDir.listFiles(new PluginFileFilter());
		//TODO remove dirTestCodeIfunneccersceagfieshfoihoiewhgfoiuewgfew
		for (File file : files) {
			if (file.isDirectory())
				return null;
		}
		
		return files;
		//
	}
	private boolean validatePluginDir(File pluginDir) {
		// Existence check
		if (!pluginDir.exists()) {
			logger.logDebugMessage("validatePluginDir: The plugin-directory does't exist and will be created:");
			
			// Use mkdir() because if the pluginDir needs to create more than one folder
			// something has gone wrong... or the directory was changed without thinking.
			if (pluginDir.mkdir()) {
				logger.logInfo("validatePluginDir: The plugin-directory was created by the PluginManager.");
			} else {
				logger.logError("validatePluginDir: The plugin-directory doesn't exist and couldn't be crated!");
				return false;
			}
		}
		
		// Validation check
		if (!pluginDir.isDirectory()) {
			// Someone created a file with the name of the plugin folder
			// Someone needs a life...
			// That someone is probably me...
			logger.logError("validatePluginDir: The plugin-directory has to be a directory and not a file...");
			return false;
		} else {
			logger.logDebugMessage("validatePluginDir: The plugin-directory has successfully completed all validation checks.");
		}
		
		// check access rights
		if (!pluginDir.canRead() || !pluginDir.canWrite()) {
			logger.logError(
					"validatePluginDir: The PluginManager has not the necessary rights for the plugin-directory." +
					"[" + pluginDir.canRead() + ", " + pluginDir.canWrite() + "]");
			return false;
		}
		
		return true;
	}
	
	public PluginManager(Logger logger) {
		this.logger = logger;

		plugins = new ArrayList<>();
	}
	
	public void initPlugins() {
		File[] plugins = getAvailablePlugins();
		for (File plugin : plugins) {
			logger.logDebugMessage("Found plugin: " + plugin.getAbsolutePath());
		}
	}
	public void terminatePlugins() {

	}

	List<MenuElement> getPluginElements() {
		List<MenuElement> menuItemList = new ArrayList<>(plugins.size());
		
		for (IPlugin plugin : this.plugins) {
			menuItemList.add(plugin.getSystemTrayMenu());
		}
		
		return menuItemList;
	}

	JPanel getSettingsPanel() {
		return null;
	}
}

class PluginFileFilter implements FileFilter {

	@Override
	public boolean accept(File pathname) {
		return !pathname.isDirectory() && 
				pathname.getName().endsWith(".jar");
	}


	
}