package net.frostbyte.quickboardx.tasks;

import net.frostbyte.quickboardx.PlayerBoard;
import net.frostbyte.quickboardx.managers.BaseBoardManager.BoardTask;

public class RemoveTempTask implements Runnable
{
	private PlayerBoard playerBoard;

	public RemoveTempTask(BoardTask boardTask)
	{
		this.playerBoard = boardTask.getPlayerBoard();
	}

	@Override
	public void run()
	{
		playerBoard.removeTemporary();
	}
}
