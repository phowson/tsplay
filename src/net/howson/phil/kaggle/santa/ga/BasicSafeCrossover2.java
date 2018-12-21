package net.howson.phil.kaggle.santa.ga;

import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gnu.trove.set.hash.TIntHashSet;

public final class BasicSafeCrossover2 implements CrossoverOperator {

	private static final Logger logger = LogManager.getLogger(BasicSafeCrossover2.class);

	private final SplittableRandom r = new SplittableRandom();
	private final TIntHashSet workingSet = new TIntHashSet();

	@Override
	public GAPopulationElement crossOver(final GAPopulationElement a, final GAPopulationElement b) {
		workingSet.clear();
		final int n = a.items.length;
		final int[] out = new int[n];
		final int crossOverPoint = r.nextInt(a.items.length-1);
		final int crossOverWidth = r.nextInt(n - crossOverPoint-1)+1;

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
