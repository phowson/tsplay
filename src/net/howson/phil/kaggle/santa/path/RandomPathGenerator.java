package net.howson.phil.kaggle.santa.path;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RandomPathGenerator {

	private static final Logger logger = LogManager.getLogger(RandomPathGenerator.class);

	private final Random r = new Random();

	public int[] generate(final int nodes, final int off) {

		final int[] out = new int[nodes];
		for (int i = 0; i < nodes; ++i) {
			out[i] = i + off;
		}

		for (int i = 0; i < nodes; ++i) {
			final int j = r.nextInt(nodes - i) + i;
			if (j != i) {
				final int t = out[i];
				out[i] = out[j];
				out[j] = t;
			}
		}
		return out;
	}

}
