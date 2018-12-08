package net.howson.phil.kaggle.santa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.map.MapLoader;
import net.howson.phil.kaggle.santa.map.WorldMap;
import net.howson.phil.kaggle.santa.path.PairSwapper;
import net.howson.phil.kaggle.santa.path.Path;
import net.howson.phil.kaggle.santa.path.PathLoader;

public class PlayAround {

	private static final Logger logger = LogManager.getLogger(PlayAround.class);

	public static void main(final String[] args) throws FileNotFoundException, IOException {
		final WorldMap map = new MapLoader().load(new File("./data/cities.csv"));
		final PairSwapper rs = new PairSwapper();

		Path bestPath;

		{
			final int[] path = new PathLoader().load(new File("./data/modelsubmission.csv"));
			final double x = map.pathDistanceRoundTripToZero(path);

			bestPath = new Path(path, x);

			System.out.println(x);
			System.out.println(1533247.82);
		}
		// 1521569.82

		boolean first = true;
		while (true) {

			boolean changed = false;

			double x = bestPath.length;
			final int[] path = Arrays.copyOf(bestPath.steps, bestPath.steps.length);

			if (!first) {
				for (int i = 0; i < 30; ++i) {
					x = rs.pairWiseRandomMutate(map, path, x, 1);
				}
				System.out.println("Post randomisation " + x);
			}
			first = false;

			do {
				changed = false;

				for (int k = 1; k < path.length - 1; ++k) {
					if (map.isPrime(k)) {

						final double newx = rs.mutateAround(map, path, x, k, 15);
						if (x != newx) {
							changed = true;
							x = newx;
//							System.out.println("Prime movement" + x);
						}

					}

				}

				// for (int k = 1; k < 15; ++k) {
				for (int i = 1; i < path.length - 1; i += 1) {
					final double newx = rs.mutateAround(map, path, x, i, 15);
					if (x != newx) {
						changed = true;
						x = newx;
//						System.out.println(x);
					}
				}

				// }

			} while (changed);

			System.out.println(x);
			if (x < bestPath.length) {
				x = map.pathDistanceRoundTripToZero(path);
				if (x < bestPath.length) {
					System.out.println("Updated best path " + x);
					bestPath = Path.copy(path, x);
					new PathWriter().save(bestPath.steps, new File("./data/out.csv"));
				}

			}

		}

	}
}
