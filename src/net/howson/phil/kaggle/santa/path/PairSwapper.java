package net.howson.phil.kaggle.santa.path;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.map.WorldMap;

public class PairSwapper {

	private static final Logger logger = LogManager.getLogger(PairSwapper.class);
	private final Random r = new Random();

	public double pairWiseRandomMutate(final WorldMap map, final int[] path, final double origLen, final int range) {

		final int i = r.nextInt(path.length - 4) + 1;
		final int j = Math.min(path.length - 2, r.nextInt(range) + i + 1);

		final double l = computeSwapDistance(path, origLen, i, j, map, 0);
		swap(path, i, j);

		return l;
	}

	public double pairWiseRandomMutateAroundCenter(final WorldMap map, final int[] path, final double origLen,
			final int range, final int iters) {

		final int z = r.nextInt(path.length - 4) + 1;

		double l = origLen;
		for (int k = 0; k < iters; ++k) {
			final int i = Math.min(path.length - 2, r.nextInt(range) + z);
			final int j = Math.min(path.length - 2, i + 1);

			l = computeSwapDistance(path, l, i, j, map, 0);
			swap(path, i, j);
		}

		return l;
	}

	public double pairWiseSideBySide(final WorldMap map, final int[] path, final double origLen, final int range) {

		final int i = r.nextInt(path.length - 4) + 1;
		final int j = Math.min(path.length - 2, i + 1);

		final double l = computeSwapDistance(path, origLen, i, j, map, 0);
		swap(path, i, j);

		return l;
	}

	public double mutateAround(final WorldMap map, final int[] path, final double origLen, final int j,
			final int distance) {

		double bestlen = origLen;

		int best = j;

		final int min = Math.max(1, j - distance);
		final int max = Math.min(path.length - 1, j + distance);

		for (int i = min; i < max - 1; ++i) {

			final double l = computeSwapDistance(path, origLen, i, j, map, 0);
			if (l < bestlen) {
				bestlen = l;
				best = i;
			}

		}

		if (bestlen < origLen) {
			swap(path, best, j);
			return bestlen;
		}
		return bestlen;
	}

	public static final double computeSwapDistance(final int[] path, final double origLen, int i, int j,
			final WorldMap map, int pathOffset) {

		double l = origLen;
		if (i == j) {
			return l;
		} else if (i - j > 1 || j - i > 1) {
			final int pi = path[i];
			final int pj = path[j];
			l -= map.distance(path[i - 1], pi, i + 1 + pathOffset);
			l -= map.distance(pi, path[i + 1], i + 2 + pathOffset);

			l -= map.distance(path[j - 1], pj, j + 1 + pathOffset) + map.distance(pj, path[j + 1], j + 2 + pathOffset);

			///////////////////////
			l += map.distance(path[i - 1], pj, i + 1 + pathOffset);
			l += map.distance(pj, path[i + 1], i + 2 + pathOffset);

			l += map.distance(path[j - 1], pi, j + 1 + pathOffset);
			l += map.distance(pi, path[j + 1], j + 2 + pathOffset);

		} else if (i < j) {
			final int pi = path[i];
			final int pj = path[j];
			final double ab = map.distance(path[i - 1], pi, i + 1 + pathOffset);
			final double bc = map.distance(pi, path[i + 1], i + 2 + pathOffset);
			final double cd = map.distance(pj, path[j + 1], j + 2 + pathOffset);

			final double ac = map.distance(path[i - 1], pj, i + 1 + pathOffset);
			final double cb = map.distance(pj, pi, i + 2 + pathOffset);
			final double bd = map.distance(pi, path[j + 1], j + 2 + pathOffset);

			l = l - ab + ac - cd + bd - bc + cb;

		} else if (i > j) {
			int z = i;
			i = j;
			j = z;

			final int pi = path[i];
			final int pj = path[j];
			final double ab = map.distance(path[i - 1], pi, i + 1 + pathOffset);
			final double bc = map.distance(pi, path[i + 1], i + 2 + pathOffset);
			final double cd = map.distance(pj, path[j + 1], j + 2 + pathOffset);

			final double ac = map.distance(path[i - 1], pj, i + 1 + pathOffset);
			final double cb = map.distance(pj, pi, i + 2 + pathOffset);
			final double bd = map.distance(pi, path[j + 1], j + 2 + pathOffset);

			l = l - ab + ac - cd + bd - bc + cb;

		}

		return l;
	}

