package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.map.WorldMap;

public class GAPopulationElement implements Comparable<GAPopulationElement> {

	private static final Logger logger = LogManager.getLogger(GAPopulationElement.class);

	final GAEnvironment env;
	final int[] items;
	private double length;

	public GAPopulationElement(GAEnvironment env, int[] items) {
		super();
		this.env = env;
		this.items = items;
	}

	public void resetLength() {
		length = 0;
	}

	public double getLength() {

		if (length == 0) {
			WorldMap map = env.map;
			double d = map.pathDistanceFrom(env.beforeIdx, env.pathOffset, items);
			d += map.distance(items[items.length - 1], env.afterIdx, items.length + env.pathOffset);
			length = d;
		}
		return length;
	}

	@Override
	public int compareTo(GAPopulationElement o) {
		double length = this.getLength();
		double length2 = o.getLength();
		if (length2 > length) {
			return -1;
		}
		if (length2 < length) {
			return 1;
		}
		return 0;
	}

}
