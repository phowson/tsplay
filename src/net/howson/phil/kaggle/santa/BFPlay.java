package net.howson.phil.kaggle.santa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.map.MapLoader;
import net.howson.phil.kaggle.santa.map.WorldMap;
import net.howson.phil.kaggle.santa.path.Path;
import net.howson.phil.kaggle.santa.path.PathLoader;

public class BFPlay implements Runnable {

	private static final Logger logger = LogManager.getLogger(BFPlay.class);

	private final WorldMap map;

	private final BruteForcePath bfp;

	private final int width;
	private final int[] pathSection;

	private final BestPathSoFar bpsf;

	private final int[] path;

	private int start;

	private int end;

	private int step;

	public BFPlay(final WorldMap map2, final BestPathSoFar bpsf, final int[] startPath, final int width, int start,
			int end, int step) throws FileNotFoundException, IOException {
		this.map = map2;
		this.bpsf = bpsf;
		this.width = width;
		bfp = new BruteForcePath(map2, width);
		path = new int[startPath.length];
		pathSection = new int[width];
		System.arraycopy(startPath, 0, path, 0, startPath.length);

		this.start = start;
		this.end = end;
		this.step = step;
	}

	public static void main(final String[] args) throws FileNotFoundException, IOException {
		final WorldMap map = new MapLoader().load(new File("./data/cities.csv"));
		// final int[] path = new PathLoader().load(new
		// File("./data/good_concorde_start_point.csv"));
		final int[] path = new PathLoader().load(new File("./data/out.csv"));
		final double origDist = map.pathDistanceRoundTripToZero(path);
		System.out.println("Starting distance :" + origDist);
		final Path bestPath = new Path(path, origDist);

		final BestPathSoFar bpsf = new BestPathSoFar(bestPath);

		int segSize = path.length / 4;
		for (int width = 9; width < 13; ++width) {
			System.out.println("Width : " + width);
			List<Thread> threads = new ArrayList<>();
			for (int i = 0; i < 4; ++i) {
				Thread t = new Thread(
						new BFPlay(map, bpsf, path, width, i * segSize + 1, ((i + 1) * segSize), 1), "t-" + i);
				t.start();
				threads.add(t);

			}

			for (Thread t : threads) {
				try {
					t.join();
				} catch (InterruptedException e) {
					logger.error("Unexpected exception", e);
				}
			}
		}

	}

	@Override
	public void run() {

		double origDist = map.pathDistanceRoundTripToZero(path);

		// while (true) {
		// // int j = r.nextInt(path.length);
		// // j = j / 10;
		// // j = j * 10 - 5;
		// // if (j < 2) {
		// // j = 2;
		// // }

		boolean repeat;
		do {
			repeat = false;
			int n = end - width - 1;
			for (int j = start; j < n; j += this.step) {
				final Path bestPath = bpsf.get();

				if (bestPath.length != origDist) {
					origDist = bestPath.length;
					System.arraycopy(bestPath.steps, 0, path, 0, bestPath.steps.length);
					repeat = true;
				}

				double d2 = tryPermutationsAt(path, j, width, origDist);
				if (d2 < origDist - 0.0001) {
					repeat = true;
				}
				origDist = d2;

			}
		} while (repeat);

		// }
		System.out.println("Thread " + Thread.currentThread() + " done");

	}

	private double tryPermutationsAt(final int[] path, final int i, final int sectionWidth, final double origDist) {
		System.arraycopy(path, i, pathSection, 0, sectionWidth);

		bfp.bruteForceSolve(pathSection, i + 1, path[i - 1], path[i + sectionWidth]);
		final double newDist = origDist - bfp.getOriginalPathLen() + bfp.getBestLength();

		if (bfp.getBestLength() < bfp.getOriginalPathLen()) {

			System.arraycopy(bfp.getBestPath(), 0, path, i, sectionWidth);
			bpsf.update(path, newDist);
			return newDist;
		}

		return origDist;
	}
}
