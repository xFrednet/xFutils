package com.gmail.xfrednet.xfutils.plugin;

import com.gmail.xfrednet.xfutils.util.Logger;

import javax.swing.*;

public interface IPlugin {

	String getDisplayName();
	String getVersionString();

	boolean init(Logger logger, PluginManager manager, String pluginDir) throws Exception;
	boolean cleanup() throws Exception;

	// TODO 04.11.2018 maybe icon?
	JMenu getSystemTrayMenu();
}
