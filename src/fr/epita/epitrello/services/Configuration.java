package fr.epita.epitrello.services;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class Configuration {
	private static Properties configurationProperties = new Properties();
	private static boolean isInit = false;

	private static void init() {
		try {
			configurationProperties.load(new FileInputStream("conf.properties"));

		} catch (Exception e) {
			System.out.println(e.getMessage());
			Logger.getLogger(e.getMessage());
		}
	}

	public static String getValueFromKey(String key) {
		if (!isInit) {
			init();
			isInit = true;
		}
		return configurationProperties.getProperty(key);
	}
}