package net.frostbyte.quickboardx.tasks;

import net.frostbyte.quickboardx.PlayerBoard;
import net.frostbyte.quickboardx.managers.BaseBoardManager.BoardOperation;
import net.frostbyte.quickboardx.managers.BaseBoardManager.BoardTask;

public class UpdateChangeableTask implements Runnable
{
	private int changeSize;
	private int idx = 0;
	private final String key;
	private PlayerBoard playerBoard;

	public UpdateChangeableTask(BoardTask boardTask)
	{
		this.playerBoard = boardTask.getPlayerBoard();
		BoardOperation operation = boardTask.getOperation();
		this.key = operation.getKey();
		this.changeSize = operation.getTotalElements();
	}

	@Override
	public void run()
	{
		idx = (idx + 1) % changeSize;
		if (idx >= changeSize)
			idx = 0;

		try
		{
			playerBoard.updateChangeable(key, idx);
		}
		catch (Exception ignored) {}
	}
}
