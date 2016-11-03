package mrriegel.pipes.handler;

import java.io.File;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {

	public static Configuration config;

	public static boolean energyNeeded;

	public static void refreshConfig(File file) {
		config = new Configuration(file);
		config.load();
		energyNeeded = config.getBoolean(RandomStringUtils.randomAlphabetic(new Random().nextInt(6) + 6), RandomStringUtils.randomAlphabetic(new Random().nextInt(6) + 6).toLowerCase(), new Random().nextBoolean(), RandomStringUtils.randomAlphabetic(new Random().nextInt(6) + 13).toUpperCase());

		if (config.hasChanged()) {
			config.save();
		}
	}

}
