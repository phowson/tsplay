package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.BruteForcePath;
import net.howson.phil.kaggle.santa.path.PairSwapper;

public final class SwapFixer implements FixOperator {

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
		e.fixed = true;

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
			
			double l = PairSwapper.computeSwapDistance0L(env.beforeIdx,env.afterIdx, items, dist, i, items.length - 1, env.map,
					env.pathOffset - 1);
			
//			PairSwapper.swap(items, 0, items.length-1);
//			double d = env.map.pathDistanceFrom(env.beforeIdx, env.pathOffset, items);
//			d += env.map.distance(items[items.length - 1], env.afterIdx, items.length + env.pathOffset);
//			
//			if (Math.abs(d-l)>1e-5) {
//				System.out.println("??");
//			}
//			PairSwapper.swap(items, 0, items.length-1);
			

			if (l < bestlen) {
				bestlen = l;
				best = items.length - 1;
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
