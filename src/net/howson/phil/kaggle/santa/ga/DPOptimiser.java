package net.howson.phil.kaggle.santa.ga;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import gnu.trove.list.array.TIntArrayList;
import net.howson.phil.kaggle.santa.map.MapLoader;
import net.howson.phil.kaggle.santa.map.WorldMap;
import net.howson.phil.kaggle.santa.path.PathLoader;

public class DPOptimiser {

	static class ArrayTour {
		WorldMap tsp;
		int[] tour_; // including trailing 0

		public ArrayTour(int[] myStyleTour, WorldMap tsp) {
			this.tsp = tsp;
			tour_ = new int[myStyleTour.length + 2];
			setFromMyStyleTour(myStyleTour);
		}

		public void setFromMyStyleTour(int[] myStyleTour) {
			System.arraycopy(myStyleTour, 0, tour_, 1, myStyleTour.length);
		}

		public int[] getMyTourStyle() {
			int[] t = new int[tour_.length - 2];
			System.arraycopy(tour_, 1, t, 0, t.length);
			return t;
		}

		public double scoreF() {
			return tsp.pathDistanceRoundTripToZero(getMyTourStyle());
		}

		public void copyTo(int[] pathArray) {
			System.arraycopy(tour_, 1, pathArray, 0, pathArray.length);

		}

		public void putIntoTour(int pathOffset, int[] items) {
			System.arraycopy(items, 0, tour_, pathOffset, items.length);

		}

		public void copyTo(int pathOffset, int[] pathArray) {
			System.arraycopy(tour_, pathOffset, pathArray, 0, pathArray.length);

		}

	};

	// The main DP solver class.
	private static class DPSolver {
		private static final int MAXK = 20;

		DPSolver(int K, boolean quiet) {
			this.K = K;
			this.quiet = quiet;

		}

		private int K; // Number of nodes to permute
		private boolean quiet; // Do not pring debug information
		private WorldMap tsp; // Saved reference to TSP problem data
		private int N; // Number of cities

		// Original city indexes in the optimized segment including
		// the two fixed surrounding cities: segment[0] and segment[K+1].
		// segment[1..K] are the nodes that we're trying to permute.
		private final int[] segment = new int[MAXK + 2];

		// Segment's positions in the tour mod 10.
		private int[] segment_mod = new int[MAXK + 2];

		// Distance matrix between segment[] cities.
		private double[][] dist = new double[MAXK + 2][MAXK + 2];

		// DP memoization table
		// [bitmask of unvisited nodes][last visited node index]
		private double[][] dp = new double[1 << MAXK][MAXK];

		// Position of segment[0] in the tour (fixed node to the left of the
		// segment)
		private int left_idx_;

		private double orig_total; // Original score of the segment
		private double best_total; // Best found optimized score of the segment
		private TIntArrayList perm = new TIntArrayList(); // Reconstructed
															// permutation
		// of nodes

		public int builtin_popcount(int mask) {
			int c = 0;
			while (mask != 0) {
				if ((mask & 1) == 1) {
					++c;
				}
				mask = mask >> 1;
			}
			return c;
		}

		// Optimizes a segment of the tour: tour[left_idx+1 .. left_idx+K].
		// left_idx is index of the fixed node immediately to the left of
		// optimized segment. Out of bounds indexes get wrapped around.
		// Returns true if improvement is possible. Call Apply() to commit it.
		boolean canOptimize(final ArrayTour tour, int left_idx) {
			tsp = tour.tsp;
			N = tsp.size();
			left_idx = (left_idx % N + N) % N; // wrap around to [0, N) range
			left_idx_ = left_idx;

			for (int i = 0; i < K + 2; i++) {
				segment[i] = tour.tour_[(left_idx_ + i) % N];
				segment_mod[i] = ((left_idx_ + i) % N) % 10;
			}

			for (int i = 0; i < K + 2; i++) {
				for (int j = i + 1; j < K + 2; j++) {
					dist[i][j] = dist[j][i] = tsp.distanceNoPenalty(segment[i], segment[j]);
				}
				dist[i][i] = 0;
			}

			// Boundary case: all nodes in the segment are visited (mask=0),
			// with given index of the node at which we stopped last.
			for (int last = 0; last < K; last++) {
				dp[0][last] = tsp.distanceSMOD(segment[last + 1], segment[K + 1], segment_mod[K]);
			}

			// Optimize every subproblem in the order of increasing number of
			// unvisited cities, up to mask of all 1's which corresponds to
			// the full problem.
			for (int mask = 1; mask < (1 << K); mask++) {
				for (int last = 0; last < K; last++) {
					if ((mask & (1 << last)) != 0)
						continue;
					dp[mask][last] = DoSubproblem(false, mask, last);
				}
			}

			orig_total = 0;
			for (int i = 0; i <= K; i++) {
				orig_total += tsp.distanceSMOD(segment[i], segment[i + 1], segment_mod[i]);
			}

			best_total = DoSubproblem(false, (1 << K) - 1, -1);
			return best_total < orig_total - WorldMap.EPS;
		}

