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

	public static class LoggingRunnable implements Runnable {

		private final BestPathSoFar bpsf;
		private final GAStats gaStats;

		public LoggingRunnable(final BestPathSoFar bpsf, final GAStats gaStats) {
			this.bpsf = bpsf;
			this.gaStats = gaStats;
		}

		@Override
		public void run() {
			try {

				Thread.sleep(20000);
				gaStats.reset();
				for (int i = 0; i < 60; ++i) {
					System.out.println("Warming up");
					gaStats.print();
					System.out.println("Current best : " + bpsf.get().length);
					Thread.sleep(10000);
				}

				gaStats.reset();

				while (!Thread.interrupted()) {
					Thread.sleep(10000);
					gaStats.print();
					System.out.println("Current best : " + bpsf.get().length);
				}
			} catch (final InterruptedException e) {

			}
		}
	}

	private static final Logger logger = LogManager.getLogger(GATest.class);
	private final WorldMap map;
	private final int sectionWidth = 50;
	private final int fixInterval = 1;
	private final int retries = 1;
	private final int unFixedGenerations = 10;
	private final int maxDupRuns = 100;
	private final int canTerminateGenerations = 100;
	
	
	private final double eliteProportion = 0.3;
	private final int populationSize = 125;
	private final double mutationRate = 0.10;
	private final double mutationProportion = 0.15;
	private boolean uniformlySelected = true;
	private boolean sequential = true;

	private final GAStats gaStats;
	private final int startIdx;
	private final int endIdx;
	private final BestPathSoFar bpsf;
	private final SplittableRandom sr = new SplittableRandom();
	private boolean overallConvergence;
	private PathSectionSelector pathSectionSelector;

	public GATest(final WorldMap map, final BestPathSoFar bpsf2, final GAStats gaStats, final int startIdx,
			final int endIdx) {
		this.map = map;
		this.bpsf = bpsf2;
		this.gaStats = gaStats;
		this.startIdx = startIdx;
		this.endIdx = endIdx;
		this.pathSectionSelector = new PathSectionSelector(map, sectionWidth, 2000);

	}

	public static void main(final String[] args) throws FileNotFoundException, IOException {

		final WorldMap map = new MapLoader().load(new File("./data/cities.csv"));
		final int[] path = new PathLoader().load(new File("./data/out.csv"));
		final double initialLength = map.pathDistanceRoundTripToZero(path);
		System.out.println("Started at : " + initialLength);
		final BestPathSoFar bpsf = new BestPathSoFar(new Path(path, initialLength));
		final GAStats gaStats = new GAStats();

		int nThreads = 8;
		final int width = path.length / nThreads;
		for (int i = 0; i < nThreads; ++i)
			new Thread(new GATest(map, bpsf, gaStats, 1 + (i * width), 1 + width + (i * width))).start();

		new Thread(new LoggingRunnable(bpsf, gaStats)).start();

	}

	@Override
	public void run() {

		final int[] pathSection = new int[sectionWidth];

		while (true) {


			
			if (sequential) {
				

				for (int i = startIdx; i<endIdx; ++i) {
					final Path b = bpsf.get();
					final int[] path = b.steps;
					runGaAt(map, path, sectionWidth, pathSection, i);
					if (overallConvergence)
						bpsf.update(path, map.pathDistanceRoundTripToZero(path));
				}
				
			} else {
			
				final Path b = bpsf.get();
				final int[] path = b.steps;
				
				int i;
				
				if (uniformlySelected) {
					i = sr.nextInt((endIdx - startIdx) - sectionWidth - 1) + startIdx;
				} else {
					i = this.pathSectionSelector.selectNextIndex(b);
					if (i > path.length - sectionWidth - 1) {
						i = path.length - sectionWidth - 1;
					} else if (i < 1) {
						i = 1;
					}
				}

				runGaAt(map, path, sectionWidth, pathSection, i);
				if (overallConvergence)
					bpsf.update(path, map.pathDistanceRoundTripToZero(path));
			}


		}

	}

	private void runGaAt(final WorldMap map, final int[] path, final int sectionWidth, final int[] pathSection,
			final int pathIndex) {
		System.arraycopy(path, pathIndex, pathSection, 0, sectionWidth);
		final GAEnvironment gae = new GAEnvironment(map, path[pathIndex - 1], path[pathIndex + sectionWidth],
				pathIndex + 1, pathSection);

		GAPopulationElement absoluteBest = new GAPopulationElement(gae, pathSection);
		final GA ga = new GA(mutationRate, eliteProportion, populationSize, gae, new BasicSafeCrossover2(),
				new BasicRandomisationMutation((int) (sectionWidth * mutationProportion)),
				new BasicRandomisationMutation((int) (sectionWidth)),

				new GreedySwapFixer(gae), 
				new ProportionalSelection(populationSize, new InverseScorer())
		// new EliteSelection(0.5, populationSize)
		// new NoSelection()

		);

		overallConvergence = false;
		for (int t = 0; t < retries; ++t) {
			absoluteBest = singleGaRun(path, sectionWidth, pathIndex, absoluteBest, ga);
		}
		gaStats.overallStats(overallConvergence);

	}

	private GAPopulationElement singleGaRun(final int[] path, final int sectionWidth, final int pathIndex,
			GAPopulationElement absoluteBest, final GA ga) {
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

			final boolean f = canFix && g % fixInterval == 0;
			ga.runOneGeneration(f);
			if (f) {
				++fixes;
			}

			final double best = ga.getBestSoFar();

			if (lastBest == best && canFix && g>canTerminateGenerations) {
				
				
				++duplicateRuns;
			
			} else {
				duplicateRuns = 0;
			}
			lastBest = best;

			if (g>canTerminateGenerations && duplicateRuns >= maxDupRuns) {
				break;
			}
			++g;
		}
		
		final double best = ga.getBestSoFar();
		
//		System.out.println("Best : " + absoluteBest.getLength());
//		System.out.println("Diff : "+ (best - absoluteBest.getLength()));
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
