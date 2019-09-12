package net.frostbyte.quickboardx.tasks;

import net.frostbyte.quickboardx.PlayerBoard;
import net.frostbyte.quickboardx.managers.BaseBoardManager;

public class UpdateTextTask implements Runnable
{
	private PlayerBoard playerBoard;
	private BaseBoardManager boardManager;

	public UpdateTextTask(PlayerBoard playerBoard, BaseBoardManager boardManager)
	{
		this.playerBoard = playerBoard;
		this.boardManager = boardManager;
	}

	@Override
	public void run()
	{
		boardManager.updateText(playerBoard);
	}
}
