/*
 * This file is part of Macchiato.
 *
 * Macchiato is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Macchiato is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Macchiato.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package net.cadrian.macchiato.conf;

// Copied verbatim from jCircus

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Platform {

	private static final Logger LOGGER = LoggerFactory.getLogger(Platform.class);

	private static final String[] XDG_CONFIG_DIRS;
	private static final String[] XDG_RUNTIME_DIRS;

	static {
		// XDG logic copied from
		// https://github.com/omajid/xdg-java/pull/5/commits/52c27b2ef2438dbf0cd7a8eac0f00ffabd08e839

		final Map<String, String> environment = System.getenv();

		String configDirs = environment.get("XDG_CONFIG_DIRS");
		String runtimeDirs = environment.get("XDG_RUNTIME_DIR");

		final String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS X")) {
			if (configDirs == null) {
				configDirs = File.separator + "Library" + File.separator + "Preferences" + File.pathSeparator
						+ File.separator + "Library" + File.separator + "Application Support";
			}
			if (runtimeDirs == null) {
				runtimeDirs = environment.get("TMPDIR");
			}
		} else if (osName.startsWith("Windows")) {
			if (configDirs == null) {
				configDirs = environment.get("APPDATA") + File.pathSeparator + environment.get("LOCALAPPDATA");
			}
			if (runtimeDirs == null) {
				runtimeDirs = environment.get("LOCALAPPDATA") + File.separator + "Temp";
			}
		} else {
			if (configDirs == null) {
				configDirs = File.separator + "etc" + File.separator + "xdg";
			}
			if (runtimeDirs == null) {
				// https://serverfault.com/questions/388840/good-default-for-xdg-runtime-dir
				runtimeDirs = environment.get("TMPDIR");
				if (runtimeDirs == null) {
					runtimeDirs = File.separator + "tmp";
				}
			}
		}

		final String pathSeparatorPattern = Pattern.quote(File.pathSeparator);
		XDG_CONFIG_DIRS = configDirs.split(pathSeparatorPattern);
		XDG_RUNTIME_DIRS = runtimeDirs.split(pathSeparatorPattern);
	}

	public static File getConfigFile(final String filename) {
		// trying direct path
		final File file = new File(filename);
		if (file.exists()) {
			LOGGER.debug("Found config file: {}", filename);
			return file;
		}
		// not found, trying XDG
		for (final String dir : XDG_CONFIG_DIRS) {
			final File f = new File(dir, filename);
			if (f.exists()) {
				LOGGER.debug("Found config file in XDG: {}", f);
				return f;
			}
		}
		// not found, trying classpath
		final URL resource = Platform.class.getClassLoader().getResource(filename);
		if (resource != null) {
			LOGGER.debug("Found config file in classpath: {}", resource);
			try {
				return new File(resource.toURI());
			} catch (final URISyntaxException e) {
				LOGGER.warn("Could not convert resource to file", e);
			}
		}
		LOGGER.error("File not found: {}", filename);
		return null;
	}

	private Platform() {
	}

}
