package com.gmail.xfrednet.xfutils.util.language;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

public class LanguageGUIManager implements ILanguageListener{

	Language language;
	
	List<JMenuItem> menuItems;
	
	LanguageGUIManager(Language language) {
		this.language = language;
		
		this.menuItems = new ArrayList<>();
	}

	@Override
	public void onLanguageChange(Language language) {
		updateGUI(language);
	}
	
	public void updateGUI(Language language) {
		updateMenuItems(language);
	}
	
	public void updateMenuItems(Language language) {
		for (JMenuItem item : this.menuItems) {
			String key = item.getName();
			item.setText(language.getString(key));
		}
	}
	
	public void add(JMenuItem item, String languageKey) {
		item.setName(languageKey);
		this.menuItems.add(item);
		item.setText(this.language.getString(languageKey));
	}

}
