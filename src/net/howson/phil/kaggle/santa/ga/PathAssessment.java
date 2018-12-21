package net.howson.phil.kaggle.santa.ga;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.map.WorldMap;

public class PathAssessment {

	public class PathItem {
		public int pathIdx;
		public int cityId;
		public boolean isTenthStep;
		public boolean isPrime;

	}

	public static class MapSector {
		List<PathItem> items = new ArrayList<>();
		List<PathItem> unusedPrimes = new ArrayList<>();

		public PathItem findClosestUnusedPrime(double x, double y) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private static final Logger logger = LogManager.getLogger(PathAssessment.class);

	private static final int TABLE_WIDTH = 100;

	private static final int TABLE_HEIGHT = 100;

	private WorldMap worldMap;
	private double pathLen;
	private int[] path;

	private MapSector[][] fastLookupTable;
	private List<PathItem> nonPrimeTenthSteps = new ArrayList<>();

	private double xRatio;

	private double yRatio;

	public PathAssessment(WorldMap map) {
		this.worldMap = map;
		double maxX = 0;
		double maxY = 0;
		for (int i = 0; i < worldMap.size(); ++i) {
			maxX = Math.max(worldMap.getX(i), maxX);
			maxY = Math.max(worldMap.getY(i), maxY);

		}

		xRatio = (double) TABLE_WIDTH / (maxX + 1);
		yRatio = (double) TABLE_HEIGHT / (maxY + 1);

	}

	public List<PathItem> getNonPrimeTenthSteps() {
		return nonPrimeTenthSteps;
	}

	public PathItem getClosestUnusedPrimes(int pathIdx, int minPathDistance) {

		List<PathItem> pi = getClosestUnusedPrimes(pathIdx);

		PathItem closest = null;
		double dist = 0;

		for (PathItem i : pi) {
			if (Math.abs(i.pathIdx - pathIdx) > minPathDistance) {
				if (closest == null) {
					closest = i;
					dist = worldMap.distanceNoPenalty(i.cityId, path[pathIdx]);
				} else {
					double d = worldMap.distanceNoPenalty(i.cityId, path[pathIdx]);
					if (d < dist) {
						dist = d;
						closest = i;
					}
				}
			}
		}
		return closest;

	}

	public List<PathItem> getClosestUnusedPrimes(int pathIdx) {

		List<PathItem> out = new ArrayList<PathAssessment.PathItem>();
		int cityIndex = this.path[pathIdx];
		double x = worldMap.getX(cityIndex);
		double y = worldMap.getY(cityIndex);
		int tx = (int) (x * xRatio);
		int ty = (int) (y * yRatio);
		MapSector f = fastLookupTable[tx][ty];

		out.addAll(f.unusedPrimes);
		if (ty > 0) {
			f = fastLookupTable[tx][ty - 1];
			out.addAll(f.unusedPrimes);
		}

		if (ty < TABLE_HEIGHT-1) {
			f = fastLookupTable[tx][ty + 1];
			out.addAll(f.unusedPrimes);
		}

		if (tx > 0) {
			f = fastLookupTable[tx - 1][ty];
			out.addAll(f.unusedPrimes);

			if (ty > 0) {
				f = fastLookupTable[tx - 1][ty - 1];
				out.addAll(f.unusedPrimes);
			}

			if (ty < TABLE_HEIGHT-1) {
				f = fastLookupTable[tx - 1][ty + 1];
				out.addAll(f.unusedPrimes);
			}
		}

		if (tx < TABLE_WIDTH-1) {
			f = fastLookupTable[tx + 1][ty];
			out.addAll(f.unusedPrimes);

			if (ty > 0) {
				f = fastLookupTable[tx + 1][ty - 1];
				out.addAll(f.unusedPrimes);
			}

			if (ty < TABLE_HEIGHT-1) {
				f = fastLookupTable[tx + 1][ty + 1];
				out.addAll(f.unusedPrimes);
			}
		}
		return out;
	}

	public void updateWithPath(net.howson.phil.kaggle.santa.path.Path p) {
		if (pathLen != p.length) {
			nonPrimeTenthSteps.clear();
			fastLookupTable = new MapSector[TABLE_WIDTH][TABLE_HEIGHT];

			for (int i = 0; i < TABLE_WIDTH; ++i) {
				for (int j = 0; j < TABLE_WIDTH; ++j) {
					fastLookupTable[i][j] = new MapSector();
				}
			}

			pathLen = p.length;
			path = p.steps;

			int i = 0;
			while (i < path.length) {
				boolean isTenthStep = (i + 2) % 10 == 0;
				boolean isPrime = worldMap.isPrime(path[i]);

				PathItem pi = new PathItem();
				pi.cityId = path[i];
				pi.isPrime = isPrime;
				pi.isTenthStep = isTenthStep;
				pi.pathIdx = i;

				double x = worldMap.getX(pi.cityId);
				double y = worldMap.getY(pi.cityId);

				MapSector f = fastLookupTable[(int) (x * xRatio)][(int) (y * yRatio)];
				f.items.add(pi);
				if (isPrime && !isTenthStep) {
					f.unusedPrimes.add(pi);
				}

				if (!isPrime && isTenthStep) {
					nonPrimeTenthSteps.add(pi);
				}

				i++;
			}

		}
	}

}
