package com.gmail.xfrednet.xfutils.link;

import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.gmail.xfrednet.xfutils.Main;
import com.gmail.xfrednet.xfutils.util.Language;

import sun.awt.shell.ShellFolder;

/**
 * This is the link manager of xFutils. It loads links from the
 * {@link #LINK_DIR <tt>link directory</tt>} and converters them to JMenuItems
 * that execute the link when they are activated on.
 * */
public class LinkManager {

	private static final String LINK_DIR    = "links\\";
	static final String LINK_SUFFIX = ".lnk";
	private static final int MENU_ICON_SIZE = 32;
	
	private Language language;
	
	/**
	 * This method tests if the {@link #LINK_DIR <tt>link directory</tt>}
	 * is valid and if the {@link com.gmail.xfrednet.xfutils.links.LinkManager}
	 * can be used.
	 * 
	 * @return It returns true if this instance can be used and if the 
	 *         directory is valid.
	 * */
	public boolean init(Language language) {
		if (!Main.AreLinksEnabled)
			return false;
			
		this.language = language;
		
		return linkDirectoryValidation();
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
	
	// Creates a JMenuItem for every file and folder that is inside the link directory 
	public JMenuItem[] getMenuItems() {
		// Value creation
		File linkDir = new File(LINK_DIR);
		File[] validFiles = linkDir.listFiles(new LinkManagerFileFilter());
		JMenuItem[] menuItems = new JMenuItem[((validFiles != null) ? validFiles.length : 0) + 1];
		
		// Create a MenuItem for every valid file
		for (int itemIndex = 0; itemIndex < menuItems.length - 1; itemIndex++) {
			menuItems[itemIndex] = createMenuItemFromFile(validFiles[itemIndex]);
		}
		
		menuItems[menuItems.length - 1] = createAddLinkMenu();
		
		// return the MenuItems
		return menuItems;
	}
	private JMenuItem createAddLinkMenu() {
		JMenuItem addLinkMenu = new JMenuItem(this.language.getString(Language.Keys.MENU_ITEM_ADD_LINK));
		
		// Open in explorer
		addLinkMenu.addActionListener(l -> {
			try {
				Desktop.getDesktop().open(new File(LINK_DIR));
			} catch (IOException e) {
				Main.Logger.logError("LinkManager[AddLink-Item]: Unable to open the link-directory", e);
			}
		});
		
		return addLinkMenu;
	}
	private JMenuItem createMenuItemFromFile(File itemFile) {
		String menuLabel = getMenuLabelFromFile(itemFile);
		
		// If the file is a directory a sub menu is created with the 
		// valid files from that directory (if any)
		if (itemFile.isDirectory()) {
			JMenu item = new JMenu(menuLabel);
			item.setIcon(LoadFileIcon(itemFile));
			
			// Add items for all valid files
			File[] validFiles = itemFile.listFiles(new LinkManagerFileFilter());
			if (validFiles != null && validFiles.length != 0) {
				for (File file : validFiles) {
					item.add(createMenuItemFromFile(file));
				}
			} else {
				item.add(this.language.getString(Language.Keys.MENU_ITEM_EMPTY));
			}

			return item;
		}

		// TODO add a Icon preview
		// Create Item and an ActionListener
		JMenuItem item = new JMenuItem(menuLabel);
		item.setIcon(LoadFileIcon(itemFile));
		item.addActionListener(l -> StartLink(itemFile));
		
		return item;
	}
	
	private static String getMenuLabelFromFile(File file) {
		if (file.getName().toLowerCase().endsWith(LinkManager.LINK_SUFFIX)) 
			return file.getName().substring(0, file.getName().length() - LINK_SUFFIX.length());
		
		return file.getName();
	}
	private static Icon LoadFileIcon(File file) {
		try {
			// Load the icon
			ShellFolder shellFolder = ShellFolder.getShellFolder(file);
			Image loadedImage = shellFolder.getIcon(true);
			int loadedWidth = loadedImage.getWidth(null);
			int loadedHeight = loadedImage.getHeight(null);
			
			// resize the icon if it has the wrong size
			Image iconImage;
			if (loadedWidth == MENU_ICON_SIZE && 
					loadedHeight == MENU_ICON_SIZE) {
				iconImage = loadedImage;
			} else {
				// A BufferedImage where the Icon is scaled on to
				BufferedImage bufferedImage = new BufferedImage(
						MENU_ICON_SIZE, 
						MENU_ICON_SIZE, 
						BufferedImage.TYPE_INT_ARGB);
				
				// drawing the scaled Image on the BufferedImage
				Graphics2D g = bufferedImage.createGraphics();
				g.drawImage(loadedImage, 
						0, 0, MENU_ICON_SIZE, MENU_ICON_SIZE, null);
				g.dispose();
				
				// set the iconImage to the BufferedImage
				iconImage = bufferedImage;
			}
			
			// Create a ImageIcon from the image and return that icon
			return new ImageIcon(iconImage);
		} catch (FileNotFoundException e) {
			Main.Logger.logAlert(
					"LinkManager.LoadFileIcon: Unable to load the icon for the file: " + file.getAbsolutePath(), 
					e);
		}
		return null;
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
		return file.getName().toLowerCase().endsWith(LinkManager.LINK_SUFFIX);
		
		// TOD0 maybe add a general "file.canExecute" or the link type of other OS's
	}
	
}
