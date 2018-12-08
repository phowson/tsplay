package net.howson.phil.kaggle.santa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gnu.trove.list.array.TIntArrayList;
import net.howson.phil.kaggle.santa.map.MapLoader;
import net.howson.phil.kaggle.santa.map.WorldMap;
import net.howson.phil.kaggle.santa.path.PairSwapper;
import net.howson.phil.kaggle.santa.path.Path;
import net.howson.phil.kaggle.santa.path.PathLoader;

public class PlayAround3 {

	private static final Logger logger = LogManager.getLogger(PlayAround3.class);

	private static class BestPathSoFar {

		private Path bestPath;

		public BestPathSoFar(final Path bestPath) {
			this.bestPath = bestPath;
		}

		public synchronized Path get() {
			return bestPath;
		}

		public synchronized void update(final int[] path, final double x) {
			if (x < bestPath.length) {
				System.out.println(System.currentTimeMillis() + " Updated best path " + x + " "
						+ Thread.currentThread().getName());
				bestPath = Path.copy(path, x);
				try {
					new PathWriter().save(bestPath.steps, new File("./data/out.csv"));
				} catch (final FileNotFoundException e) {
					logger.error("Unexpected exception", e);
				}

			}

		}

	}

	private static final class Worker implements Runnable {

		private final BestPathSoFar bestPathInput;
		private final PairSwapper rs = new PairSwapper();
		private final WorldMap map;
		private final boolean isAorB;

		@Override
		public void run() {
			final TIntArrayList primes = new TIntArrayList();
			for (int k = 1; k < map.size() - 2; ++k) {
				if (map.isPrime(k)) {
					primes.add(k);
				}
			}

			final int[] allPrimes = primes.toArray();
			final int path[] = new int[bestPathInput.get().steps.length];

			while (true) {

				final Path bestPath = bestPathInput.get();

				double bestLength = bestPath.length;
				System.arraycopy(bestPath.steps, 0, path, 0, bestPath.steps.length);

				if (isAorB) {
					//bestLength = doABehaviour(path, bestLength, 25, 40, 20);
					bestLength = doABehaviour(path, bestLength, 20, 25, 40);
				} else {
					
					bestLength = doABehaviour(path, bestLength, 25, 30, 40);
					
					//bestLength = doABehaviour(path, bestLength, 11, 15, 20);
					// bestLength = doABehaviour(path, bestLength, 20, 11, 30);
					
					
				}

				if (bestLength < bestPath.length - 0.0001) {
					bestLength = map.pathDistanceRoundTripToZero(path);
					if (bestLength < bestPath.length - 0.0001) {
						bestPathInput.update(path, bestLength);
					}

				}
			}
		}

		private final SplittableRandom r = new SplittableRandom();

		private double doABehaviour(final int[] path, double bestLength, final int randomisationWidth,
				final int correctionWidth, final int numShuffles) {
			final int z = Math.max(1, r.nextInt(path.length - 4) - randomisationWidth / 2);

			for (int k = 0; k < numShuffles; ++k) {
				final int i = Math.min(path.length - 2, r.nextInt(randomisationWidth) + z);
				final int j = Math.min(path.length - 2, i + 1);

				bestLength = PairSwapper.computeSwapDistance(path, bestLength, i, j, map,0);
				PairSwapper.swap(path, i, j);
			}

			final int min = Math.max(1, z - correctionWidth);
			final int max = Math.min(z + correctionWidth, path.length - 2);



			for (int i = min; i < max; ++i) {

				bestLength = tryPermutationsAt(path, i, fixWidth, bestLength);

			}
			
			for (int i = min; i < max; ++i) {
				bestLength = rs.mutateAround(map, path, bestLength, i, 15);

			}
			
			return bestLength;
		}

