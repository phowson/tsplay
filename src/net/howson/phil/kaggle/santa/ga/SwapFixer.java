package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.map.WorldMap;
import net.howson.phil.kaggle.santa.path.PairSwapper;

public final class SwapFixer implements FixOperator {

	private static final Logger logger = LogManager.getLogger(SwapFixer.class);

	private final GAEnvironment env;

	public SwapFixer(final GAEnvironment env) {
		this.env = env;
	}

	@Override
	public void fix(final GAPopulationElement e) {

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
		e.fixed = true;

	}

	private double trySwapsAt(final int[] items, final int i, final double dist) {
		double bestlen = dist;
		int best = i;

		final int pathOffsetMinusOne = env.pathOffset - 1;
		final int bi = env.beforeIdx;
		final WorldMap map = env.map;
		final int liMinusOne = items.length - 1;
		final int afterIdx = env.afterIdx;
		if (i == 0) {

			for (int j = 1; j < liMinusOne; ++j) {
				final double l = PairSwapper.computeSwapDistance0(bi, items, dist, i, j, map, pathOffsetMinusOne);

				if (l < bestlen) {
					bestlen = l;
					best = j;
				}

			}

			final double l = PairSwapper.computeSwapDistance0L(bi, afterIdx, items, dist, i, liMinusOne, map,
					pathOffsetMinusOne);

			if (l < bestlen) {
				bestlen = l;
				best = liMinusOne;
			}

		} else if (i == liMinusOne) {
			for (int j = 1; j < liMinusOne; ++j) {
				final double l = PairSwapper.computeSwapDistanceL(afterIdx, items, dist, i, j, map, pathOffsetMinusOne);

				if (l < bestlen) {
					bestlen = l;
					best = j;
				}

			}

		} else {

			for (int j = i; j < liMinusOne; ++j) {

				final double l = PairSwapper.computeSwapDistance(items, dist, i, j, map, pathOffsetMinusOne);

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
