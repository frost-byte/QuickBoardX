package me.tade.quickboard.util;

import me.tade.quickboard.QuickBoard;

public interface BinderBridge
{
	void createBridge(QuickBoard plugin);
	void injectMembers(Object o);
	<T> T getInstance(Class<T> var1);
}
