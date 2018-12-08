package net.howson.phil.kaggle.santa;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PathWriter {

	private static final Logger logger = LogManager.getLogger(PathWriter.class);

	public synchronized void save(final int[] path, final File file) throws FileNotFoundException {
		try (PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));) {

			ps.println("Path");
			ps.println("0");
			for (int i = 0; i < path.length; ++i) {
				ps.println(path[i]);
			}
			ps.println("0");

		}

	}
}
