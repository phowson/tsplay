package net.howson.phil.kaggle.santa.ga;

import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

public class BasicSafeCrossover implements CrossoverOperator {

	private static final Logger logger = LogManager.getLogger(BasicSafeCrossover.class);

	private final SplittableRandom r = new SplittableRandom();

	@Override
	public GAPopulationElement crossOver(GAPopulationElement a, GAPopulationElement b) {
		TIntHashSet workingSet = new TIntHashSet();

		final int n = a.items.length;
		int[] out = new int[n];
		int crossOverPoint = r.nextInt(a.items.length);

		for (int i = 0; i < n; ++i) {
			workingSet.add(a.items[i]);
		}

		for (int i = 0; i < crossOverPoint; ++i) {
			final int z = a.items[i];
			workingSet.remove(z);
			out[i] = z;
		}

		for (int i = crossOverPoint; i < n; ++i) {
			final int z = b.items[i];
			workingSet.remove(z);
			out[i] = z;
		}

		if (!workingSet.isEmpty()) {
			final TIntIterator it = workingSet.iterator();

			final TIntHashSet workingSet2 = new TIntHashSet();
			for (int i = 0; i < n; ++i) {
				if (!workingSet2.add(out[i])) {
					out[i] = it.next();
				}
			}
		}

		return new GAPopulationElement(a.env, out);
	}
}
