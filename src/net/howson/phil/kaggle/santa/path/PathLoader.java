package net.howson.phil.kaggle.santa.path;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gnu.trove.list.array.TIntArrayList;

public class PathLoader {

	private static final Logger logger = LogManager.getLogger(PathLoader.class);

	public int[] load(final File fls) throws FileNotFoundException, IOException {

		final TIntArrayList x = new TIntArrayList();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new BufferedInputStream(new FileInputStream(fls))))) {
			reader.readLine();
			String s;
			while ((s = reader.readLine()) != null) {

				x.add(Integer.valueOf(s));

			}

		}
		return x.toArray(1, x.size() - 2);
	}
}
