package net.howson.phil.kaggle.santa.ga;

import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BasicRandomisationMutation implements MutationOperator {

	private static final Logger logger = LogManager.getLogger(BasicRandomisationMutation.class);

	private final int amount;

	private final SplittableRandom r = new SplittableRandom();

	public BasicRandomisationMutation(int amount) {
		this.amount = amount;
	}

	@Override
	public void mutate(GAPopulationElement e) {
		final int n = e.items.length;
		final int z = r.nextInt(amount) + 1;
		for (int i = 0; i < z; ++i) {
			int a = r.nextInt(n);
			int b = r.nextInt(n);
			swap(e.items, a, b);

		}
		e.resetLength();

	}

	private void swap(int[] items, int a, int b) {
		int t = items[a];
		items[a] = items[b];
		items[b] = t;
	}

}
