package org.peakaboo.plugins.launcher;

import java.io.File;
import java.util.ServiceLoader;
import java.util.logging.Level;

import org.peakaboo.app.PeakabooPluginRegistry;
import org.peakaboo.dataset.source.plugin.DataSourcePlugin;
import org.peakaboo.dataset.source.plugin.DataSourceRegistry;
import org.peakaboo.framework.accent.log.OneLog;
import org.peakaboo.framework.bolt.plugin.java.BoltJavaPlugin;
import org.peakaboo.framework.bolt.plugin.java.loader.BoltJavaBuiltinLoader;
import org.peakaboo.framework.bolt.repository.BuiltinPluginRepository;
import org.peakaboo.tier.BasicTierProvider;

/**
 * A {@link org.peakaboo.tier.TierProvider} that runs the standard (basic) Peakaboo
 * application, but additionally registers every plugin found on the classpath. This lets
 * the plugins in this repo be exercised against a live Peakaboo without first packaging
 * them as jars and installing them into the user's plugin directory.
 * <p>
 * Plugins are discovered via Java {@link ServiceLoader}, using the
 * {@code META-INF/services} declarations each plugin module already ships (the same
 * mechanism Bolt uses when loading external plugin jars). Discovered classes are handed
 * to a {@link BoltJavaBuiltinLoader} so they appear as ordinary built-in plugins.
 */
public class PluginsTier extends BasicTierProvider {

	@Override
	public void initializePlugins(File pluginsRoot) {
		// Set up the standard registries (and their built-in plugins) first.
		super.initializePlugins(pluginsRoot);

		// Then layer in everything declared on the classpath. Currently every plugin in
		// this repo is a DataSourcePlugin; add further extension points here as needed.
		registerClasspathPlugins(DataSourceRegistry.system(), DataSourcePlugin.class);

		// super.initializePlugins() already built the plugin repositories, and the
		// BuiltinPluginRepository cached the registry contents *before* we added our
		// plugins above. Without this the Plugins manager/browser UI shows a stale list.
		// Refresh only the builtin repositories so we don't trigger a network fetch from
		// the remote repository at startup.
		for (var repo : getPluginRepositories().getRepositories()) {
			if (repo instanceof BuiltinPluginRepository) {
				repo.refresh();
			}
		}
	}

	/**
	 * Discovers all classpath implementations of {@code iface} via {@link ServiceLoader}
	 * and registers them with the given registry.
	 */
	private static <T extends BoltJavaPlugin> void registerClasspathPlugins(
			PeakabooPluginRegistry<T> registry, Class<T> iface) {

		var loader = new BoltJavaBuiltinLoader<>(registry, iface);
		// Load against this class's own classloader (the one that has our dependencies on
		// it) rather than relying on the thread context classloader, which can differ
		// depending on how the app is launched. Provider.type() yields the implementation
		// Class without instantiating it.
		int[] count = {0};
		ServiceLoader.load(iface, PluginsTier.class.getClassLoader())
				.stream()
				.forEach(provider -> {
					loader.load(provider.type());
					count[0]++;
				});
		registry.addLoader(loader);
		registry.reload();
		OneLog.log(Level.INFO, "Registered " + count[0] + " classpath " + iface.getSimpleName()
				+ " plugin(s); registry now holds " + registry.getPlugins().size());
	}

}
