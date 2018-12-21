package net.howson.phil.kaggle.santa.ga;

import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gnu.trove.set.hash.TIntHashSet;

public final class BasicSafeCrossover3 implements CrossoverOperator {

	private static final Logger logger = LogManager.getLogger(BasicSafeCrossover3.class);

	private final SplittableRandom r = new SplittableRandom();
	private final TIntHashSet workingSet = new TIntHashSet();

	@Override
	public GAPopulationElement crossOver(final GAPopulationElement a, final GAPopulationElement b) {
		workingSet.clear();
		final int n = a.items.length;
		final int[] out = new int[n];

		for (int i = 0; i < n; ++i) {
			
			if (r.nextDouble()< 0.5) {
				workingSet.add(out[i] = a.items[i]);
			}

		}

		int i = 0;
		int j = 0;
		while (i < n) {
			if (out[i]==0) {
				if (workingSet.add(b.items[j])) {
					out[i] = b.items[j];
					++i;
				}
				++j;
			} else {
				++i;
			}
		}

		return new GAPopulationElement(a.env, out);
	}
}
