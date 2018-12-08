package net.howson.phil.kaggle.santa.ga;

import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

public class BasicSafeCrossover2 implements CrossoverOperator {

	private static final Logger logger = LogManager.getLogger(BasicSafeCrossover2.class);

	private final SplittableRandom r = new SplittableRandom();

	@Override
	public GAPopulationElement crossOver(GAPopulationElement a, GAPopulationElement b) {
		TIntHashSet workingSet = new TIntHashSet();

		final int n = a.items.length;
		int[] out = new int[n];
		int crossOverPoint = r.nextInt(a.items.length);
		int crossOverWidth = r.nextInt(n - crossOverPoint);

		for (int i = crossOverPoint; i < crossOverPoint + crossOverWidth; ++i) {
			workingSet.add(out[i] = a.items[i]);

		}

		int i = 0;
		int j = 0;
		while (i < crossOverPoint) {
			if (workingSet.add(b.items[j])) {
				out[i] = b.items[j];
				++i;
			}
			++j;
		}

		i = crossOverPoint + crossOverWidth;

		while (i < n) {
			if (workingSet.add(b.items[j])) {
				out[i] = b.items[j];
				++i;
			}
			++j;
		}

		return new GAPopulationElement(a.env, out);
	}
}
