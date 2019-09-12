package net.frostbyte.quickboardx.tasks;

import net.frostbyte.quickboardx.PlayerBoard;
import net.frostbyte.quickboardx.managers.BaseBoardManager.BoardTask;

public class UpdateScrollerTask implements Runnable
{
	private PlayerBoard playerBoard;
	private final String key;

	public UpdateScrollerTask(BoardTask boardTask)
	{
		this.playerBoard = boardTask.getPlayerBoard();
		this.key = boardTask.getOperation().getKey();
	}

	@Override
	public void run()
	{
		playerBoard.updateScrollerText(key);
	}
}
