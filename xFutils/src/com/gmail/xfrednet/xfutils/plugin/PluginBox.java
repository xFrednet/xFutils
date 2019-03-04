package com.gmail.xfrednet.xfutils.plugin;

class PluginBox {
	
	private IPlugin plugin;
	
	private boolean networkAccessAllowed         = false;
	private boolean threadAccessAllowed          = false;
	private boolean awtAccessAllowed             = false;
	private boolean globalFileAccessAllowed      = false;
	
	private boolean createClassLoaderAllowed     = false;
	private boolean linkLibraryAllowed           = false;
	private boolean printJobAccessAllowed        = false;
	private boolean checkPropertiesAccessAllowed = false;
	
	PluginBox(IPlugin plugin) {
		//TODO error check 
		this.plugin = plugin;
	}
	
	private SecurityManager createSecuritymanager() {
		SecurityManager m = new SecurityManager();
		
		
		return null;
	}
	
}
