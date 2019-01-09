package com.gmail.xfrednet.xfutils.util.language;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

/**
 * This class manages the translation and text updates for GUI Items
 * You can use on of multiple (soon) multiple add functions to add an
 * item that should be managed.
 *
 * <p>While this class can stand alone I would advice to use
 * {@link Language#getGUIManager() <tt>getGUIManager()</tt} from
 * your current {@linkplain Language} class instance</p>
 * */
public class LanguageGUIManager implements ILanguageListener{

	/**
	 * This is a link to the {@linkplain Language} class that this
	 * instance is listening to. The instance is used to set the
	 * initial text when a new GUI item is added
	 * */
	Language language;

	/**
	 * This is a {@linkplain List} of all {@linkplain JMenuItem}s that
	 * are managed by this class.
	 * */
	List<JMenuItem> menuItems;

	/**
	 * This saves the given {@linkplain Language} class and initializes
	 * other members. Note that this does not add it self to the
	 * {@linkplain Language} as a listener. This has to be done manually.
	 * This step is automatically done with the {@linkplain LanguageGUIManager}
	 * instance inside the {@linkplain Language} class. The instance can be
	 * accessed via {@linkplain Language#getGUIManager()}
	 * */
	LanguageGUIManager(Language language) {
		this.language = language;
		
		this.menuItems = new ArrayList<>();
	}

	/**
	 * This method makes this class an official stalker, a stalker with
	 * class but still a stalker. It will be called when the languages changes.
	 *
	 * @param language The language with the newly loaded translation
	 *
	 * @see ILanguageListener for more information
	 * */
	@Override
	public void onLanguageChange(Language language) {
		updateGUI(language);
	}

	/**
	 * This method updates all GUI items that are managed by this manager
	 *
	 * @param language The {@linkplain Language} that should be used to updates
	 *         the Items. Note that this does not replace the instance that is
	 *         used to give the initial translation when a item is added
	 * */
	public void updateGUI(Language language) {
		updateMenuItems(language);
	}

	/**
	 * This method updates all {@linkplain JMenuItem}s that are managed by this manager.
	 *
	 * @param language The {@linkplain Language} that should be used to updates
	 *         the Items. Note that this does not replace the instance that is
	 *         used to give the initial translation when a item is added
	 * */
	public void updateMenuItems(Language language) {
		for (JMenuItem item : this.menuItems) {
			String key = item.getName();
			item.setText(language.getString(key));
		}
	}

	/**
	 * This adds the given {@linkplain JMenuItem} to the manages items list.
	 * It also sets the text of the item to the current String that is stored under
	 * the given key.
	 *
	 * <p>Note that this uses the name of the item to store the language key.</p>
	 *
	 * @param item The {@linkplain JMenuItem} that should be managed.
	 * @param languageKey The key that is used to get the items translation.
	 * */
	public void add(JMenuItem item, String languageKey) {
		item.setName(languageKey);
		this.menuItems.add(item);
		item.setText(this.language.getString(languageKey));
	}

}
