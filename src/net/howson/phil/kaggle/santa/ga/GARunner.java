package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.map.WorldMap;

public class GARunner {

	private static final Logger logger = LogManager.getLogger(GARunner.class);

	public int unFixedGenerations = 10;
	public int fixInterval = 5;
	public int fixInterval2 = 50;
	public int maxDupRuns = 200;
	public int canTerminateAtGen = 0;
	public FixOperator fixoperator2;

	public void run(final WorldMap map, final int[] path, final int pathIndex, final int sectionWidth,
			GAFactory factory, GAStats gaStats, int retries, boolean bestOnly) {
		final int[] pathSection = new int[sectionWidth];
		System.arraycopy(path, pathIndex, pathSection, 0, sectionWidth);
		final GAEnvironment gae = new GAEnvironment(map, path[pathIndex - 1], path[pathIndex + sectionWidth],
				pathIndex + 1, pathSection);
		double initialL = new GAPopulationElement(gae, pathSection).getLength();
		GAPopulationElement absoluteBest = null;
		final GA ga = factory.create(gae, sectionWidth);

		for (int t = 0; t < retries; ++t) {

			int g = 0;
			ga.setup(null);

			double lastBest = 0;
			int duplicateRuns = 0;
			boolean canFix = false;
			int fixes = 0;

			while (true) {

				if (g == unFixedGenerations) {
					canFix = true;
				}

				final boolean f = canFix && g % fixInterval == 0;
				ga.runOneGeneration(f);
				if (f) {
					++fixes;
				}
				
				if (fixoperator2!=null && canFix && g % fixInterval2 ==0 ) {
//					System.out.println("Fix2");
					ga.applyOneOffFix(fixoperator2);
//					System.out.println("Fix2Done");
				}
				

				final double best = ga.getBestSoFar();

				if (lastBest == best && canFix && g>canTerminateAtGen) {
					++duplicateRuns;
				} else {
					duplicateRuns = 0;
				}
				lastBest = best;

				if (duplicateRuns == maxDupRuns) {
					if (fixoperator2!=null) {
						ga.applyOneOffFix(fixoperator2);
					}
					
					break;
				}
				++g;
			}
			final double best = ga.getBestSoFar();
			if (absoluteBest == null || best < absoluteBest.getLength()) {
				absoluteBest = ga.getBestItem();
			}
			
			gaStats.updateStats(best <= initialL + WorldMap.EPS, g, fixes);
		}
		
		
		
		gaStats.overallStats(absoluteBest.getLength() <= initialL);

		if (!bestOnly || absoluteBest.getLength() < initialL)
			
			System.arraycopy(absoluteBest.items, 0, path, pathIndex, sectionWidth);

	}

}
