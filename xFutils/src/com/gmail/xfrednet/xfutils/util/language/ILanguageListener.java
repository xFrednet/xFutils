package com.gmail.xfrednet.xfutils.util.language;

/**
 * This interface can be used to receive updates when the language chnages
 * */
public interface ILanguageListener {
	
	/**
	 * This method will be called when the ILanguageListener is bound to the
	 * language and the language changes.
	 * */
	public void onLanguageChange(Language language);
	
}
