package com.gmail.xfrednet.xfutils.wrapper.globalshortcut;

import com.gmail.xfrednet.xfutils.wrapper.GlobalShortcut;
import com.gmail.xfrednet.xfutils.wrapper.IGlobalShortcutListener;

public class NoGlobalShortcut extends GlobalShortcut {
	@Override
	public int registerGlobalShortcut(int eventMask, int key, IGlobalShortcutListener listener) {
		return -1;
	}

	@Override
	public void unregisterGlobalShortcut(int identifier) {}

	@Override
	public void cleanUp() {}
}
