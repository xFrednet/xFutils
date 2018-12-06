package com.gmail.xfrednet.xfutils.util;

import com.gmail.xfrednet.xfutils.Main;

import java.awt.Component;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * This class wraps the {@link javax.swing.JPopupMenu <tt>JPopupMenu</tt>} class and
 * adds some needed features to use it without a JFrame or Window
 * 
 * <p>The {@linkplain JPopupMewnu} should support something called <tt>lightweight</tt>
 * this means that it can stand alone without being attached to a different swing
 * component. This <tt>lightweight</tt> mode does not seam to work very well. After 
 * some testing I found a working solution, that is described in the docu of 
 * {@linkplain #showMenu(int, int)}. You can just use this class without any extra
 * afford.</p>
 */
public class IndependentPopupMenu {

	/**
	 * This is the {@linkplain JPopupMenu} that get's displayed if 
	 * {@linkplain #showMenu(int, int)} is called
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
	 * {@linkplain #add(JMenuItem, int)}.
	 *
	 * <p>These indices are maintained and changed by
	 * {@linkplain #add(JMenuItem, int)} and
	 * {@linkplain #remove(JMenuItem)}</p>
	 * */
	private int[] sectionEnds;

	/**
	 * This initializes everything inside this class that can be initialized.
	 * 
	 * <p>Call {@linkplain #dispose()} at the end of usage</p>
	 *
	 * @param sectionCount The amount of section that this menu should have.
	 */
	public IndependentPopupMenu(int sectionCount) {
		// validation
		if (sectionCount < 1) {
			sectionCount = 1;
			Main.Logger.logAlert("IndependentPopupMenu: The section count has to be atleast 1");
		}
		
		initPopupMenu(sectionCount);
		initShowSupporter();
	}
	private void initPopupMenu(int sectionCount) {
		this.sectionEnds = new int[sectionCount];

		this.popupMenu = new JPopupMenu();
		
		// setup everything for the section system
		for (int sectionNo = 0; sectionNo < sectionCount; sectionNo++) {
			if (sectionNo != 0) {
				this.popupMenu.addSeparator();
			}
			this.sectionEnds[sectionNo] = sectionNo;
		}

		// This popup listener is used to hide the dialog after the
		// show support of it isn't needed anymore
		this.popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				Main.Logger.logDebugMessage(
					"IndependentPopupMenu.PopupMenuListener: The menu will become invisible, " + 
						"the dialog will be hidden");
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
	
	/**
	 * This method shows the popupmenu at the given location.
	 * 
	 * <p>This method or the entire class need some explanations, so here
	 * it goes: The {@linkplain JPopupMenu} has something like a modus called
	 * leightweight that should do exactly what is wanted showing a 
	 * {@linkplain JPopupMenu} without being attached to a window.
	 * <br>
	 * But not all is beautiful in the world of popup menus because the menu,
	 * like children, doesn't behave like it should when the parents aren't 
	 * around. It doesn't show which items are hovered over and doesn't close
	 * if the user clicks beside the popupmenu like it should.
	 * <br>
	 * So what is the solution or better say workaround? An invisible 
	 * {@linkplain JDialog} is created.({@linkplain #initShowSupporter()}) 
	 * The dialog is used because it doesn't show an unnecessary Icon in 
	 * the taskbar. This dialog is than put to the requested location. 
	 * The next step is to show the {@linkplain JPopupMenu} on the
	 * {@linkplain JDialog}. The popupmenu behaves now like it's expected 
	 * because the {@linkplain JDialog} takes th parent role.
	 * <br>
	 * The last task is to hide the dialog when it is not needed this is
	 * done by add an {@linkplain PopupMenuListener} that is attached to 
	 * the {@linkplain #popupMenu} the menu hides the dialog when it self
	 * becomes invisible</p>
	 * 
	 * @param x The x position where the menu should be shown
	 * @param y The y position where the menu should be shown
	 * */
	public void showMenu(int x, int y) {
		// So the following, the TrayIcon does not work well with
		// a JPopupMenu, and with not well I mean not at all. The main problem
		// is that the menu wouldn't close it self.
		// So what did I do? well I create a JDialog without any decoration, that has a
		// alpha value of 0. The JPopupMenu, now behaves like a good boy, the only problem
		// is disposing the dialog after the JPopupMenu is done. The easy fix? Add a
		// PopupMenuListener that disposes the dialog when the menu goes invisible

		// Show the dialog
		this.showSupporter.setLocation(x, y);
		this.showSupporter.setVisible(true);

		// The x and y values for the tray menu are now dialog relative
		this.popupMenu.show(this.showSupporter, 0, 0);
		
		// Log something to the log
		Main.Logger.logDebugMessage("IndependentPopupMenu: The popupMenu should be visible now!");

		// So, am I proud of this code, well I'm proud I found a well
		// working solution for my problem
	}

	/**
	 * This hides the popup menu if it is visible
	 * */
	public void hideMenu() {
		if (this.popupMenu.isVisible()) {
			this.popupMenu.setVisible(false);
		}
	}
	
	/**
	 * This hides the popup menu, disposes the helper {@linkplain JDialog}
	 * and sets everything null.
	 * 
	 * <p>Note that the popup menu can't be used again after dispose is called</p>
	 * */
	public void dispose() {
		// Kill le popup menu
		if (this.popupMenu.isVisible()) {
			this.popupMenu.setVisible(false);
		}
		this.popupMenu = null;
		
		// kill the support character
		this.showSupporter.dispose();
		this.showSupporter = null;
		
		// kill the author for these useless commands
	}
	
	/**
	 * This add a {@linkplain JMenuItem} at the end of the section.
	 * 
	 * @param menuItem The {@linkplain JMenuItem} that should be added to the end of
	 *                 the section that is defined by sectionNo.
	 * @param sectionNo The index of the section that this {@linkplain JMenuItem} should
	 *                  be added to. The sections start with the index 0.
	 * */
	public void add(JMenuItem menuItem, int sectionNo) {
		// Validation
		if (menuItem == null) {
			return;
		}
		
		// Clamp sectionNo
		if (sectionNo < 0) {
			sectionNo = 0;
		} else if (sectionNo >= this.sectionEnds.length) {
			sectionNo = this.sectionEnds.length - 1;
		}
		
		// Add the item
		this.popupMenu.insert(menuItem, this.sectionEnds[sectionNo]);
		
		// increase this and the following section ends
		for (int sectionIndex = sectionNo; sectionIndex < this.sectionEnds.length; sectionIndex++) {
			this.sectionEnds[sectionIndex]++;
		}
		
		// and now: be happy don't worry
	}

	/**
	 * This removes the given {@linkplain JMenuItem} from the popupMenu and
	 * changes to section ends to fit the new indices.
	 * 
	 * @param menuItem The {@linkplain JMenuItem} that should be removed
	 * */
	public void remove(JMenuItem menuItem) {
		// Search the item inside the component list
		int menuIndex = 0;
		Component[] components = this.popupMenu.getComponents();
		for (Component com : components) {
			if (!(com instanceof JMenuItem))
				continue;
			
			// Test if the Item was found.
			if (menuItem.equals(com)) {
				// break if the item was found.
				break;
			}
			
			menuIndex++;
		}
		
		// Test if the component was found. The item was only found if menuIndex != components.length
		if (menuIndex == components.length) {
			return;
		}
		
		// remove the component
		this.popupMenu.remove(menuItem);
		
		// sub one from all section ends that follow the item
		for (int sectionIndex = this.sectionEnds.length - 1; 
				sectionIndex >= 0;
				sectionIndex++){
			
			// The section end is always one higher than the last item in the section
			// this means that the >-operator should "catch em all"... sorry...
			if (this.sectionEnds[sectionIndex] > menuIndex) {
				this.sectionEnds[sectionIndex]--;
			} else {
				// The loop can break when one index is less than the menuIndex because
				// all following ends will be lower as well.
				break;
			}
		}
	}
}
