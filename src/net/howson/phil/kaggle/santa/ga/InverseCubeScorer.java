package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class InverseCubeScorer implements PopulationScorer {

	private static final Logger logger = LogManager.getLogger(InverseCubeScorer.class);

	@Override
	public double score(final GAPopulationElement e) {
		final double l = e.getLength();
		return 1.0 / (l * l * l);
	}
}
