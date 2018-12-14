package net.howson.phil.kaggle.santa.ga;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.BestPathSoFar;
import net.howson.phil.kaggle.santa.ga.GATest.LoggingRunnable;
import net.howson.phil.kaggle.santa.map.MapLoader;
import net.howson.phil.kaggle.santa.map.WorldMap;
import net.howson.phil.kaggle.santa.path.Path;
import net.howson.phil.kaggle.santa.path.PathLoader;

public class GATest implements Runnable {

	public static class LoggingRunnable implements Runnable {

		private BestPathSoFar bpsf;
		private GAStats gaStats;

		public LoggingRunnable(BestPathSoFar bpsf, GAStats gaStats) {
			this.bpsf = bpsf;
			this.gaStats = gaStats;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(60000);
				gaStats.reset();

				while (!Thread.interrupted()) {
					Thread.sleep(10000);
					gaStats.print();
					System.out.println("Current best : " + bpsf.get().length);
				}
			} catch (InterruptedException e) {

			}
		}
	}

	private static final Logger logger = LogManager.getLogger(GATest.class);
	private WorldMap map;
	private final int sectionWidth = 50;
	private final int fixInterval = 5;
	private final int retries = 1;
	private int unFixedGenerations = 10;
	private int maxDupRuns = 200;
	private double eliteProportion = 0.3;
	private int populationSize = 125;

	private GAStats gaStats;
	private int startIdx;
	private int endIdx;
	private BestPathSoFar bpsf;
	private SplittableRandom sr = new SplittableRandom();
	private boolean overallConvergence;

	public GATest(WorldMap map, BestPathSoFar bpsf2, GAStats gaStats, int startIdx, int endIdx) {
		this.map = map;
		this.bpsf = bpsf2;
		this.gaStats = gaStats;
		this.startIdx = startIdx;
		this.endIdx = endIdx;

	}

	public static void main(String[] args) throws FileNotFoundException, IOException {

		final WorldMap map = new MapLoader().load(new File("./data/cities.csv"));
		final int[] path = new PathLoader().load(new File("./data/out.csv"));
		double initialLength = map.pathDistanceRoundTripToZero(path);
		System.out.println("Started at : " + initialLength);
		BestPathSoFar bpsf = new BestPathSoFar(new Path(path, initialLength));
		GAStats gaStats = new GAStats();

		int width = path.length / 4;
		for (int i = 0; i < 4; ++i)
			new Thread(new GATest(map, bpsf, gaStats, 1 + (i * width), 1 + width + (i * width))).start();

		new Thread(new LoggingRunnable(bpsf, gaStats)).start();

	}

	public void run() {

		int[] pathSection = new int[sectionWidth];

		while (true) {

			Path b = bpsf.get();
			int[] path = b.steps;
			int i = sr.nextInt((endIdx - startIdx) - sectionWidth - 1) + startIdx;

			runGaAt(map, path, sectionWidth, pathSection, i);

			bpsf.update(path, map.pathDistanceRoundTripToZero(path));

		}

	}

	private void runGaAt(final WorldMap map, final int[] path, int sectionWidth, int[] pathSection, int pathIndex) {
		System.arraycopy(path, pathIndex, pathSection, 0, sectionWidth);
		GAEnvironment gae = new GAEnvironment(map, path[pathIndex - 1], path[pathIndex + sectionWidth], pathIndex + 1,
				pathSection);

		GAPopulationElement absoluteBest = new GAPopulationElement(gae, pathSection);
		GA ga = new GA(0.25, eliteProportion, populationSize, gae, new BasicSafeCrossover2(), new BasicRandomisationMutation(sectionWidth / 4),
				// new LocalRandomisationMutation(sectionWidth/4, 1),
				// new BrokenPermFixer(5, gae)
				new SwapFixer(gae));

		overallConvergence = false;
		for (int t = 0; t < retries; ++t) {
			absoluteBest = singleGaRun(path, sectionWidth, pathIndex, absoluteBest, ga);
		}
		gaStats.overallStats(overallConvergence);

	}

	private GAPopulationElement singleGaRun(final int[] path, int sectionWidth, int pathIndex,
			GAPopulationElement absoluteBest, GA ga) {
		int g = 0;
		ga.setup(null);

		double lastBest = 0;
		int duplicateRuns = 0;
		boolean canFix = false;
		int fixes = 0;
		while (true) {

			if (g == unFixedGenerations) {
				canFix = true;
			}

			boolean f = canFix && g % fixInterval == 0;
			ga.runOneGeneration(f);
			if (f) {
				++fixes;
			}

			double best = ga.getBestSoFar();

			if (lastBest == best && canFix) {
				++duplicateRuns;
			} else {
				duplicateRuns = 0;
			}
			lastBest = best;

			if (duplicateRuns == maxDupRuns) {
				break;
			}
			++g;
		}

		double best = ga.getBestSoFar();
		gaStats.updateStats(best <= absoluteBest.getLength() + 1e-5, g, fixes);
		overallConvergence |= best <= absoluteBest.getLength() + 1e-5;

		if (best < absoluteBest.getLength()) {
			System.out.println("Improved! " + best + " at path position " + pathIndex);
			absoluteBest = ga.getBestItem();
			System.arraycopy(absoluteBest.items, 0, path, pathIndex, sectionWidth);

		}
		return absoluteBest;
	}

}