		private double doBBehaviour(final int[] path, double bestLength, final int randomisationWidth,
				final int correctionWidth, final int numShuffles) {
			final int z = Math.max(1, r.nextInt(path.length - 4) - randomisationWidth / 2);

			for (int k = 0; k < numShuffles; ++k) {
				final int i = Math.min(path.length - 2, r.nextInt(randomisationWidth) + z);
				final int j = Math.min(path.length - 2, i + 1);

				bestLength = PairSwapper.computeSwapDistance(path, bestLength, i, j, map,0);
				PairSwapper.swap(path, i, j);
			}

			final int min = Math.max(1, z - correctionWidth);
			final int max = Math.min(z + correctionWidth, path.length - 2);



//			for (int i = min; i < max; ++i) {
//
//				bestLength = tryPermutationsAt(path, i, fixWidth, bestLength);
//
//			}
			
			for (int i = min; i < max; ++i) {
				bestLength = rs.mutateAround(map, path, bestLength, i, 15);

			}
			
			return bestLength;
		}

		
		
		private final BruteForcePath bfp;
		private final int[] pathSection;

		private double tryPermutationsAt(final int[] path, final int i, final int sectionWidth, final double origDist) {

			if (i > 1 && i < path.length - sectionWidth - 2) {
				System.arraycopy(path, i, pathSection, 0, sectionWidth);

				bfp.bruteForceSolve(pathSection, i + 1, path[i - 1], path[i + sectionWidth]);
				final double newDist = origDist - bfp.getOriginalPathLen() + bfp.getBestLength();

				if (bfp.getBestLength() < bfp.getOriginalPathLen()) {

					System.arraycopy(bfp.getBestPath(), 0, path, i, sectionWidth);
					return newDist;
				}
			}

			return origDist;
		}

		// private double doBBehaviour(final int[] allPrimes, final int[] path,
		// double bestLength) {
		// for (int i = 0; i < 30; ++i) {
		// bestLength = rs.pairWiseRandomMutate(map, path, bestLength, 1);
		// }
		//
		// boolean changed;
		// do {
		// changed = false;
		// for (int k = 0; k < allPrimes.length; ++k) {
		// final double newLen = rs.mutateAround(map, path, bestLength,
		// allPrimes[k], 22);
		// if (bestLength != newLen) {
		// changed = true;
		// bestLength = newLen;
		// }
		// }
		//
		// for (int i = 1; i < path.length - 1; ++i) {
		// final double newLen = rs.mutateAround(map, path, bestLength, i, 15);
		// if (bestLength != newLen) {
		// changed = true;
		// bestLength = newLen;
		// }
		// }
		// } while (changed);
		// return bestLength;
		// }
		//

		private static final int fixWidth = 5;

		public Worker(final BestPathSoFar bestPathInput, final WorldMap map, final boolean isAorB) {

			this.bestPathInput = bestPathInput;
			this.map = map;
			this.isAorB = isAorB;

			bfp = new BruteForcePath(map, fixWidth);
			pathSection = new int[fixWidth];
		}

	}

	public static void main(final String[] args) throws FileNotFoundException, IOException {
		// Start from last known good.
		final WorldMap map = new MapLoader().load(new File("./data/cities.csv"));
//1516787.95
		//1516800.85
		
		
//		final int[] path = new PathLoader().load(new File("./data/good_concorde_start_point.csv"));
//		 final int[] path = new PathLoader().load(new File("./data/bestsofar-1516843.csv"));
		 
//		 final int[] path = new PathLoader().load(new
//				 File("./data/better_concorde_start_point.csv"));
		 
		 final int[] path = new PathLoader().load(new
				 File("./data/out.csv"));
		final double x = map.pathDistanceRoundTripToZero(path);
		System.out.println("Initial length : "+ x);

		final Path bestPath = new Path(path, x);

		final BestPathSoFar bpsf = new BestPathSoFar(bestPath);

		for (int i = 0; i < 4; ++i) {
			new Thread(new Worker(bpsf, map, i % 2 == 0), "t-" + i).start();
		}

	}
}
