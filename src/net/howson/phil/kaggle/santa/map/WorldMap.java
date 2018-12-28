package net.howson.phil.kaggle.santa.map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class WorldMap {

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
		final double o = (cityX[a] - cityX[b]);
		final double ad = (cityY[a] - cityY[b]);
		final double d = Math.sqrt((o * o) + (ad * ad));

		if (step % 10 == 0 && !isPrime[a]) {
			return d * 1.1;
		}

		return d;
	}
	
	


	public double distanceNoPenalty(int a, int b) {
		final double o = (cityX[a] - cityX[b]);
		final double ad = (cityY[a] - cityY[b]);
		final double d = Math.sqrt((o * o) + (ad * ad));
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

			double d = distance(currentPosition, path[step], step + stepOffset);;
//			if (Math.abs(d-8.3319)<1e-3) {
//				System.out.println("?");
//			}
			
			dist += d;

			currentPosition = path[step];
			step++;
		}

		return dist;

	}

	public int primeUtilisation(final int[] path) {

		int step = 8;
		int dist = 0;
		while (step < path.length) {

			if ((step+2) % 10 == 0 && isPrime[path[step]]) {
				++dist;
			}

			step+=10;
		}


		return (int) dist;

	}

	public int size() {
		return this.cityX.length;
	}

	public boolean isPrime(final int i) {

		return isPrime[i];
	}

	public double getX(int cityId) {
		return cityX[cityId];
	}

	public double getY(int cityId) {
		return cityY[cityId];
	}

	public void setPrime(int cityId) {
		isPrime[cityId] = true;
	}

}
