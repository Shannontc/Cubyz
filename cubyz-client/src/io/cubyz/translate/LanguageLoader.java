package io.cubyz.translate;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;

import io.cubyz.CubyzLogger;
import io.cubyz.utils.ResourceManager;

public class LanguageLoader {

	public static Language load(String locale) {
		locale = Objects.requireNonNull(locale);
		File[] assetsFolders = ResourceManager.listFiles("");
		Language lang = new Language(locale);
		for (File assetFolder : assetsFolders) {
			File langFile = ResourceManager.lookup(assetFolder.getName() + "/lang/" + locale + ".lang");
			if (langFile != null) {
				Properties props = new Properties();
				try {
					FileReader reader = new FileReader(langFile);
					props.load(reader);
					for (Object key : props.keySet()) {
						lang.add(key.toString(), props.getProperty(key.toString()));
					}
					reader.close();
				} catch (IOException e) {
					CubyzLogger.instance.log(Level.SEVERE, "Could not open language file " + locale + " for mod " + assetFolder.getName(), e);
				}
			}
		}
		return lang;
	}
	
}
