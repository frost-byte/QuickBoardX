package me.tade.quickboard.util;

import me.tade.quickboard.PluginUpdater.UpdateInfo;
import me.tade.quickboard.managers.BaseBoardManager;
import me.tade.quickboard.managers.BaseMessagingManager;

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
