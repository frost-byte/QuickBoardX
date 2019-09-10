package net.frostbyte.quickboardx.util;

import net.frostbyte.quickboardx.PluginUpdater.UpdateInfo;
import net.frostbyte.quickboardx.managers.BaseBoardManager;
import net.frostbyte.quickboardx.managers.BaseMessagingManager;

import java.util.UUID;

@SuppressWarnings({"unused"})
public interface VersionBridge
{
	String getUpdateMessage(UpdateInfo updateInfo);
	String togglePlayerBoard(UUID playerID);
	String getIntroMessage(String version, boolean papiEnabled, boolean metricsEnabled);
	String getWelcomeMessage(String playerName);
	BaseMessagingManager getMessagingManager();
	BaseBoardManager getBoardManager();
}
