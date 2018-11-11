package com.gmail.xfrednet.xfutils.plugin;

import com.gmail.xfrednet.xfutils.util.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PluginManager {

	private static final String PLUGIN_DIR             = "plugins\\";
	private static final String ENABLED_PLUGINS_FILE   = PLUGIN_DIR + ".config.xml";
	private static final String XML_ROOT_ELEMENT       = "pluginmamanger";
	private static final String XML_ENABLED_PLUGIN_TAG = "plugin";

	private Logger logger;
	private List<IPlugin> plugins;

	public PluginManager(Logger logger) {
		this.logger = logger;

		plugins = new ArrayList<>();
	}

	public void initPlugins() {
		File[] pluginFiles = getAvailablePlugins();

		List<String> names = new ArrayList<>();
		names.add("hello.jar");
		names.add("name 2.jar");
		names.add("me not");
		saveEnabledPluginsList(names);

		loadEnabledPluginsList();
	}
	// File interactions
	private File[] getAvailablePlugins() {
		File pluginDir = new File(PLUGIN_DIR);

		// Debug info
		logger.logInfo("getAvailablePlugins: The plugin-directory is: " + pluginDir.getAbsolutePath());

		// validation
		if (!validatePluginDir(pluginDir)) {
			logger.logError("getAvailablePlugins: The plugins can't be loaded because the plugin-directory is invalid.");
			return new File[0];
		}

		// Get the ".jar"-files
		File[] jarFiles = pluginDir.listFiles(new PluginFileFilter());

		// Debug info
		if (logger.isDebugLogEnabled()) {
			for (File jarFile : jarFiles) {
				// get the plugin path and shorten it for the log.
				String jarPath = jarFile.getAbsolutePath();
				jarPath = ".\\" +  jarPath.substring(jarPath.indexOf(PLUGIN_DIR));

				// log the plugin path
				logger.logDebugMessage("getAvailablePlugins: Found possible plugin: " + jarPath);
			}
		}

		return jarFiles;
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
	// Settings interaction
	private List<String> loadEnabledPluginsList() {
		// get the file
		File enabledPluginsFile = new File(ENABLED_PLUGINS_FILE);
		logger.logDebugMessage("loadEnabledPluginsList: The path of the config is: " + enabledPluginsFile.getAbsolutePath());

		// validation
		if (!enabledPluginsFile.exists() ||
				enabledPluginsFile.isDirectory() ||
				!enabledPluginsFile.canRead()) {
			// log information
			logger.logAlert("loadEnabledPluginsList: The config is invalid." );
			logger.logDebugMessage("loadEnabledPluginsList: Check 1: file.exists()      : " + enabledPluginsFile.exists());
			logger.logDebugMessage("loadEnabledPluginsList: Check 2: !file.isDirectory(): " + !enabledPluginsFile.isDirectory());
			logger.logDebugMessage("loadEnabledPluginsList: Check 3: file.canRead()     : " + enabledPluginsFile.canRead());

			// return
			return new ArrayList<>();
		} else {
			// log success
			logger.logDebugMessage("loadEnabledPluginsList: The config seams to be valid.");
		}

		// parse or at least try to parse the file
		try {
			// load the content
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(enabledPluginsFile);
			doc.getDocumentElement().normalize();

			// create the output array
			List<String> enabledPluginNames = new ArrayList<>();

			// go though the nodes
			NodeList enabledPluginsNodeList = doc.getElementsByTagName(XML_ENABLED_PLUGIN_TAG);
			logger.logDebugMessage("loadEnabledPluginsList: has found " + enabledPluginsNodeList.getLength() + " nodes.");
			for (int nodeIndex = 0; nodeIndex < enabledPluginsNodeList.getLength(); nodeIndex++) {
				// get the node
				Node enabledPluginNode = enabledPluginsNodeList.item(nodeIndex);
				if (enabledPluginNode.getNodeType() != Node.ELEMENT_NODE)
					continue;

				// convert to element
				Element enabledPluginElement = (Element)enabledPluginNode;
				String pluginName = enabledPluginElement.getTextContent();
				logger.logDebugMessage("loadEnabledPluginsList: Note number: " + nodeIndex + " contains the plugin name: " + pluginName);
				enabledPluginNames.add(pluginName);
			}

			// return
			return enabledPluginNames;

		} catch (ParserConfigurationException | SAXException | IOException e) {
			logger.logError("loadEnabledPluginsList: Fail: ", e);
			e.printStackTrace();
			return new ArrayList<>();
		}
		// - Okay here is the thing, I love clean code. It's the best thing going back and
		//   seeing that you were able to write good code at some point...
		// - But for the love of GOD! I can't write any code that loads from a file and
		//   looks clean. I JUST CAN'T!
		// - I just hope that this is readable enough... and that no one have has to read this again.
		// - ¯\_(ツ)_/¯
	}
	private boolean saveEnabledPluginsList(List<String> enabledPlugins) {
		try {
			DocumentBuilderFactory dbFactory =
				DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();

			// root element
			Element rootElement = doc.createElement(XML_ROOT_ELEMENT);
			doc.appendChild(rootElement);

			// write elements
			for (String pluginName : enabledPlugins) {
				Element pluginElement = doc.createElement(XML_ENABLED_PLUGIN_TAG);
				pluginElement.setTextContent(pluginName);
				rootElement.appendChild(pluginElement);
			}

			// Write to file
			StreamResult result = new StreamResult(new File(ENABLED_PLUGINS_FILE));
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(new DOMSource(doc), result);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
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

		if (pathname.isDirectory())
			return false;

		return pathname.getName().endsWith(".jar");
	}

}