package net.howson.phil.kaggle.santa.ga;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.BestPathSoFar;
import net.howson.phil.kaggle.santa.map.MapLoader;
import net.howson.phil.kaggle.santa.map.WorldMap;
import net.howson.phil.kaggle.santa.path.Path;
import net.howson.phil.kaggle.santa.path.PathLoader;

public class GATest2 implements Runnable {

	public static class LoggingRunnable implements Runnable {

		private final BestPathSoFar bpsf;
		private GAStats gaStats;
		private GAStats gaStats2;
		private GAStats gaStats3;

		public LoggingRunnable(final BestPathSoFar bpsf, final GAStats gaStats, final GAStats gaStats2,
				final GAStats gaStats3) {
			this.bpsf = bpsf;
			this.gaStats = gaStats;
			this.gaStats2 = gaStats2;
			this.gaStats3 = gaStats3;
		}

		@Override
		public void run() {
			try {

				while (!Thread.interrupted()) {
					Thread.sleep(120000);
					System.out.println("***********************************");
					System.out.println();
					System.out.println("100 wide stats");
					gaStats.print();

					System.out.println();
					System.out.println("50 wide stats");
					gaStats2.print();

					System.out.println();
					System.out.println("Overall stats");
					gaStats3.print();

					System.out.println("Current best : " + bpsf.get().length);
				}
			} catch (final InterruptedException e) {

			}
		}
	}

	private static final Logger logger = LogManager.getLogger(GATest2.class);
	private final WorldMap map;

	private class GAFactoryImpl implements GAFactory {

		private final double eliteProportion = 0.3;
		private final int populationSize = 125;
		private final double mutationRate = 0.10;
		private final double mutationProportion = 0.15;

		@Override
		public GA create(GAEnvironment gae, int sectionWidth) {
			final GA ga = new GA(mutationRate, eliteProportion, populationSize, gae, new BasicSafeCrossover2(),
					new BasicRandomisationMutation((int) (sectionWidth * mutationProportion)),
					new BasicRandomisationMutation((int) (sectionWidth)),

					new GreedySwapFixer(gae), new ProportionalSelection(populationSize, new InverseScorer())
			// new EliteSelection(0.5, populationSize)
			// new NoSelection()

			);
			return ga;
		}
	}
	private class GAFactoryBigImpl implements GAFactory {

		private final double eliteProportion = 0.3;
		private final int populationSize = 125;
		private final double mutationRate = 0.10;
		private final double mutationProportion = 0.15;

		@Override
		public GA create(GAEnvironment gae, int sectionWidth) {
			final GA ga = new GA(mutationRate, eliteProportion, populationSize, gae, new BasicSafeCrossover2(),
					new BasicRandomisationMutation((int) (sectionWidth * mutationProportion)),
					new BasicRandomisationMutation((int) (sectionWidth)),

					new GreedySwapFixer(gae), new ProportionalSelection(populationSize, new InverseScorer())
			// new EliteSelection(0.5, populationSize)
			// new NoSelection()

			);
			return ga;
		}
	}
	private boolean uniformlySelected = false;

	private final GAStats gaStats100;
	private final GAStats gaStats50;
	private final GAStats gaStatsOverall;
	private final int sectionWidth = 100;
	private final int startIdx;
	private final int endIdx;
	private final BestPathSoFar bpsf;
	private final SplittableRandom sr = new SplittableRandom();
	private PathSectionSelector pathSectionSelector;

	public GATest2(final WorldMap map, final BestPathSoFar bpsf2, final GAStats gaStats100, GAStats gaStats50,
			GAStats gaStatsOverall, final int startIdx, final int endIdx) {
		this.map = map;
		this.bpsf = bpsf2;
		this.gaStats100 = gaStats100;
		this.gaStats50 = gaStats50;
		this.gaStatsOverall = gaStatsOverall;
		this.startIdx = startIdx;
		this.endIdx = endIdx;
		this.pathSectionSelector = new PathSectionSelector(map, sectionWidth, 50);

	}

	public static void main(final String[] args) throws FileNotFoundException, IOException {

		final WorldMap map = new MapLoader().load(new File("./data/cities.csv"));
		final int[] path = new PathLoader().load(new File("./data/out.csv"));
		final double initialLength = map.pathDistanceRoundTripToZero(path);
		System.out.println("Started at : " + initialLength);
		final BestPathSoFar bpsf = new BestPathSoFar(new Path(path, initialLength));
		final GAStats gaStats100 = new GAStats();
		final GAStats gaStats50 = new GAStats();
		final GAStats gaStatsOverall = new GAStats();

		int nThreads = 5;
		final int width = path.length / nThreads;
		for (int i = 0; i < nThreads; ++i)
			new Thread(new GATest2(map, bpsf, gaStats100, gaStats50, gaStatsOverall, 1 + (i * width),
					1 + width + (i * width))).start();

		new Thread(new LoggingRunnable(bpsf, gaStats100, gaStats50, gaStatsOverall)).start();

	}

	@Override
	public void run() {


		while (true) {

			final Path inputPath = bpsf.get();
			
			Path pathCopy = new Path(Arrays.copyOf(inputPath.steps,inputPath.steps.length), inputPath.length);
			final int[] path = pathCopy.steps;
			int i;
			
			if (uniformlySelected) {
				i = sr.nextInt((endIdx - startIdx) - sectionWidth - 1) + startIdx;
			} else {
				i = this.pathSectionSelector.selectNextIndex(pathCopy);
				if (i > path.length - sectionWidth - 1) {
					i = path.length - sectionWidth - 1;
				} else if (i < 1) {
					i = 1;
				}
			}
			
			doGa(i, pathCopy);


			double d = map.pathDistanceRoundTripToZero(pathCopy.steps);
			
			gaStatsOverall.updateStats(d<=pathCopy.length, 1, 1);
			gaStatsOverall.overallStats(d<=pathCopy.length);
			if (d < pathCopy.length) {

				bpsf.update(pathCopy.steps, d);
			}
		}

	}

	private void doGa(int index, Path path) {
		GARunner runner = new GARunner();
		GAFactoryImpl gaf = new GAFactoryImpl();
		GAFactoryBigImpl gafBig = new GAFactoryBigImpl();
		int [] pathArray = path.steps;
		
		runner.fixInterval=3;
		runner.run(map, pathArray, index, sectionWidth, gafBig, gaStats100, 5, false);
		double d = map.pathDistanceRoundTripToZero(path.steps);	
		System.out.println("After 100 opt, " + d);
		
		runner.fixInterval=1;
		runner.run(map, pathArray, index, 50, gaf, gaStats50, 4, true);
		d = map.pathDistanceRoundTripToZero(path.steps);
//		System.out.println("After 50 opt 1, " + d);
		
		
		runner.run(map, pathArray, index+50, 50, gaf, gaStats50, 4, true);
		d = map.pathDistanceRoundTripToZero(path.steps);
//		System.out.println("After 50 opt 2, " + d);
		
		runner.run(map, pathArray, index+25, 50, gaf, gaStats50, 4, true);
		d = map.pathDistanceRoundTripToZero(path.steps);
		System.out.println("After 50 opt 3, " + d);
		
		
	}

}
