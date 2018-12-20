package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.map.WorldMap;
import net.howson.phil.kaggle.santa.path.PairSwapper;

public final class GreedySwapFixer implements FixOperator {

	private static final Logger logger = LogManager.getLogger(GreedySwapFixer.class);

	private final GAEnvironment env;

	public GreedySwapFixer(final GAEnvironment env) {
		this.env = env;
	}

	@Override
	public void fix(final GAPopulationElement e) {

		double dist = e.getLength();
		double origdist = dist;
		final int n = e.items.length;

		
		for (int i = 0; i < n; ++i) {
			dist = trySwapsAt(e.items, i, dist);
		}

		e.fixed = origdist == dist;
		if (!e.fixed) {
			e.resetLength();
		}

	}

	private double trySwapsAt(final int[] items, final int i, double dist) {

		final int pathOffsetMinusOne = env.pathOffset - 1;
		final int bi = env.beforeIdx;
		final WorldMap map = env.map;
		final int liMinusOne = items.length - 1;
		final int afterIdx = env.afterIdx;
		if (i == 0) {

			for (int j = 1; j < liMinusOne; ++j) {
				final double l = PairSwapper.computeSwapDistance0(bi, items, dist, i, j, map, pathOffsetMinusOne);

				if (l < dist) {
					PairSwapper.swap(items, j, i);
					dist = l;
				}

			}

			final double l = PairSwapper.computeSwapDistance0L(bi, afterIdx, items, dist, i, liMinusOne, map,
					pathOffsetMinusOne);

			if (l < dist) {
				PairSwapper.swap(items, liMinusOne, i);
				dist = l;
			}

		} else if (i == liMinusOne) {
			for (int j = 1; j < liMinusOne; ++j) {
				final double l = PairSwapper.computeSwapDistanceL(afterIdx, items, dist, i, j, map, pathOffsetMinusOne);

				if (l < dist) {
					PairSwapper.swap(items, j, i);
					dist = l;
				}

			}

		} else {

			for (int j = i; j < liMinusOne; ++j) {

				final double l = PairSwapper.computeSwapDistance(items, dist, i, j, map, pathOffsetMinusOne);

				if (l < dist) {
					PairSwapper.swap(items, j, i);
					dist = l;
				}

			}
		}

		return dist;

	}

}
