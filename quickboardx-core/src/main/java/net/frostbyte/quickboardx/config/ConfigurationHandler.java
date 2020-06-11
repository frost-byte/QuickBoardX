package net.frostbyte.quickboardx.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/** Abstraction for loading Custom Yaml Configuration files using the
 * Bukkit Configuration API.
 * @author frost-byte
 * @since 1.0.0
 */
@SuppressWarnings( { "unused", "WeakerAccess", "ResultOfMethodCallIgnored" })
public abstract class ConfigurationHandler
{
	/**
	 * The base name of the configuration file. (Without the extension or path)
	 */
	protected String fileName;

	/**
	 * The path for the file within the plugin's data folder; includes subdirectories, file name and file extension.
	 */
	protected String filePath;

	/**
	 * Indicates whether the file should be read (that it should already exists)
	 * Indicates that the file should be created when it is false.
	 */
	protected boolean readFile;

	protected Logger logger;

	/**
	 * The folder where the configuration is stored - i.e. the plugin's datafolder
	 */
	protected File dataFolder;

	/**
	 * The File I/O Instance of the Configuration
	 */
	protected File file;

	/**
	 * The Configuration
	 */
	protected YamlConfiguration config;

	/**
	 * Indicates if a property in the config has been updated and needs to be
	 * saved to disk.
	 */
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

	/**
	 * Initialize the Configuration, will read from an existing file, if readFile is true,
	 * or else create a new configuration file. Then the configuration is loaded into memory.
	 */
	public void init()
	{
		if (readFile)
			loadFile();
		else
			file = createFile(filePath, fileName);

		config = loadConfig();
		isDirty = false;
	}

	/**
	 * @return Does the file need to be saved to Disk after a configuration update or change?
	 */
	public boolean shouldSave() { return isDirty;}

	/**
	 * Reload the Configuration from disk
	 */
	public void reloadConfig()
	{
		this.config = loadConfig();
	}

	/**
	 * Add default configuration values to the config.
	 */
	protected abstract void addDefaults();

	/**
	 * Load the Configuration File
	 */
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

	/**
	 * @return Has the configuration file been loaded and does it exist on disk?
	 */
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

		try
		{
			File file = new File(fullPath + fileName + ".yml");
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		catch (NullPointerException | IOException | SecurityException e)
		{
			e.printStackTrace();
		}

		return file;
	}

	/**
	 * Loads and provides the Yaml configuration instance.
	 */
	protected YamlConfiguration loadConfig()
	{
		return YamlConfiguration.loadConfiguration(file);
	}

	/**
	 * Saves all values of the Config from memory to the file on disk.
	 * @param addDefaults Should the default values be added to the config?
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

	/**
	 * Save the Configuration to Disk
	 */
	public void saveConfig()
	{
		saveConfig(false);
	}
}
