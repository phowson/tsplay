package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class InverseScorer implements PopulationScorer {

	private static final Logger logger = LogManager.getLogger(InverseScorer.class);

	@Override
	public double score(final GAPopulationElement e) {
		final double l = e.getLength();
		return 1.0 / l;
	}
}
