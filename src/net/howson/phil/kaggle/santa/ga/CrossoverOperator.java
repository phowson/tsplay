package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface CrossoverOperator {
	public GAPopulationElement crossOver(GAPopulationElement a, GAPopulationElement b);
}
