package net.howson.phil.kaggle.santa.nopt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.BestPathSoFar;
import net.howson.phil.kaggle.santa.ga.PathAssessment;
import net.howson.phil.kaggle.santa.ga.PathAssessment.PathItem;
import net.howson.phil.kaggle.santa.map.MapLoader;
import net.howson.phil.kaggle.santa.map.SubPath;
import net.howson.phil.kaggle.santa.map.WorldMap;
import net.howson.phil.kaggle.santa.path.Path;
import net.howson.phil.kaggle.santa.path.PathLoader;

public class ThreeOptPlay implements Runnable {

	private static final int N = 3;
	private static final Logger logger = LogManager.getLogger(NOptPlay.class);
	private static final int MINPATHDIST = 3;
	private static final int MINTOTALPATHDIST = 40;
	private final WorldMap map;

	private final BestPathSoFar bpsf;
	private final int startIdx;
	private final int endIdx;

	private PathAssessment pathAssessment;

	public ThreeOptPlay(final WorldMap map, final BestPathSoFar bpsf2, int startIdx, int endIdx) {
		this.map = map;
		this.bpsf = bpsf2;
		this.pathAssessment = new PathAssessment(map);
		this.startIdx = startIdx;
		this.endIdx = endIdx;

	}

	public static void main(final String[] args) throws FileNotFoundException, IOException {

		final WorldMap map = new MapLoader().load(new File("./data/cities.csv"));
		final int[] path = new PathLoader().load(new File("./data/out.csv"));
		final double initialLength = map.pathDistanceRoundTripToZero(path);
		System.out.println("Started at : " + initialLength);
		final BestPathSoFar bpsf = new BestPathSoFar(new Path(path, initialLength), "4opt.csv");

		int nThreads = 8;

		double perThread = (path.length - 2) / (double) nThreads;

		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < nThreads; ++i) {
			Thread t = new Thread(new ThreeOptPlay(map, bpsf, (int) Math.floor(i * perThread) + 1,
					(int) Math.floor((i + 1) * perThread)));
			threads.add(t);
			t.start();
		}

		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				logger.error("Unexpected exception", e);
			}
		}
	}

	@Override
	public void run() {

		System.out.println("Prime utilisation : " + map.primeUtilisation(bpsf.get().steps));
		// while (true) {
		for (int t = 0; t < 2; ++t) {
			for (int i = startIdx; i < endIdx; ++i) {

				final Path inputPath = bpsf.get();
				pathAssessment.updateWithPath(inputPath);
				if (i % 1000 == 0) {
					System.out.println(i + ", best = " + inputPath.length + ", time = " + t);
				}
				int max = inputPath.steps.length;

				TreeMap<Double, PathItem> closest = pathAssessment.getClosestNodes(i, MINPATHDIST);

				PathItem[] closestPoints = new PathItem[N];
				int k = 0;
				for (Iterator<Map.Entry<Double, PathItem>> it = closest.entrySet().iterator(); it.hasNext();) {
					Entry<Double, PathItem> e = it.next();
					PathItem v = e.getValue();

					boolean anyTooClose = false;

					for (int z = 0; z < k; ++z) {
						if (Math.abs(v.pathIdx - closestPoints[z].pathIdx) < 2) {
							anyTooClose = true;
							break;
						}
					}
					if (anyTooClose || v.pathIdx == 0 || v.pathIdx == max) {
						continue;
					}

					if (e.getKey() > 50 && k == 0) {
						break;
					}

					// System.out.println(e.getKey());
					closestPoints[k++] = v;
					if (k == closestPoints.length) {
						break;
					}
				}

				if (k == closestPoints.length) {
					// We found enough to continue

					runPermutations(closestPoints, i, inputPath, t == 0);
				}
			}

		}

	}
	// }

	private final BitSet bitSet = new BitSet();
	private final SubPath[] subpaths = new SubPath[N];

	private void runPermutations(PathItem[] closestPoints, int idx, Path inPath, boolean shiftOne) {
		int[] path = inPath.steps;
		int[] indexes = new int[N + 1];
		indexes[0] = idx;
		for (int i = 0; i < closestPoints.length; ++i) {
			indexes[i + 1] = closestPoints[i].pathIdx;
		}

		Arrays.sort(indexes);
		if (indexes[indexes.length - 1] - indexes[0] < MINTOTALPATHDIST) {
			return;
		}

		if (shiftOne) {
			// for (int i = 1; i<indexes.length-1; ++i) {
			// indexes[i] = indexes[i]+1;
			// }
			indexes[1] = indexes[1] + 1;
		}

		// System.out.println("Path indicies : " + Arrays.toString(indexes));
		for (int i = 0; i < indexes.length - 1; ++i) {

			int[] steps = new int[indexes[i + 1] - indexes[i]];
			System.arraycopy(path, indexes[i], steps, 0, steps.length);
			subpaths[i] = new SubPath(indexes[i] + 1, steps, path[indexes[i] - 1], path[indexes[i + 1]], map);
		}

		// System.out.println("SubPaths : " + Arrays.toString(subpaths));

		double subPathLength = 0;
		for (int i = 0; i < subpaths.length - 1; ++i) {
			subPathLength += subpaths[i].originalDistanceWithoutOverlap;
		}
		subPathLength += subpaths[subpaths.length - 1].originalDistance;

		double lengthWithoutSubPath = inPath.length - subPathLength;

		boolean[] invert = new boolean[N];
		bitSet.clear();

		double bestLen = inPath.length;
		int bestA = -1;
		int bestB = -1;
		int bestC = -1;
		boolean bestInvert0 = false;
		boolean bestInvert1 = false;
		boolean bestInvert2 = false;

		for (int a = 0; a < subpaths.length; ++a) {
			bitSet.set(a);
			for (int b = 0; b < subpaths.length; ++b) {
				if (!bitSet.get(b)) {
					bitSet.set(b);
					for (int c = 0; c < subpaths.length; ++c) {
						if (!bitSet.get(c)) {
							bitSet.set(c);

							for (int x = 0; x < 2; ++x) {
								for (int y = 0; y < 2; ++y) {
									for (int z = 0; z < 2; ++z) {
										invert[0] = x == 0;
										invert[1] = y == 0;
										invert[2] = z == 0;

										double len = tryPerm(a, b, c, inPath, subpaths, lengthWithoutSubPath, invert);
										if (len < bestLen) {
											bestLen = len;
											bestA = a;
											bestB = b;
											bestC = c;
											bestInvert0 = invert[0];
											bestInvert1 = invert[1];
											bestInvert2 = invert[2];
										}
									}

								}
							}

							bitSet.clear(c);
						}
					}
					bitSet.clear(b);
				}
			}

			bitSet.clear(a);
		}

		if (bestLen < inPath.length - 1e-5)

		{
			int origPathOffset = subpaths[0].pathOffset;

			int[] p = Arrays.copyOf(inPath.steps, inPath.steps.length);

			int off = origPathOffset - 1;
			subpaths[bestA].copyTo(p, off, bestInvert0);

			off += subpaths[bestA].steps.length;
			subpaths[bestB].copyTo(p, off, bestInvert1);

			off += subpaths[bestB].steps.length;
			subpaths[bestC].copyTo(p, off, bestInvert2);

			double realLength = map.pathDistanceRoundTripToZero(p);

			bpsf.update(p, realLength);

		}

	}

	private double tryPerm(int a, int b, int c, Path inPath, SubPath[] subpaths, double lengthWithoutSubPath,
			boolean[] invert) {
		// if (a == 0 && b == 1 && c == 2 && !invert[0] && !invert[1] &&
		// !invert[2]) {
		// return;
		// }

		double newLength = lengthWithoutSubPath;
		int origPathOffset = subpaths[0].pathOffset;
		int origBeforeCityId = subpaths[0].originalBeforeCityId;
		int origAfterCityId = subpaths[subpaths.length - 1].originalAfterCityId;

		int pathOffset = origPathOffset;
		newLength += subpaths[a].distanceAt(origPathOffset, origBeforeCityId, subpaths[b].firstStep(invert[1]),
				invert[0], false);

		pathOffset += subpaths[a].steps.length;
		newLength += subpaths[b].distanceAt(pathOffset, subpaths[a].lastStepCityId(invert[0]),
				subpaths[c].firstStep(invert[2]), invert[1], false);

		pathOffset += subpaths[b].steps.length;
		newLength += subpaths[c].distanceAt(pathOffset, subpaths[b].lastStepCityId(invert[1]), origAfterCityId,
				invert[2], true);

		return newLength;

		// System.out.printf("%d %d %d\n", a, b, c);
		// System.out.println(newLength);
		// if (newLength < inPath.length) {
		// System.out.println("?");
		// }

		// int[] p = Arrays.copyOf(inPath.steps, inPath.steps.length);
		// subpaths[a].copyTo(p, origPathOffset - 1, invert[0]);
		// subpaths[b].copyTo(p, origPathOffset - 1 + subpaths[a].steps.length,
		// invert[1]);
		// subpaths[c].copyTo(p, origPathOffset - 1 + subpaths[a].steps.length +
		// subpaths[b].steps.length, invert[2]);
		//
		// double realLength = map.pathDistanceRoundTripToZero(p);
		// System.out.println(realLength - newLength);

	}

}
