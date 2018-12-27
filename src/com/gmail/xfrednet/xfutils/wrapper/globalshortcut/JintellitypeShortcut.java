package com.gmail.xfrednet.xfutils.wrapper.globalshortcut;

import com.gmail.xfrednet.xfutils.Main;
import com.gmail.xfrednet.xfutils.wrapper.GlobalShortcut;
import com.gmail.xfrednet.xfutils.wrapper.IGlobalShortcutListener;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

import java.awt.*;
import java.util.ArrayList;

public class JintellitypeShortcut extends GlobalShortcut implements HotkeyListener {

	private int identifierCounter = 0;
	private ArrayList<IGlobalShortcutListener> listeners = new ArrayList<>();

	public JintellitypeShortcut() {
		JIntellitype.getInstance().addHotKeyListener(this);
	}

	public static boolean IsSupported() {
		return JIntellitype.isJIntellitypeSupported();
	}

	private int getNewIdentifier(IGlobalShortcutListener listener) {
		this.listeners.add(listener);
		return this.identifierCounter++;
	}

	@Override
	public int registerGlobalShortcut(int eventMask, int key, IGlobalShortcutListener listener) {
		int identifier = getNewIdentifier(listener);

		int intellMask = 0x00000000;
		intellMask |= ((eventMask & Event.SHIFT_MASK) != 0) ? JIntellitype.MOD_SHIFT   : 0;
		intellMask |= ((eventMask & Event.CTRL_MASK)  != 0) ? JIntellitype.MOD_CONTROL : 0;
		intellMask |= ((eventMask & Event.META_MASK)  != 0) ? JIntellitype.MOD_WIN     : 0;
		intellMask |= ((eventMask & Event.ALT_MASK)   != 0) ? JIntellitype.MOD_ALT     : 0;

		JIntellitype.getInstance().registerHotKey(identifier, intellMask, key);

		Main.Logger.logDebugMessage(
			"GlobalShortcut.RegisterGlobalShortcut: Registered a new Shortcut: identifier: " + identifier +
				", Mask: " + eventMask +
				", Key " + key);

		return identifier;
	}

	@Override
	public void unregisterGlobalShortcut(int identifier) {
		JIntellitype.getInstance().unregisterHotKey(identifier);
		if (identifier > 0 || identifier < this.listeners.size()) {
			this.listeners.set(identifier, null);
		}

		Main.Logger.logDebugMessage(
			"GlobalShortcut.UnregisterGlobalShortcut: Removed the Shortcut with the Identifier: "
			+ identifier);
	}

	@Override
	public void cleanUp() {
		JIntellitype.getInstance().cleanUp();
	}

	@Override
	public void onHotKey(int identifier) {
		if (identifier < 0 || identifier >= this.listeners.size()) {
			return;
		}

		IGlobalShortcutListener listener = this.listeners.get(identifier);
		if (listener != null)
			listener.activated(identifier);
	}
}
