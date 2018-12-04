package com.gmail.xfrednet.xfutils.util;

import com.gmail.xfrednet.xfutils.Main;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * This class wraps the {@link javax.swing.JPopupMenu <tt>JPopupMenu</tt>} class and
 * adds some needed features to use it without a JFrame or Window
 */
public class IndependentPopupMenu {

	/**
	 * This is a {@link javax.swing.JPopupMenu <tt>JPopupMenu</tt>} the default java
	 * {@link java.awt.TrayIcon <tt>TrayIcon</tt>} does not support the JPopupMenu as
	 * the menu. A ActionListener is used determine when this menu should be shown.
	 * Some extra trikeruny is used to receive updates and make the menu behave like it
	 * should. This trikeru is described in the documentation
	 * {@linkplain #showMenu(int, int)}
	 */
	private JPopupMenu popupMenu = null;

	/**
	 * This is a {@link javax.swing.JDialog <tt>JDialog</tt>} that is used to show the
	 * {@linkplain #popupMenu}. The reason for this is described in
	 * {@linkplain #showMenu(int, int)}
	 * */
	private JDialog showSupporter = null;

	/**
	 * This is in array of indices of section ends. These values are used to add
	 * {@linkplain JMenuItem}s at the end of a section with
	 * {@linkplain #addMenuItem(JMenuItem, int)}.
	 *
	 * <p>These indices are maintained and changed by
	 * {@linkplain #addMenuItem(JMenuItem, int)} and
	 * {@linkplain #removeMenuItem(JMenuItem)}</p>
	 * */
	private int[] sectionEnds;

	/**
	 * This initializes everything inside this class that can be initialized
	 *
	 * @param sectionCount The amount of section that this menu should have.
	 */
	public IndependentPopupMenu(int sectionCount) {
		initPopupMenu(sectionCount);
		initShowSupporter();
	}

	private void initPopupMenu(int sectionCount) {
		this.sectionEnds = new int[sectionCount];

		this.popupMenu = new JPopupMenu();

		for (int sectionNo = 0; sectionNo < sectionCount; sectionNo++) {
			this.popupMenu.addSeparator();
			this.sectionEnds[sectionNo] = sectionNo;
		}

		this.popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				Main.Logger.logDebugMessage(
					"IndependentPopupMenu.PopupMenuListener: The menu will become invisible, the dialog will be disposed");
				showSupporter.setVisible(false);
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {}
		});
	}
	private void initShowSupporter() {
		this.showSupporter = new JDialog();

		this.showSupporter.setFocusable(true);
		this.showSupporter.setSize(10, 10);
		this.showSupporter.setUndecorated(true);
		this.showSupporter.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.showSupporter.setAlwaysOnTop(true);
		this.showSupporter.setVisible(false);
		this.showSupporter.setOpacity(0.0f);
	}

	public void showMenu(int x, int y) {
		// So the following, the TrayIcon does not work well with
		// a JPopupMenu, and with not well I mean not at all. The main problem
		// is that the menu wouldn't close it self.
		// So what did I do? well I create a JDialog without any decoration, that has a
		// alpha value of 0. The JPopupMenu, now behaves like a good boy, the only problem
		// is disposing the dialog after the JPopupMenu is done. The easy fix? Add a
		// PopupMenuListener that disposes the dialog when the menu goes invisible

		// Create the dialog
		JDialog dialog = new JDialog();
		dialog.setFocusable(true);
		dialog.setBounds(x, y, 10, 10);
		dialog.setUndecorated(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		dialog.setOpacity(0.0f);

		// Show the menu
		// The x and y values for the tray menu are now dialog relative
		this.popupMenu.show(dialog, 0, 0);
		this.popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				Main.Logger.logDebugMessage(
					"IndependentPopupMenu.PopupMenuListener: The menu will become invisible, the dialog will be disposed");
				dialog.dispose();
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {}
		});
		Main.Logger.logInfo("showTrayMenu: The popupMenu should be visible now!");

		// So, am I proud of this code, well I'm proud I found a well
		// working solution for my problem
	}

	public void addMenuItem(JMenuItem menuItem, int section) {

	}

	public void removeMenuItem(JMenuItem menuItem) {

	}
}
