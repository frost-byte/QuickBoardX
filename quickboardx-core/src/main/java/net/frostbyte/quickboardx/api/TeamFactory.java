package net.frostbyte.quickboardx.api;

import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("unused")
public interface TeamFactory
{
	Team create(
		@Assisted("name")
		String name,
		@Assisted("displayName")
		String displayName,
		@Assisted("color")
		String color,
		@Assisted("prefix")
		String prefix
	);
}
