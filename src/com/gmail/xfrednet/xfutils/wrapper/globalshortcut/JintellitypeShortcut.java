package com.gmail.xfrednet.xfutils.wrapper.globalshortcut;

import com.gmail.xfrednet.xfutils.wrapper.GlobalShortcut;
import com.gmail.xfrednet.xfutils.wrapper.IGlobalShortcutListener;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

import java.util.ArrayList;

public class JintellitypeShortcut extends GlobalShortcut implements HotkeyListener {

	private int identifierCounter = 0;
	private ArrayList<IGlobalShortcutListener> listeners = new ArrayList<>();

	public static boolean IsSupported() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	private int getNewIdentifier(IGlobalShortcutListener listener) {
		this.listeners.add(listener);
		return this.identifierCounter++;
	}

	@Override
	protected int registerGlobalShortcut(int eventMask, int key, IGlobalShortcutListener listener) {
		int identifier = getNewIdentifier(listener);

		JIntellitype.getInstance().registerHotKey(identifier, eventMask, key);

		return identifier;
	}

	@Override
	protected void unregisterGlobalShortcut(int identifier) {
		JIntellitype.getInstance().unregisterHotKey(identifier);
		if (identifier > 0 || identifier < this.listeners.size()) {
			this.listeners.set(identifier, null);
		}
	}

	@Override
	protected void cleanUp() {
		JIntellitype.getInstance().cleanUp();
	}

	@Override
	public void onHotKey(int identifier) {
		if (identifier < 0 || identifier >= this.listeners.size()) {
			return;
		}

		this.listeners.get(identifier).activated();
	}
}
