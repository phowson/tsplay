package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gnu.trove.set.hash.TIntHashSet;
import net.howson.phil.kaggle.santa.map.WorldMap;

public class GAEnvironment {

	private static final Logger logger = LogManager.getLogger(GAEnvironment.class);

	public final WorldMap map;
	public final int beforeIdx;
	public final int afterIdx;
	public final int pathOffset;

	public TIntHashSet permissableNodes;

	public GAEnvironment(final WorldMap map, final int beforeIdx, final int afterIdx, final int pathOffset,
			final int[] initialPath) {
		super();
		this.map = map;
		this.beforeIdx = beforeIdx;
		this.afterIdx = afterIdx;
		this.pathOffset = pathOffset;

		permissableNodes = new TIntHashSet();

		for (final int i : initialPath) {
			permissableNodes.add(i);
		}
	}

}
