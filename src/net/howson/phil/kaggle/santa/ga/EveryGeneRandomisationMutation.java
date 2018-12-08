package net.howson.phil.kaggle.santa.ga;

import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EveryGeneRandomisationMutation implements MutationOperator {

	private static final Logger logger = LogManager.getLogger(EveryGeneRandomisationMutation.class);

	private final SplittableRandom r = new SplittableRandom();

	private double rate;

	public EveryGeneRandomisationMutation(double rate) {
		this.rate = rate;
	}

	@Override
	public void mutate(GAPopulationElement e) {
		final int n = e.items.length;
		for (int i = 0; i < n; ++i) {
			if (r.nextDouble() < rate) {
				int b = r.nextInt(n);
				swap(e.items, i, b);
			}

		}
		e.resetLength();

	}

	private void swap(int[] items, int a, int b) {
		int t = items[a];
		items[a] = items[b];
		items[b] = t;
	}

}
