package com.gmail.xfrednet.xfutils.plugin;

import com.gmail.xfrednet.xfutils.util.Logger;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class PluginManager {

	private static final String PLUGIN_DIR = "./plugins/";

	Logger logger;
	List<IPlugin> plugins;

	public PluginManager(Logger logger) {
		this.logger = logger;

		plugins = new ArrayList<>();
	}

	public void initPlugins() {

	}
	public void terminatePlugins() {

	}

	List<MenuElement> getPluginElements() {

	}

	JPanel getSettingsPanel() {

	}
}
