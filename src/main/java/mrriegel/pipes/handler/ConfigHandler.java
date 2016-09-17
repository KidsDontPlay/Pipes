package mrriegel.pipes.handler;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {

	public static Configuration config;

	public static boolean energyNeeded;

	public static void refreshConfig(File file) {
		config = new Configuration(file);
		config.load();
		config.getBoolean("energyNeeded", Configuration.CATEGORY_GENERAL, true, "");

		if (config.hasChanged()) {
			config.save();
		}
	}

}
