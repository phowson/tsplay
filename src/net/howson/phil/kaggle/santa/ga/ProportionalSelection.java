package net.howson.phil.kaggle.santa.ga;

import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProportionalSelection implements CrossoverSelection {

	private static final Logger logger = LogManager.getLogger(ProportionalSelection.class);
	private final double[] popScores;
	private double totalScore;
	private final PopulationScorer scorer;
	private final SplittableRandom sr = new SplittableRandom();

	public ProportionalSelection(int popSize, PopulationScorer scorer) {
		this.popScores = new double[popSize];
		this.scorer = scorer;
	}

	@Override
	public void init(GAPopulationElement[] population, int eliteSize) {
		totalScore = 0;
		for (int i = 0; i < population.length; ++i) {

			totalScore += popScores[i] = scorer.score(population[i]);
		}
	}

	@Override
	public int nextElement(int eliteSize) {
		double v = sr.nextDouble(totalScore);
		int k = 0;
		final int n = popScores.length;
		while (k < n - 1) {
			v -= popScores[k];
			if (v < 0) {
				break;
			}
			++k;
		}
		return k;
	}

}
