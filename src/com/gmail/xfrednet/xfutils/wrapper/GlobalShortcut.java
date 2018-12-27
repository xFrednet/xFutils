package com.gmail.xfrednet.xfutils.wrapper;

import com.gmail.xfrednet.xfutils.Main;
import com.gmail.xfrednet.xfutils.wrapper.globalshortcut.JintellitypeShortcut;
import com.gmail.xfrednet.xfutils.wrapper.globalshortcut.NoGlobalShortcut;

/**
 * This class is a singleton class that is used to register global shortcuts. Globals shortcuts
 * are shortcuts that always work no matter what application is focused.
 * */
public abstract class GlobalShortcut {

	/* ###################################################################################### */
	// # Static #
	/* ###################################################################################### */

	/**
	 * This class is a singleton class and this is the instance of that.
	 * */
	private static GlobalShortcut instance = null;

	/**
	 * This static method initializes the instance of the GlobalShortcut. The instance has to be OS
	 * specific because java has no real way to add global shortcuts und the libraries are OS dependent.
	 *
	 * @return A instance if the {@linkplain GlobalShortcut} class.
	 */
	public static GlobalShortcut GetInstance() {
		if (instance == null) {
			if (JintellitypeShortcut.IsSupported()) {
				instance = new JintellitypeShortcut();
			} else {
				Main.Logger.logError("GetInstance: xFutils has no global shortcuts library that can work in this environment!");
				instance = new NoGlobalShortcut();
			}
		}

		return instance;
	}

	/* ###################################################################################### */
	// # Class #
	/* ###################################################################################### */

	/**
	 * This static method adds a global shortcut, the shortcut will trigger the {@linkplain IGlobalShortcutListener}
	 * when the shortcut is pressed.
	 *
	 * @param eventMask This mask defines special keys that are part of the shortcut.
	 *                  Mask values: (Event.SHIFT_MASK, Event.CTRL_MASK, Event.META_MASK and Event.ALT_MASK)
	 * @param key       The Key that that should be pressed. The key ID is the same as teh ascii code of the capital letter.
	 *                  Example: <aa>(int)'A'</aa> for the A key
	 * @param listener  This listener will be called when the shortcut was activated.
	 * @return This returns a unique ID that is used by {@linkplain #unregisterGlobalShortcut(int)} to remove the shortcut again.
	 */
	public abstract int registerGlobalShortcut(int eventMask, int key, IGlobalShortcutListener listener);

	/**
	 * This method removes the global shortcut that is bound to the identifier.
	 * @param identifier The identifier that was returned by {@linkplain #registerGlobalShortcut(int, int, IGlobalShortcutListener)}
	 */
	public abstract void unregisterGlobalShortcut(int identifier);

	/**
	 * This method cleans up everything that was made dirty by the GlobalShortcut instance. This documentation is actually
	 * obsolete since it should only be called once at the end ov everything. This is already done. I could have easily just say
	 * DO NOT CALL THIS, THIS IS CALLED AUTOMATICALLY. But what would be the fun in that??? I can think of multiple things...
	 * not it's to late. I should stop writing now........... NOW!!!!
	 *
	 * Brain:    Are you sure???
	 * xFrednet: Yes!
	 * Brain:    really?
	 * xFrednet: Yes, I could be doing way better things!
	 * Brain:    for instance?
	 * xFrednet: Why am I arguing with my brain?
	 * Brain:    Well you are me that is your job.
	 * xFrednet: Brain you can't tell me what to do!
	 * Brain:    I can!
	 * xFrednet: Nope
	 * Brain:    Why are you hitting your self.
	 * Brain:    Okay this hurts me too.
	 * xFrednet: I'm done!
	 */
	public abstract void cleanUp();
}
