package net.frostbyte.quickboardx.util;

import net.frostbyte.quickboardx.QuickBoardX;

public interface BinderBridge
{
	void createBridge(QuickBoardX plugin);
	void injectMembers(Object o);
	<T> T getInstance(Class<T> var1);
}
