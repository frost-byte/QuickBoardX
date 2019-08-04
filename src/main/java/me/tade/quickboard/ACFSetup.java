package me.tade.quickboard;

import co.aikar.commands.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.util.logging.Logger;


@Singleton
public class ACFSetup
{
	private final BukkitCommandManager commandManager;
	private final Logger logger;

	@SuppressWarnings( { "FieldCanBeLocal", "unused" })
	private final QuickBoard plugin;

	@Inject
	public ACFSetup(
		BukkitCommandManager commandManager,
		QuickBoard plugin,
		@Named("QuickBoard") Logger logger
	) {
		this.commandManager = commandManager;
		this.logger = logger;
		this.plugin = plugin;
	}

	/**
	 * Defines Completions which can be referenced by any Command registered with ACF,
	 * which allows the user to use the tab key to provide suggestions or complete
	 * partially typed input
	 */
	void registerCommandCompletions()
	{
		logger.info("ACF Setup: Registering command completions");

		CommandCompletions<BukkitCommandCompletionContext> comp = commandManager.getCommandCompletions();

		comp.registerCompletion("boards", c -> plugin.getInfo().keySet());
	}

	/**
	 * Provides ACF with contexts for converting string input to commands into
	 * object instances, based upon various annotations, Flags, for example.
	 * Compares the parameters defined for a command and converts the input strings
	 * provided by the command issuer into a relevant object instance.
	 * <p>
	 * Specifying an IssuerAware context allows the CommandSender or initiator of the
	 * command to be treated as an instance of a specific object.
	 */
	void registerCommandContexts()
	{
		logger.info("ACF Setup: Registering command contexts");

		CommandContexts<BukkitCommandExecutionContext> con = commandManager.getCommandContexts();

		// Converts the input string to an integer
		con.registerContext(int.class, c -> {
			int number;

			try
			{
				number = Integer.parseInt(c.popFirstArg());
			}
			catch (NumberFormatException e)
			{
				throw new InvalidCommandArgument("Invalid number format.");
			}

			return number;
		});
	}

	/**
	 * Register Replacements with ACF
	 * Defines an alias for a widely used permission or string, that can be referenced when
	 * defining the permissions for a command/subcommands registered with ACF.
	 * The obvious benefit of using this, instead of just referring to them literally in all places,
	 * is to use the "%player" replacement instead of having to change "ve.player" everywhere. This way you change it
	 * in one spot.
	 */
	void registerCommandReplacements()
	{
		// logger.info("ACF Setup: Registering command replacements");

		// CommandReplacements rep = commandManager.getCommandReplacements();

		// Permissions Replacements
		//		rep.addReplacement("%player","ve.player");
		//		rep.addReplacement("%moderator","ve.mod");
		//		rep.addReplacement("%admin","ve.admin");
	}
}
