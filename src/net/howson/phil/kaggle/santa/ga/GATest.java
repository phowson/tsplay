package net.howson.phil.kaggle.santa.ga;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.BestPathSoFar;
import net.howson.phil.kaggle.santa.map.MapLoader;
import net.howson.phil.kaggle.santa.map.WorldMap;
import net.howson.phil.kaggle.santa.path.Path;
import net.howson.phil.kaggle.santa.path.PathLoader;

public class GATest implements Runnable {

	private static final Logger logger = LogManager.getLogger(GATest.class);
	private WorldMap map;
	private final int sectionWidth = 55;
	private final int fixInterval = 2;
	private final int retries = 4;
	private GAStats gaStats;

	public GATest(WorldMap map, BestPathSoFar bpsf2, GAStats gaStats) {
		this.map = map;
		this.bpsf = bpsf2;
		this.gaStats = gaStats;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {

		final WorldMap map = new MapLoader().load(new File("./data/cities.csv"));
		final int[] path = new PathLoader().load(new File("./data/out.csv"));
		double initialLength = map.pathDistanceRoundTripToZero(path);
		System.out.println("Started at : "+ initialLength);
		BestPathSoFar bpsf = new BestPathSoFar(new Path(path, initialLength));
		GAStats gaStats = new GAStats();
		for (int i = 0; i < 4; ++i)
			new Thread(new GATest(map, bpsf, gaStats)).start();

	}

	private BestPathSoFar bpsf;
	private SplittableRandom sr = new SplittableRandom();
	private boolean overallConvergence;

	public void run() {

		int[] pathSection = new int[sectionWidth];

		while (true) {

			Path b = bpsf.get();
			int[] path = b.steps;
			int i = sr.nextInt(path.length - sectionWidth - 1) + 1;

			runGaAt(map, path, sectionWidth, pathSection, i);

			bpsf.update(path, map.pathDistanceRoundTripToZero(path));
			gaStats.print();
		}

	}

	private void runGaAt(final WorldMap map, final int[] path, int sectionWidth, int[] pathSection, int pathIndex) {
		System.arraycopy(path, pathIndex, pathSection, 0, sectionWidth);
		GAEnvironment gae = new GAEnvironment(map, path[pathIndex - 1], path[pathIndex + sectionWidth], pathIndex + 1,
				pathSection);

		GAPopulationElement absoluteBest = new GAPopulationElement(gae, pathSection);
		GA ga = new GA(0.25, 100, gae, new BasicSafeCrossover2(), new BasicRandomisationMutation(sectionWidth / 4),
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
		
		
		while (true) {

			if (g==200) {
				canFix = true;
				duplicateRuns = 0;
			}
			
			ga.runOneGeneration(canFix && g % fixInterval == 0);

			double best = ga.getBestSoFar();
			if (lastBest == best) {
				++duplicateRuns;
			} else {
				duplicateRuns = 0;
			}
			lastBest = best;
			// if (g % 2 == 0) {
			// ga.fix();
			//

			/*
			 * if (g % 50 == 0) { System.out.println("-- Generation : " + g);
			 * System.out.println("Original length : " + orig.getLength());
			 * System.out.println("Best : " + ga.getBestSoFar()); }
			 */

			if (duplicateRuns == 300) {
				// System.out.println("fix");
				// double beforeFix = ga.getBestSoFar();
				// ga.fix();
				// if (ga.getBestSoFar() == beforeFix) {
				// ++fixNoEffect;
				//
				// ga.insert(orig);
				// }
				//
				// if (fixNoEffect == 2) {
				// break;
				// }
				// System.out.println("Best : " + ga.getBestSoFar());
				// duplicateRuns = 0;
				break;

			}
			++g;
		}

		double best = ga.getBestSoFar();
		gaStats.updateStats(best <= absoluteBest.getLength() + 1e-5, g);
		overallConvergence |= best <= absoluteBest.getLength() + 1e-5;

		if (best < absoluteBest.getLength()) {
			System.out.println("Improved! " + best + " at path position " + pathIndex);
			absoluteBest = ga.getBestItem();
			System.arraycopy(absoluteBest.items, 0, path, pathIndex, sectionWidth);

		}
		return absoluteBest;
	}

}
