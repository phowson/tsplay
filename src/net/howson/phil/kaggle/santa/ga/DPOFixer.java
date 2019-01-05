package net.howson.phil.kaggle.santa.ga;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.map.WorldMap;

public class DPOFixer implements FixOperator {

	private static final Logger logger = LogManager.getLogger(DPOFixer.class);
	private DPOptimiser dpo;
	private int k;
	
	public DPOFixer(int K, int [] initialPath, WorldMap map) {
		dpo = new DPOptimiser(K, initialPath, map);
		this.k = K;
	}
	
	public void reset(int [] path) {
		dpo.setTour(path);
	}
	

	@Override
	public void fix(GAPopulationElement e) {
		dpo.putIntoTour(e.env.pathOffset, e.items);
		int j = e.env.pathOffset;
		boolean ok = false;
		int n = -1+e.items.length/k;
		for (int i = 0; i<n; ++i) {			
			ok |= dpo.solveAt(j);
			j+= k;
		}
		if (ok) {
			dpo.copyTo(e.env.pathOffset, e.items);
			e.resetLength();
		}

	}
}
