package net.howson.phil.kaggle.santa.map;

public class SubPath {

	public final int[] steps;
	private final double[] interStepDistance;
	private final WorldMap map;
	public final int pathOffset;
	public final int originalBeforeCityId;
	public final int originalAfterCityId;
	public final double originalDistance;
	public final double originalDistanceWithoutOverlap;

	public SubPath(int pathOffset, int[] steps, int beforeCityId, int afterCityId, WorldMap map) {
		super();
		this.steps = steps;
		this.originalBeforeCityId = beforeCityId;
		this.originalAfterCityId = afterCityId;
		this.map = map;
		this.pathOffset = pathOffset;

		interStepDistance = new double[steps.length];

		double d0 = this.map.distanceNoPenalty(beforeCityId, steps[0]);
		interStepDistance[0] = d0;

		double d = 0;

		if ((pathOffset) % 10 == 0 && !map.isPrime(beforeCityId)) {
			d += d0 * 1.1;
		} else {
			d += d0;
		}

		for (int i = 1; i < steps.length; ++i) {
			double dist = this.map.distanceNoPenalty(steps[i - 1], steps[i]);

			interStepDistance[i] = dist;
			if ((i + pathOffset) % 10 == 0 && !map.isPrime(steps[i - 1])) {
				d += dist * 1.1;
			} else {
				d += dist;
			}

		}
		originalDistanceWithoutOverlap = d;
		double dL = this.map.distanceNoPenalty(afterCityId, steps[steps.length - 1]);
		if ((pathOffset + steps.length) % 10 == 0 && !map.isPrime(steps[steps.length - 1])) {
			d += dL * 1.1;
		} else {
			d += dL;
		}

		this.originalDistance = d;

	}

	public int getFirstIndex() {
		return steps[0];
	}

	public int getLastIndex() {
		return steps[steps.length - 1];
	}

	public void copyTo(int[] array, int idx, boolean reversed) {
		if (reversed) {
			int k = idx;
			for (int i = steps.length - 1; i >= 0; --i) {
				array[k] = steps[i];

				++k;

			}

		} else {
			System.arraycopy(steps, 0, array, idx, steps.length);
		}

	}

	public double distanceAt(int pathOffset, int beforeCityId, int afterCityId, boolean reversed, boolean overlap) {
		double d = 0;

		if (!reversed) {
			
			if (originalBeforeCityId == beforeCityId) {
				if ((pathOffset) % 10 == 0 && !map.isPrime(beforeCityId)) {
					d += interStepDistance[0] * 1.1;
				} else {
					d += interStepDistance[0];
				}
			} else {
				double d0 = this.map.distanceNoPenalty(beforeCityId, steps[0]);
				if ((pathOffset) % 10 == 0 && !map.isPrime(beforeCityId)) {
					d += d0 * 1.1;
				} else {
					d += d0;
				}
			}

			for (int i = 1; i < steps.length; ++i) {
				double dist = interStepDistance[i];
				if ((i + pathOffset) % 10 == 0 && !map.isPrime(steps[i - 1])) {
					d += dist * 1.1;
				} else {
					d += dist;
				}
			}

			if (overlap) {
				double dL = this.map.distanceNoPenalty(afterCityId, steps[steps.length - 1]);
				if ((pathOffset + steps.length) % 10 == 0 && !map.isPrime(steps[steps.length - 1])) {
					d += dL * 1.1;
				} else {
					d += dL;
				}
			}

		} else {
			double d0 = this.map.distanceNoPenalty(beforeCityId, steps[steps.length - 1]);
			if ((pathOffset) % 10 == 0 && !map.isPrime(beforeCityId)) {
				d += d0 * 1.1;
			} else {
				d += d0;
			}
			int k = 1;
			for (int i = steps.length - 1; i > 0; --i) {

				double dist = interStepDistance[i];
				if ((k + pathOffset) % 10 == 0 && !map.isPrime(steps[i])) {
					d += dist * 1.1;
				} else {
					d += dist;
				}
				k++;

			}

			if (overlap) {
				double dL = this.map.distanceNoPenalty(afterCityId, steps[0]);
				if ((pathOffset + steps.length) % 10 == 0 && !map.isPrime(steps[0])) {
					d += dL * 1.1;
				} else {
					d += dL;
				}
			}
		}

		return d;
	}

	@Override
	public String toString() {
		return "SubPath [pathOffset=" + pathOffset + ", originalBeforeCityId=" + originalBeforeCityId
				+ ", originalAfterCityId=" + originalAfterCityId + ", originalDistance=" + originalDistance
				+ ", originalDistanceWithoutOverlap=" + originalDistanceWithoutOverlap + "]";
	}

	public int firstStep(boolean b) {
		if (b) {
			return steps[steps.length - 1];
		}
		return steps[0];
	}

	public int lastStepCityId(boolean b) {
		if (b) {
			return steps[0];
		}
		return steps[steps.length - 1];

	}

}
