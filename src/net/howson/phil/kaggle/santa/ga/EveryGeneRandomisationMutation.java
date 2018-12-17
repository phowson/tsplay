package net.howson.phil.kaggle.santa.ga;

import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class EveryGeneRandomisationMutation implements MutationOperator {

	private static final Logger logger = LogManager.getLogger(EveryGeneRandomisationMutation.class);

	private final SplittableRandom r = new SplittableRandom();

	private final double rate;

	public EveryGeneRandomisationMutation(final double rate) {
		this.rate = rate;
	}

	@Override
	public void mutate(final GAPopulationElement e) {
		final int n = e.items.length;
		for (int i = 0; i < n; ++i) {
			if (r.nextDouble() < rate) {
				final int b = r.nextInt(n);
				swap(e.items, i, b);
			}

		}
		e.resetLength();

	}

	private void swap(final int[] items, final int a, final int b) {
		final int t = items[a];
		items[a] = items[b];
		items[b] = t;
	}

}
