package me.tade.quickboard.v1_12_R1.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.tade.quickboard.PluginUpdater.UpdateInfo;
import me.tade.quickboard.managers.BaseBoardManager;
import me.tade.quickboard.managers.BaseMessagingManager;
import me.tade.quickboard.util.VersionBridge;
import me.tade.quickboard.v1_12_R1.managers.BoardManager;
import me.tade.quickboard.v1_12_R1.managers.MessagingManager;

import java.util.UUID;

@SuppressWarnings("unused")
@Singleton
public class BridgeImpl implements VersionBridge
{
	private final MessagingManager messagingManager;
	private final BoardManager boardManager;

	@Inject
	BridgeImpl(
		MessagingManager messagingManager,
		BoardManager boardManager
	)
	{
		this.messagingManager = messagingManager;
		this.boardManager = boardManager;
	}
	@Override
	public String getUpdateMessage(UpdateInfo updateInfo)
	{
		return messagingManager.getUpdateMessage(updateInfo);
	}

	@Override
	public String togglePlayerBoard(UUID playerID)
	{
		return messagingManager.togglePlayerBoard(playerID);
	}

	@Override
	public String getIntroMessage(String version, boolean papiEnabled, boolean metricsEnabled)
	{
		return messagingManager.getIntroMessage(version, papiEnabled, metricsEnabled);
	}

	@Override
	public String getWelcomeMessage(String playerName)
	{
		return messagingManager.getWelcomeMessage(playerName);
	}

	@Override
	public BaseMessagingManager getMessagingManager()
	{
		return messagingManager;
	}

	@Override
	public BaseBoardManager getBoardManager()
	{
		return boardManager;
	}
}
