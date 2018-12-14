package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.BruteForcePath;
import net.howson.phil.kaggle.santa.path.PairSwapper;

public class SwapFixer implements FixOperator {

	private static final Logger logger = LogManager.getLogger(SwapFixer.class);

	private final GAEnvironment env;

	public SwapFixer(GAEnvironment env) {
		this.env = env;
	}

	@Override
	public void fix(GAPopulationElement e) {

		double dist = e.getLength();
		final int n = e.items.length;
		double origdist;
		do {
			origdist = dist;
			for (int i = 0; i < n; ++i) {
				dist = trySwapsAt(e.items, i, dist);
			}

		} while (origdist != dist);

		e.resetLength();

	}

	private double trySwapsAt(int[] items, int i, double dist) {
		double bestlen = dist;
		int best = i;

		if (i == 0) {
			for (int j = 1; j < items.length - 1; ++j) {
				final double l = PairSwapper.computeSwapDistance0(env.beforeIdx, items, dist, i, j, env.map,
						env.pathOffset - 1);

				if (l < bestlen) {
					bestlen = l;
					best = j;
				}

			}

		} else if (i == items.length - 1) {
			for (int j = 1; j < items.length - 1; ++j) {
				final double l = PairSwapper.computeSwapDistanceL(env.afterIdx, items, dist, i, j, env.map,
						env.pathOffset - 1);

				if (l < bestlen) {
					bestlen = l;
					best = j;
				}

			}

		} else {

			for (int j = i; j < items.length - 1; ++j) {

				final double l = PairSwapper.computeSwapDistance(items, dist, i, j, env.map, env.pathOffset - 1);

				if (l < bestlen) {
					bestlen = l;
					best = j;
				}

			}
		}
		if (bestlen < dist) {
			PairSwapper.swap(items, best, i);
		}
		return bestlen;

	}

}