	public static final double computeSwapDistance0(int prevIdx, final int[] path, final double origLen, int i, int j,
			final WorldMap map, int pathOffset) {

		double l = origLen;

		int pIMinus1 = prevIdx;
		int pIPlus1 = path[i + 1];

		if (Math.abs(i - j) > 1) {
			final int pi = path[i];
			final int pj = path[j];
			l -= map.distance(pIMinus1, pi, i + 1 + pathOffset);
			l -= map.distance(pi, pIPlus1, i + 2 + pathOffset);

			l -= map.distance(path[j - 1], pj, j + 1 + pathOffset) + map.distance(pj, path[j + 1], j + 2 + pathOffset);

			///////////////////////
			l += map.distance(pIMinus1, pj, i + 1 + pathOffset);
			l += map.distance(pj, pIPlus1, i + 2 + pathOffset);

			l += map.distance(path[j - 1], pi, j + 1 + pathOffset);
			l += map.distance(pi, path[j + 1], j + 2 + pathOffset);

		} else if (i < j) {
			final int pi = path[i];
			final int pj = path[j];
			final double ab = map.distance(pIMinus1, pi, i + 1 + pathOffset);
			final double bc = map.distance(pi, pIPlus1, i + 2 + pathOffset);
			final double cd = map.distance(pj, path[j + 1], j + 2 + pathOffset);

			final double ac = map.distance(pIMinus1, pj, i + 1 + pathOffset);
			final double cb = map.distance(pj, pi, i + 2 + pathOffset);
			final double bd = map.distance(pi, path[j + 1], j + 2 + pathOffset);

			l = l - ab + ac - cd + bd - bc + cb;

		} else if (i > j) {
			int z = i;
			i = j;
			j = z;

			pIMinus1 = path[i - 1];
			pIPlus1 = path[i + 1];

			final int pi = path[i];
			final int pj = path[j];
			final double ab = map.distance(pIMinus1, pi, i + 1 + pathOffset);
			final double bc = map.distance(pi, pIPlus1, i + 2 + pathOffset);
			final double cd = map.distance(pj, path[j + 1], j + 2 + pathOffset);

			final double ac = map.distance(pIMinus1, pj, i + 1 + pathOffset);
			final double cb = map.distance(pj, pi, i + 2 + pathOffset);
			final double bd = map.distance(pi, path[j + 1], j + 2 + pathOffset);

			l = l - ab + ac - cd + bd - bc + cb;

		}

		return l;
	}

	public static final double computeSwapDistanceL(int postIdx, final int[] path, final double origLen, int i, int j,
			final WorldMap map, int pathOffset) {

		double l = origLen;

		int pIMinus1 = path[i - 1];
		int pIPlus1 = postIdx;

		int pJPlus1 = path[j + 1];
		if (Math.abs(i - j) > 1) {
			final int pi = path[i];
			final int pj = path[j];
			l -= map.distance(pIMinus1, pi, i + 1 + pathOffset);
			l -= map.distance(pi, pIPlus1, i + 2 + pathOffset);

			l -= map.distance(path[j - 1], pj, j + 1 + pathOffset) + map.distance(pj, pJPlus1, j + 2 + pathOffset);

			///////////////////////
			l += map.distance(pIMinus1, pj, i + 1 + pathOffset);
			l += map.distance(pj, pIPlus1, i + 2 + pathOffset);

			l += map.distance(path[j - 1], pi, j + 1 + pathOffset);
			l += map.distance(pi, pJPlus1, j + 2 + pathOffset);

		} else if (i < j) {
			final int pi = path[i];
			final int pj = path[j];
			final double ab = map.distance(pIMinus1, pi, i + 1 + pathOffset);
			final double bc = map.distance(pi, pIPlus1, i + 2 + pathOffset);
			final double cd = map.distance(pj, pJPlus1, j + 2 + pathOffset);

			final double ac = map.distance(pIMinus1, pj, i + 1 + pathOffset);
			final double cb = map.distance(pj, pi, i + 2 + pathOffset);
			final double bd = map.distance(pi, pJPlus1, j + 2 + pathOffset);

			l = l - ab + ac - cd + bd - bc + cb;

		} else if (i > j) {
			int z = i;
			i = j;
			j = z;

			pIMinus1 = path[i - 1];
			pIPlus1 = path[i + 1];

			pJPlus1 = postIdx;

			final int pi = path[i];
			final int pj = path[j];
			final double ab = map.distance(pIMinus1, pi, i + 1 + pathOffset);
			final double bc = map.distance(pi, pIPlus1, i + 2 + pathOffset);
			final double cd = map.distance(pj, pJPlus1, j + 2 + pathOffset);

			final double ac = map.distance(pIMinus1, pj, i + 1 + pathOffset);
			final double cb = map.distance(pj, pi, i + 2 + pathOffset);
			final double bd = map.distance(pi, pJPlus1, j + 2 + pathOffset);

			l = l - ab + ac - cd + bd - bc + cb;

		}

		return l;
	}

	public static final void swap(final int[] path, final int i, final int j) {
		final int t = path[i];
		path[i] = path[j];
		path[j] = t;

	}

}
