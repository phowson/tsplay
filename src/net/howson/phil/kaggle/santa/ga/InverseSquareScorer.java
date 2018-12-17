package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class InverseSquareScorer implements PopulationScorer {

	private static final Logger logger = LogManager.getLogger(InverseSquareScorer.class);

	@Override
	public double score(final GAPopulationElement e) {
		final double length = e.getLength();
		return 1.0 / (length * length);
	}

}
