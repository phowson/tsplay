package net.howson.phil.kaggle.santa.ga;

import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocalRandomisationMutation implements MutationOperator {

	private static final Logger logger = LogManager.getLogger(LocalRandomisationMutation.class);

	private final int amount;
	private final int locale;

	private final SplittableRandom r = new SplittableRandom();

	public LocalRandomisationMutation(int amount, int locale) {
		this.amount = amount;
		this.locale = locale;
	}

	@Override
	public void mutate(GAPopulationElement e) {
		final int n = e.items.length;
		final int z = r.nextInt(amount) + 1;
		for (int i = 0; i < z; ++i) {
			int a = r.nextInt(n);
			int b = Math.min(n - 1 - a, 1 + r.nextInt(locale));
			swap(e.items, a, a + b);

		}
		e.resetLength();

	}

	private void swap(int[] items, int a, int b) {
		int t = items[a];
		items[a] = items[b];
		items[b] = t;
	}

}
