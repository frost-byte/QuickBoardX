package net.frostbyte.quickboardx.v1_14_R1.util;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import net.frostbyte.quickboardx.QuickBoardX;
import net.frostbyte.quickboardx.managers.BaseBoardManager;
import net.frostbyte.quickboardx.managers.BaseMessagingManager;
import net.frostbyte.quickboardx.util.BinderBridge;
import net.frostbyte.quickboardx.v1_14_R1.managers.BoardManager;
import net.frostbyte.quickboardx.v1_14_R1.managers.MessagingManager;

import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("unused")
public class BinderModuleImpl implements BinderBridge
{
	private Injector injector;

	public BinderModuleImpl() {}

	@Override
	public void createBridge(QuickBoardX plugin)
	{
		checkNotNull(plugin, "The plugin instance cannot be null.");

		injector = new AbstractModule()
		{
			@Override
			protected void configure()
			{
				bind(QuickBoardX.class).toInstance(plugin);
				bind(BaseMessagingManager.class).to(MessagingManager.class);
				bind(BaseBoardManager.class).to(BoardManager.class);
				bind(Logger.class)
					.annotatedWith(Names.named("QuickBoard"))
					.toInstance(plugin.getLogger());
			}

			/**
			 * Generate the Guice Injector for this Module
			 *
			 * @return The guice injector used to retrieve bound instances and create new instances based upon the
			 * implementations bound to their specified contract class
			 */
			Injector createInjector()
			{
				injector = Guice.createInjector(this);
				return injector;
			}

		}.createInjector();
		injector.injectMembers(plugin);
	}

	@Override
	public void injectMembers(Object o)
	{
		injector.injectMembers(o);
	}

	@Override
	public <T> T getInstance(Class<T> var1)
	{
		return injector.getInstance(var1);
	}
}
