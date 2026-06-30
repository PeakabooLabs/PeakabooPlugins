package org.peakaboo.plugins.launcher;

import org.peakaboo.tier.Tier;
import org.peakaboo.ui.swing.Peakaboo;

/**
 * Launches the full Peakaboo Swing application with every plugin in this repo available.
 * <p>
 * The custom {@link PluginsTier} must be installed before {@link Peakaboo#init()} runs,
 * because {@link Tier#setProvider} refuses to replace an already-initialized provider.
 */
public class PluginsLauncher {

	public static void main(String[] args) {
		Tier.setProvider(new PluginsTier());
		Peakaboo.init();
		Peakaboo.run();
	}

}
