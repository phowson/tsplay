package net.howson.phil.kaggle.santa.ga;

import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class TotalRandomisationMutation implements MutationOperator {

	private static final Logger logger = LogManager.getLogger(TotalRandomisationMutation.class);

	private final SplittableRandom r = new SplittableRandom();

	@Override
	public void mutate(final GAPopulationElement e) {
		final int n = e.items.length;
		for (int i = 0; i < n - 1; ++i) {
			final int b = r.nextInt(n - i - 1) + i + 1;
			swap(e.items, i, b);
		}

		e.resetLength();

	}

	private void swap(final int[] items, final int a, final int b) {
		final int t = items[a];
		items[a] = items[b];
		items[b] = t;
	}

}