		// Solve a subproblem.
		// * mask: bitmask of still unvisited nodes (segment[1..K])
		// * last: index of last visited node
		// Indexing in both mask and last: 0=segment[1], ..., K=1=segment[K].
		// Special case: last=-1 refers to segment[0] for the starting case.
		// * reconstruct: template flag whether we're reconstructing solution
		// into perm array. Ain't nobody got time to reimplement this code
		// twice, so we're doing it all in same method.
		double DoSubproblem(boolean reconstruct, int mask, int last) {
			if (reconstruct) {
				if (last != -1)
					perm.add(last + 1);
				if (mask == 0)
					return 0;
			}

			// Position of last city in the segment[] array, infer from counting
			// bits in mask.
			int last_pos = K - builtin_popcount(mask);

			// Is the edge from last city going to be penalized?
			boolean penalized = tsp.penalizedAtMod(segment[last + 1], segment_mod[last_pos]);

			// Try every possible next city, pick one leading to least final
			// cost.
			// Repeat twice when reconstructing solution to get the argmin.
			double best = Double.POSITIVE_INFINITY;
			for (int cycle = 0; cycle < (reconstruct ? 2 : 1); cycle++) {
				for (int next = 0; next < K; next++) {
					if ((mask & (1 << next)) == 0)
						continue;

					// Fix city 0 at its original position.
					if (segment[next + 1] == 0 && left_idx_ + last_pos != N - 1)
						continue;

					double cost = dp[mask ^ (1 << next)][next] + dist[last + 1][next + 1]
							+ (penalized ? dist[last + 1][next + 1] / 10 : 0);
					if (cost < best) {
						best = cost;
					}

					if (reconstruct && cycle == 1 && cost == best) {
						return DoSubproblem(true, mask ^ (1 << next), next);
					}
				}
			}

			return best;
		}

		// Applies the best change found by CanOptimize() to the given tour.
		// The tour may be a different object than the one given to
		// CanOptimize(),
		// but the segment that was optimized must be the same and at same
		// position.
		void Apply(ArrayTour tour) {
			perm.clear();
			DoSubproblem(true, (1 << K) - 1, -1);

			for (int i = 0; i < K; i++) {
				int idx = (left_idx_ + 1 + i) % N;
				tour.tour_[idx] = segment[perm.get(i)];
			}

			double new_total = 0;
			for (int i = 0; i <= K; i++) {
				int idx = (left_idx_ + i) % N;
				int next_idx = (idx + 1) % N;
				new_total += tsp.distanceSMOD(tour.tour_[idx], tour.tour_[next_idx], idx % 10);
			}

			if (Math.abs(new_total - best_total) > WorldMap.EPS) {
				System.err.println("Reconstruction error: " + new_total + " vs " + best_total);
			}

			if (!quiet) {
				double d = (orig_total - new_total);
				System.out.printf("\r%.2f: %6d %5.2f  ", tour.scoreF(), left_idx_, d);
				for (int i : perm.toArray())
					System.out.printf(" %d", i);
				System.out.printf("\n");

			}
		}
	}

	private DPSolver dps;
	private ArrayTour tour;

	public DPOptimiser(int K, int[] path, WorldMap map) {
		this.dps = new DPSolver(K, true);
		this.tour = new ArrayTour(path, map);
	}

	public void setTour(int[] path) {
		this.tour.setFromMyStyleTour(path);
	}

	public boolean solveAt(int base_pos) {
		boolean success = dps.canOptimize(tour, base_pos);
		if (success) {
			dps.Apply(tour);
		}
		return success;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {

		int K = 10;
		final WorldMap map = new MapLoader().load(new File("./data/cities.csv"));
		final int[] path = new PathLoader().load(new File("./data/out.csv"));
		DPOptimiser optimiser = new DPOptimiser(K, path, map);

		for (int base_pos = -25; base_pos <= map.size() + 25; base_pos++) {
			// Uncomment if you'd like to see some progress in the console. This
			// doesn't work well in kaggle kernels.
			System.out.printf("[%d]\n", base_pos);
			optimiser.solveAt(base_pos);

		}
	}

	public void copyTo(int[] pathArray) {
		tour.copyTo(pathArray);
	}

	public void putIntoTour(int pathOffset, int[] items) {
		tour.putIntoTour(pathOffset, items);
	}

	public void copyTo(int pathOffset, int[] items) {
		tour.copyTo(pathOffset, items);

	}

}
