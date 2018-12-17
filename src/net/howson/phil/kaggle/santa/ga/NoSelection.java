package net.howson.phil.kaggle.santa.ga;

import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NoSelection implements CrossoverSelection {

	private static final Logger logger = LogManager.getLogger(NoSelection.class);
	private final SplittableRandom sr = new SplittableRandom();
	private int popSize;

	@Override
	public void init(GAPopulationElement[] population, int eliteSize) {
		this.popSize = population.length;
	}

	@Override
	public int nextElement(int eliteSize) {
		return sr.nextInt(popSize);
	}

}
