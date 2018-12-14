package net.howson.phil.kaggle.santa.ga;

public interface CrossoverOperator {
	public GAPopulationElement crossOver(GAPopulationElement a, GAPopulationElement b);
}
