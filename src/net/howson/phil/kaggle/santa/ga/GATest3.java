package net.howson.phil.kaggle.santa.ga;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.BestPathSoFar;
import net.howson.phil.kaggle.santa.ga.PathAssessment.PathItem;
import net.howson.phil.kaggle.santa.map.MapLoader;
import net.howson.phil.kaggle.santa.map.WorldMap;
import net.howson.phil.kaggle.santa.path.Path;
import net.howson.phil.kaggle.santa.path.PathLoader;

public class GATest3 implements Runnable {

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

	private static final Logger logger = LogManager.getLogger(GATest3.class);
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
					new BasicRandomisationMutation((int) (sectionWidth * mutationProportion)),

					new GreedySwapFixer(gae), new ProportionalSelection(populationSize, new InverseScorer())
			// new EliteSelection(0.5, populationSize)
			// new NoSelection()

			);
			return ga;
		}
	}

	private boolean uniformlySelected = true;

	private final GAStats gaStats100;
	private final GAStats gaStats50;
	private final GAStats gaStatsOverall;
	private final int sectionWidth = 50;
	private final BestPathSoFar bpsf;
	private final SplittableRandom sr = new SplittableRandom();

	private PathAssessment pathAssessment;

	public GATest3(final WorldMap map, final BestPathSoFar bpsf2, final GAStats gaStats100, GAStats gaStats50,
			GAStats gaStatsOverall) {
		this.map = map;
		this.bpsf = bpsf2;
		this.gaStats100 = gaStats100;
		this.gaStats50 = gaStats50;
		this.gaStatsOverall = gaStatsOverall;
		this.pathAssessment = new PathAssessment(map);

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

		int nThreads = 4;
		for (int i = 0; i < nThreads; ++i)
			new Thread(new GATest3(map, bpsf, gaStats100, gaStats50, gaStatsOverall)).start();

		new Thread(new LoggingRunnable(bpsf, gaStats100, gaStats50, gaStatsOverall)).start();

	}

	@Override
	public void run() {

		while (true) {

			final Path inputPath = bpsf.get();

			final int[] path = Arrays.copyOf(inputPath.steps, inputPath.steps.length);

			pathAssessment.updateWithPath(inputPath);
			List<PathItem> items = pathAssessment.getNonPrimeTenthSteps();
			PathItem i = items.get(sr.nextInt(items.size()));
			
//			System.out.println(map.pathDistanceRoundTripToZero(path));
//			map.setPrime(i.cityId);
//			System.out.println(map.pathDistanceRoundTripToZero(path));
//			System.exit(-1);

			PathItem closest = pathAssessment.getClosestUnusedPrimes(i.pathIdx, sectionWidth/2);

			if (closest==null) {
				continue;
			}
			
			int t = path[closest.pathIdx];
			path[closest.pathIdx] = path[i.pathIdx];
			path[i.pathIdx] = t;
			
			double d = map.pathDistanceRoundTripToZero(path);
			if (d-inputPath.length  > 8) {
				continue;
			}
			
			System.out.println("Start " +d);
			doGa(closest.pathIdx - sectionWidth / 2, path);
			doGa(i.pathIdx - sectionWidth / 2, path);

			d = map.pathDistanceRoundTripToZero(path);
			System.out.println(d);

			gaStatsOverall.updateStats(d <= inputPath.length, 1, 1);
			gaStatsOverall.overallStats(d <= inputPath.length);
			if (d < inputPath.length) {
				bpsf.update(path, d);
			}
		}

	}

	private void doGa(int index, int[] pathArray) {

		GARunner smallRunner = new GARunner();
		smallRunner.fixInterval = 1;

		GAFactoryImpl gaf = new GAFactoryImpl();

		smallRunner.run(map, pathArray, index, sectionWidth, gaf, gaStats50, 4, true);

	}

}
