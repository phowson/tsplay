package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultGAFactory implements GAFactory {

	private static final Logger logger = LogManager.getLogger(DefaultGAFactory.class);

	private final double eliteProportion = 0.3;
	private final int populationSize = 125;
	private final double mutationRate = 0.10;
	private final double mutationProportion = 0.15;

	@Override
	public GA create(GAEnvironment gae, int sectionWidth) {
		final GA ga = new GA(mutationRate, eliteProportion, populationSize, gae, new BasicSafeCrossover2(),
				new BasicRandomisationMutation((int) (sectionWidth * mutationProportion)),
				new BasicRandomisationMutation((int) (sectionWidth)), new SwapFixer(gae),
				new ProportionalSelection(populationSize, new InverseScorer()));

		return ga;
	}
}
