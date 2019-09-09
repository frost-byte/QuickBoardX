package me.tade.quickboard.util;

import me.tade.quickboard.PluginUpdater.UpdateInfo;
import me.tade.quickboard.QuickBoard;
import me.tade.quickboard.managers.BaseBoardManager;
import me.tade.quickboard.managers.BaseMessagingManager;
import org.bukkit.Bukkit;

import java.util.UUID;

@SuppressWarnings("unused")
public class VersionManager
{
	@SuppressWarnings("FieldCanBeLocal")
	private static VersionBridge BRIDGE;
	private static String MINECRAFT_REVISION;
	private static BinderBridge BINDER;
	private static final String PACKAGE_HEADER = "me.tade.quickboard.v";

	private VersionManager() {}

	public static void loadBridge() throws Exception {

		String className = PACKAGE_HEADER + getMinecraftRevision() + ".util.BridgeImpl";
		BRIDGE = (VersionBridge) getInstance(Class.forName(className));
	}

	public static void loadBinderModule(
		QuickBoard plugin
	) throws Exception
	{
		String className = PACKAGE_HEADER + getMinecraftRevision() + ".util.BinderModuleImpl";
		BINDER = (BinderBridge) Class.forName(className).getConstructor().newInstance();
		BINDER.createBridge(plugin);
	}

	@SuppressWarnings("WeakerAccess")
	public static <T> T getInstance(Class<T> var1)
	{
		return BINDER.getInstance(var1);
	}

	public static void injectMembers(Object o)
	{
		BINDER.injectMembers(o);
	}

	@SuppressWarnings("WeakerAccess")
	public static String getMinecraftRevision() {
		if (MINECRAFT_REVISION == null) {
			MINECRAFT_REVISION = Bukkit.getServer().getClass().getPackage().getName();
		}
		return MINECRAFT_REVISION.substring(MINECRAFT_REVISION.lastIndexOf('.') + 2);
	}

	public static String getUpdateMessage(UpdateInfo updateInfo)
	{
		return BRIDGE.getUpdateMessage(updateInfo);
	}

	public static String togglePlayerBoard(UUID playerID)
	{
		return BRIDGE.togglePlayerBoard(playerID);
	}

	public static String getIntroMessage(String version, boolean papiEnabled, boolean metricsEnabled)
	{
		return BRIDGE.getIntroMessage(version, papiEnabled, metricsEnabled);
	}

	public static String getWelcomeMessage(String playerName)
	{
		return BRIDGE.getWelcomeMessage(playerName);
	}
	public static BaseMessagingManager getMessagingManager() { return BRIDGE.getMessagingManager(); }
	public static BaseBoardManager getBoardManager()
	{
		return BRIDGE.getBoardManager();
	}
}
