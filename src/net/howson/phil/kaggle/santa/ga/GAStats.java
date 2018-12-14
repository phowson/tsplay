package net.howson.phil.kaggle.santa.ga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GAStats {

	private static final Logger logger = LogManager.getLogger(GAStats.class);

	public double convergence;
	public double runs;
	public double generations;

	public double overallRuns;
	public double overallConvergence;

	public synchronized void updateStats(boolean b, int g) {
		if (b) {
			++convergence;
		}
		++runs;
		generations += g;

	}

	public synchronized void print() {
		System.out.println("--------------");
		System.out.println("Convergence rate : " + (convergence / runs));
		System.out.println("Average generations : " + (generations / runs));
		System.out.println("Overall convergence rate : " + (overallConvergence / overallRuns));
	}

	public synchronized void overallStats(boolean b) {
		overallRuns++;
		if (b)
			overallConvergence++;
	}

}
