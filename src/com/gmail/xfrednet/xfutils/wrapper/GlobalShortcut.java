package com.gmail.xfrednet.xfutils.wrapper;

import com.gmail.xfrednet.xfutils.Main;
import com.gmail.xfrednet.xfutils.wrapper.globalshortcut.JintellitypeShortcut;
import com.gmail.xfrednet.xfutils.wrapper.globalshortcut.NoGlobalShortcut;

public abstract class GlobalShortcut {

	//TODO actionListener

	/* ###################################################################################### */
	// # Static #
	/* ###################################################################################### */
	private static GlobalShortcut instance = null;

	private static void InitInstance() {
		if (instance != null) {
			// IT's late... well y logs are always this useful so,,,
			Main.Logger.logError("InitInstance: Someone f**ked up, Someone is getting fired!");
			return;
		}

		if (JintellitypeShortcut.IsSupported()) {
			instance = new JintellitypeShortcut();
		} else {
			instance = new NoGlobalShortcut();
		}
	}

	public static int RegisterGlobalShortcut(int eventMask, int key, IGlobalShortcutListener listener) {
		if (instance == null) {
			InitInstance();
		}

		int identifier = instance.registerGlobalShortcut(eventMask, key, listener);
		Main.Logger.logDebugMessage(
			"GlobalShortcut.RegisterGlobalShortcut: Registered a new Shortcut: identifier: " + identifier +
				", Mask: " + eventMask +
				", Key " + key);
		return identifier;
	}

	public static void UnregisterGlobalShortcut(int identifier) {
		if (instance == null) {
			Main.Logger.logAlert("GlobalShortcut.UnregisterGlobalShortcut: I have so many questions, but no one to answer them :/");
			return;
		}

		instance.unregisterGlobalShortcut(identifier);
		Main.Logger.logDebugMessage("GlobalShortcut.UnregisterGlobalShortcut: Removed the Shortcut with the Identifier: " + identifier);
	}

	public static void CleanUp() {
		if (instance != null) {
			instance.cleanUp();
			instance = null;
		}
		Main.Logger.logInfo("GlobalShortcut: I cleaned up my mess.");
	}

	/* ###################################################################################### */
	// # Class #
	/* ###################################################################################### */

	protected abstract int registerGlobalShortcut(int eventMask, int key, IGlobalShortcutListener listener);
	protected abstract void unregisterGlobalShortcut(int identifier);
	protected abstract void cleanUp();
}
