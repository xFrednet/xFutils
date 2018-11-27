package com.gmail.xfrednet.xfutils.link;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.gmail.xfrednet.xfutils.Main;

public class LinkManager {

	private static final String LINK_DIR    = "links\\";
	static final String LINK_SUFFIX = ".lnk";
	
	public boolean init() {
		if (!Main.AreLinksEnabled)
			return false;
		
		if (!linkDirectoryValidation()) {
			return false;
		}
		
		return true;
	}
	private boolean linkDirectoryValidation() {
		File linkDir = new File(LINK_DIR);
		Main.Logger.logInfo("LinkManager.loadLinkFiles: The link directory is: " + linkDir.getAbsolutePath());
		
		//
		// #### Validate the link directory
		//
		// Existence check
		if (!linkDir.exists()) {
			// The file does not exist
			
			Main.Logger.logInfo("LinkManager.loadLinkFiles: The link directory does not exist it will be created!");
			if (!linkDir.mkdir()) {
				Main.Logger.logAlert("LinkManager.loadLinkFiles: The link directory creation failed!");
				return false;
			}
			
			Main.Logger.logInfo("LinkManager.loadLinkFiles: The link directory was created sucessfully!");
			return true;
		}
		// Directory check
		if (!linkDir.isDirectory()) {
			Main.Logger.logError("LinkManager.loadLinkFiles: The link directory is a file.... I can't work like this!!!!");
			return false;
		}
		
		// 90% validation, 10% fun... well <1% fun but still
		return true;
	}
	
	public JMenuItem[] getMenuItems() {
		// Value creation
		File linkDir = new File(LINK_DIR);
		File[] validFiles = linkDir.listFiles(new LinkManagerFileFilter());
		JMenuItem[] menuItems = new JMenuItem[validFiles.length];
		
		// Create a MenuItem for every valid file
		for (int itemIndex = 0; itemIndex < validFiles.length; itemIndex++) {
			menuItems[itemIndex] = createMenuItemFromFile(validFiles[itemIndex]);
		}
		
		// return the MenuItems
		return menuItems;
	}
	private JMenuItem createMenuItemFromFile(File itemFile) {
		String menuLabel = getMenuLabelFromFile(itemFile);
		
		// If the file is a directory a sub menu is created with the 
		// valid files from that directory (if any)
		if (itemFile.isDirectory()) {
			JMenu item = new JMenu(menuLabel);
			
			// Add items for all valid files
			File[] validFiles = itemFile.listFiles(new LinkManagerFileFilter());
			for (File file : validFiles) {
				item.add(createMenuItemFromFile(file));
			}
			
			return item;
		}

		// TODO add a Icon preview
		// Create Item and an ActionListener
		JMenuItem item = new JMenuItem(menuLabel);
		item.addActionListener(l -> StartLink(itemFile));
		
		return item;
	}
	
	private String getMenuLabelFromFile(File file) {
		if (file.getName().toLowerCase().endsWith(LinkManager.LINK_SUFFIX)) 
			return file.getName().substring(0, file.getName().length() - LINK_SUFFIX.length());
		
		return file.getName();
	}
	private static void StartLink(File linkFile) {
		if (!linkFile.exists())
			return;

		try {
			new ProcessBuilder("cmd", "/c", linkFile.getAbsolutePath()).start();
			Main.Logger.logInfo("LinkManager.StartLink: Started the file: " + linkFile.getAbsolutePath());
		} catch (IOException e) {
			Main.Logger.logError("LinkManager.StartLink: Unable to start the Process, the following Error occurred", e);
		}
	}
}

class LinkManagerFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		// I don't load hidden files. It would just confuse unknown users
		if (file.isHidden())
			return false;
		
		// Directories are valid, they will be listed as Menus with Link as sub items in the tray menu
		if (file.isDirectory())
			return true;
		
		// Test if the file is a windows link file
		if (file.getName().toLowerCase().endsWith(LinkManager.LINK_SUFFIX)) 
			return true;
		
		// TOD0 maybe add a general "file.canExecute" or the link type of other OS's
		return false;
	}
	
}
