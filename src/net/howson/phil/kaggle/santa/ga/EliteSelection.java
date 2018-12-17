package net.howson.phil.kaggle.santa.ga;

import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EliteSelection implements CrossoverSelection {

	private static final Logger logger = LogManager.getLogger(EliteSelection.class);
	private final SplittableRandom sr = new SplittableRandom();

	private final double eliteProbability;
	private final int popSize;

	public EliteSelection(double eliteProbability, int populationSize) {
		super();
		this.eliteProbability = eliteProbability;
		this.popSize = populationSize;
	}

	@Override
	public void init(GAPopulationElement[] population, int eliteSize) {

	}

	@Override
	public int nextElement(int eliteSize) {
		if (sr.nextDouble() < eliteProbability)
			return sr.nextInt(eliteSize);
		else {
			return sr.nextInt(popSize - eliteSize) + eliteSize;
		}
	}

}
