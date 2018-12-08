package net.howson.phil.kaggle.santa;

import java.io.File;
import java.io.FileNotFoundException;

import net.howson.phil.kaggle.santa.path.Path;

public class BestPathSoFar {

	private Path bestPath;

	public BestPathSoFar(final Path bestPath) {
		this.bestPath = bestPath;
	}

	public synchronized Path get() {
		return bestPath;
	}

	public synchronized void update(final int[] path, final double x) {
		if (x < bestPath.length) {
			System.out.println(
					System.currentTimeMillis() + " Updated best path " + x + " " + Thread.currentThread().getName());
			bestPath = Path.copy(path, x);
			try {
				new PathWriter().save(bestPath.steps, new File("./data/out.csv"));
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			}

		}

	}

}