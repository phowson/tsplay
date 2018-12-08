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
	int sectionWidth = 50;
	public GATest(WorldMap map, BestPathSoFar bpsf2) {
		this.map = map;
		this.bpsf = bpsf2;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {

		final WorldMap map = new MapLoader().load(new File("./data/cities.csv"));
		// final int[] path = new PathLoader().load(new File("./data/out.csv"));
		final int[] path = new PathLoader().load(new File("./data/out.csv"));
		BestPathSoFar bpsf = new BestPathSoFar(new Path(path, map.pathDistanceRoundTripToZero(path)));
		for (int i =0; i<4; ++i)
			new Thread(new GATest(map, bpsf)).start();
		
	}

	private BestPathSoFar bpsf;
	private SplittableRandom sr = new SplittableRandom();

	public void run() {


		int[] pathSection = new int[sectionWidth];

		while (true) {

			int[] path = bpsf.get().steps;
			int i = sr.nextInt(path.length - sectionWidth - 1) + 1;

			runGaAt(map, path, sectionWidth, pathSection, i);
			bpsf.update(path, map.pathDistanceRoundTripToZero(path));
		}

	}

	private void runGaAt(final WorldMap map, final int[] path, int sectionWidth, int[] pathSection, int i) {
		System.arraycopy(path, i, pathSection, 0, sectionWidth);
		GAEnvironment gae = new GAEnvironment(map, path[i - 1], path[i + sectionWidth], i + 1, pathSection);

		GAPopulationElement orig = new GAPopulationElement(gae, pathSection);
		GAPopulationElement absoluteBest = new GAPopulationElement(gae, pathSection);
		absoluteBest.getLength();
		GA ga = new GA(0.25, 100, gae, new BasicSafeCrossover2(), new BasicRandomisationMutation(sectionWidth / 4),
				// new LocalRandomisationMutation(sectionWidth/4, 1),
				// new BrokenPermFixer(5, gae)
				new PermFixer(6, gae));

		for (int t = 0; t < 4; ++t) {
			ga.setup(null);

			int g = 1;
			double lastBest = 0;
			int duplicateRuns = 0;
			int fixNoEffect = 0;

			while (true) {

				ga.runOneGeneration();

				double best = ga.getBestSoFar();
				if (lastBest == best) {
					++duplicateRuns;
				} else {
					fixNoEffect = 0;
					duplicateRuns = 0;
				}
				lastBest = best;
				// if (g % 2 == 0) {
				// ga.fix();
				//
				
				/*
				if (g % 50 == 0) {
					System.out.println("-- Generation : " + g);
					System.out.println("Original length : " + orig.getLength());
					System.out.println("Best : " + ga.getBestSoFar());
				}*/

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
			if (best < absoluteBest.getLength()) {
				System.out.println("Improved! " + best);
				absoluteBest = ga.getBestItem();
				System.arraycopy(absoluteBest.items, 0, path, i, sectionWidth);

			}
		}

	}

}
