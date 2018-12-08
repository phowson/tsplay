package net.howson.phil.kaggle.santa;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.FacArray.FacFunctor;
import net.howson.phil.kaggle.santa.map.WorldMap;

public class BruteForcePath implements FacFunctor {

	private static final Logger logger = LogManager.getLogger(BruteForcePath.class);

	private final WorldMap map;
	private double bestLength;
	private final int[] bestPath;
	private final FacArray fa;
	private int pathOffset;

	private int beforeIdx;

	private int afterIdx;

	private double originalPathLen;

	public BruteForcePath(final WorldMap map, final int pathLen) {
		this.map = map;
		bestPath = new int[pathLen];
		fa = new FacArray(pathLen);
	}

	public void bruteForceSolve(final int[] path, final int pathOffset, final int beforeIdx, final int afterIdx) {

		bestLength = Double.POSITIVE_INFINITY;
		this.pathOffset = pathOffset;
		this.beforeIdx = beforeIdx;
		this.afterIdx = afterIdx;

		originalPathLen = getPathLen(path, pathOffset, beforeIdx, afterIdx);
		fa.applyFac(path, this);

	}

	public double getOriginalPathLen() {
		return originalPathLen;
	}

	public double getBestLength() {
		return bestLength;
	}

	public int[] getBestPath() {
		return bestPath;
	}

	@Override
	public void onArray(final int[] arr) {

		final double d = getPathLen(arr, pathOffset, beforeIdx, afterIdx);

		if (d < bestLength) {
			this.bestLength = d;
			System.arraycopy(arr, 0, this.bestPath, 0, arr.length);
		}

	}

	public double getPathLen(final int[] arr, final int pathOffset, final int beforeIdx, final int afterIdx) {
		double d = map.pathDistanceFrom(beforeIdx, pathOffset, arr);
		d += map.distance(arr[arr.length - 1], afterIdx, arr.length + pathOffset);
		return d;
	}

}
