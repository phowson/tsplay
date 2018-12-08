package net.howson.phil.kaggle.santa.map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldMap {

	private static final Logger logger = LogManager.getLogger(WorldMap.class);

	private final double[] cityX;
	private final double[] cityY;
	private final boolean[] isPrime;

	private int[] idMap;

	public WorldMap(final double[] cityX, final double[] cityY) {
		this.cityX = cityX;
		this.cityY = cityY;
		isPrime = new boolean[cityX.length];
		for (int i = 0; i < cityX.length; ++i) {
			isPrime[i] = intIsPrime(i);
		}
	}

	public WorldMap(final double[] cityX, final double[] cityY, final int[] idMap, final boolean[] isPrime) {
		this.cityX = cityX;
		this.cityY = cityY;
		this.idMap = idMap;
		this.isPrime = isPrime;
	}

	public CityPoint[] getCityPoints() {
		final CityPoint[] out = new CityPoint[cityX.length];
		for (int i = 0; i < cityX.length; ++i) {
			if (idMap != null) {
				out[i] = new CityPoint(idMap[i], cityX[i], cityY[i], isPrime[i]);
			} else {
				out[i] = new CityPoint(i, cityX[i], cityY[i], isPrime[i]);
			}
		}
		return out;
	}

	public void setIdMap(final int[] idMap) {
		this.idMap = idMap;
	}

	public int[] getIdMap() {
		return idMap;
	}

	private static boolean intIsPrime(final int i) {
		if (i <= 1) {
			return false;
		}

		final int d = (int) (Math.round(Math.sqrt(i)) + 1);

		for (int j = 2; j < d; ++j) {
			if (i % j == 0) {
				return false;
			}
		}
		return true;
	}

	public double distance(int a, int b, final int step) {
		if (b < 0 || b >= cityX.length) {
			b = 0;
		}

		if (a < 0 || a >= cityX.length) {
			a = 0;
		}

		final double o = (cityX[a] - cityX[b]);
		final double ad = (cityY[a] - cityY[b]);
		final double d = Math.sqrt((o * o) + (ad * ad));
 
		if (step % 10 == 0 && !isPrime[a]) {
			return d * 1.1;
		}

		return d;
	}

	public double pathDistanceRoundTripToZero(final int[] path) {

		double dist = pathDistanceFrom(0, 1, path);

		final int currentPosition = path[path.length - 1];
		dist += distance(currentPosition, 0, path.length + 1);

		return dist;

	}

	public double pathDistanceFrom(final int start, final int stepOffset, final int[] path) {

		int currentPosition = start;
		int step = 0;
		double dist = 0;
		while (step < path.length) {

			dist += distance(currentPosition, path[step], step + stepOffset);

			currentPosition = path[step];
			step++;
		}

		return dist;

	}

	public int size() {
		return this.cityX.length;
	}

	public boolean isPrime(final int i) {

		return isPrime[i];
	}

}
