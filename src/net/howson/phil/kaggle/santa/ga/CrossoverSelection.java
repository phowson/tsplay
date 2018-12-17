package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface CrossoverSelection {

	void init(GAPopulationElement[] population, int eliteSize);

	int nextElement(int eliteSize);

}
