package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.BruteForcePath;

public class PermFixer implements FixOperator {

	private static final Logger logger = LogManager.getLogger(PermFixer.class);

	private final int[] pathSection;
	private final int sectionWidth;
	private final BruteForcePath bfp;
	private final GAEnvironment env;

	public PermFixer(final int sectionWidth, final GAEnvironment env) {
		this.pathSection = new int[sectionWidth];
		this.sectionWidth = sectionWidth;
		this.env = env;
		this.bfp = new BruteForcePath(env.map, sectionWidth);
	}

	private double tryPermutationsAt(final int[] path, final int i, final double origDist) {

		System.arraycopy(path, i, pathSection, 0, sectionWidth);

		bfp.bruteForceSolve(pathSection, env.pathOffset + i, path[i - 1], path[i + sectionWidth]);
		final double newDist = origDist - bfp.getOriginalPathLen() + bfp.getBestLength();

		if (bfp.getBestLength() < bfp.getOriginalPathLen()) {
			System.arraycopy(bfp.getBestPath(), 0, path, i, sectionWidth);
			return newDist;
		}

		return origDist;
	}

	@Override
	public void fix(final GAPopulationElement e) {

		double dist = e.getLength();
		final int n = e.items.length - sectionWidth - 1;
		double origdist;
		do {
			origdist = dist;
			for (int i = 1; i < n; ++i) {
				dist = tryPermutationsAt(e.items, i, dist);

			}
		} while (origdist != dist);

		e.resetLength();
		e.fixed = true;
	}

}
