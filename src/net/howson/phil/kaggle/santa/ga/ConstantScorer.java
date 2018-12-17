package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ConstantScorer implements PopulationScorer {

	private static final Logger logger = LogManager.getLogger(ConstantScorer.class);

	@Override
	public double score(final GAPopulationElement e) {
		
		return 1.0 ;
	}
}
