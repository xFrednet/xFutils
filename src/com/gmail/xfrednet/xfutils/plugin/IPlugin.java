package com.gmail.xfrednet.xfutils.plugin;

import com.gmail.xfrednet.xfutils.util.Logger;

import javax.swing.*;

public interface IPlugin {
	
	// TODO add language class and pass the language for the descriptions.
	String getDisplayName();
	String getVersionString();
	String getShortDescription();
	String getDescription();

	boolean init(Logger logger, PluginManager manager, String pluginDir) throws Exception;
	boolean terminate() throws Exception;
	
	JMenu getSystemTrayMenu();
	
	// This method will be called when the Plugin
	// has requested regular updates
	void update();
}
