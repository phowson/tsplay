package net.howson.phil.kaggle.santa.map;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gnu.trove.list.array.TDoubleArrayList;

public class MapLoader {

	private static final Logger logger = LogManager.getLogger(MapLoader.class);

	private final Pattern p = Pattern.compile(",");

	public WorldMap load(final File fls) throws FileNotFoundException, IOException {

		final TDoubleArrayList x = new TDoubleArrayList();
		final TDoubleArrayList y = new TDoubleArrayList();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new BufferedInputStream(new FileInputStream(fls))))) {
			reader.readLine();
			String s;
			while ((s = reader.readLine()) != null) {
				final String parts[] = p.split(s);
				x.add(Double.valueOf(parts[1]));
				y.add(Double.valueOf(parts[2]));
			}

		}
		return new WorldMap(x.toArray(), y.toArray());
	}

}
