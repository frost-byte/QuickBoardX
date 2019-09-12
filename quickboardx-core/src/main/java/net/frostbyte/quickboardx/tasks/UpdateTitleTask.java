package net.frostbyte.quickboardx.tasks;


import net.frostbyte.quickboardx.PlayerBoard;
import net.frostbyte.quickboardx.managers.BaseBoardManager.BoardOperation;
import net.frostbyte.quickboardx.managers.BaseBoardManager.BoardTask;

public class UpdateTitleTask implements Runnable
{
	private PlayerBoard playerBoard;
	private int index = 0;
	private int size;

	public UpdateTitleTask(BoardTask boardTask) {
		this.playerBoard = boardTask.getPlayerBoard();
		BoardOperation operation = boardTask.getOperation();
		this.size = operation.getTotalElements();
	}

	@Override
	public void run()
	{
		if (playerBoard.canUpdate())
		{
			if (++index >= size)
				index = 0;
			try
			{
				playerBoard.updateTitle(index);
			}
			catch (Exception ignored) {}
		}
	}
}
