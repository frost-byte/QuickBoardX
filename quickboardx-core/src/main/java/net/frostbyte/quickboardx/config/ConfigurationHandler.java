package net.frostbyte.quickboardx.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@SuppressWarnings( { "unused", "WeakerAccess", "ResultOfMethodCallIgnored" })
public abstract class ConfigurationHandler
{
	protected String fileName;
	protected String filePath;
	protected boolean readFile;

	protected Logger logger;
	protected File dataFolder;

	protected File file;
	protected YamlConfiguration config;
	protected boolean isDirty;

	public ConfigurationHandler(
		File dataFolder,
		Logger logger,
		String filePath,
		String fileName,
		boolean readFile
	) {
		this.logger = logger;
		this.dataFolder = dataFolder;
		this.filePath = filePath;
		this.fileName = fileName;
		this.readFile = readFile;
		this.isDirty = false;
	}

	public ConfigurationHandler(
		File dataFolder,
		Logger logger,
		String filePath,
		String fileName
	) {
		this(dataFolder, logger, filePath, fileName, false);
	}

	public ConfigurationHandler(
		File dataFolder,
		Logger logger,
		String fileName
	) {
		this(dataFolder, logger, null, fileName, false);
	}

	public void init()
	{
		if (readFile)
			loadFile();
		else
			file = createFile(filePath, fileName);

		config = loadConfig();
		isDirty = false;
	}

	public boolean shouldSave() { return isDirty;}
	public void reloadConfig()
	{
		this.config = loadConfig();
	}

	protected abstract void addDefaults();

	protected void loadFile()
	{
		String fullPath = dataFolder.getPath() + File.separator;

		if (filePath != null)
			fullPath += filePath + File.separator;

		fullPath += fileName + ".yml";
		file = new File(fullPath);

		if (!file.exists())
		{
			logger.warning("ConfigurationHandler.loadFile: Error! File does not exist!");
		}
	}

	public boolean fileExists() {
		return file != null && file.exists();
	}

	/**
	 * Creates a new configuration file if it doesn't exist.
	 */
	protected File createFile(String filePath, String fileName)
	{
		String fullPath = dataFolder.getPath() + File.separator;

		if (filePath != null)
			fullPath += filePath + File.separator;

		File file = new File(fullPath + fileName + ".yml");
		file.getParentFile().mkdirs();

		try
		{
			file.createNewFile();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return file;
	}

	/**
	 * Returns the Yaml configuration instance of the file.
	 */
	protected YamlConfiguration loadConfig()
	{
		return YamlConfiguration.loadConfiguration(file);
	}

	/**
	 * Saves all changes to the player data file.
	 */
	protected void saveConfig(boolean addDefaults)
	{
		if (addDefaults)
			config.options().copyDefaults(true);

		try
		{
			config.save(file);

			if (isDirty)
				isDirty = false;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void saveConfig()
	{
		saveConfig(false);
	}
}
