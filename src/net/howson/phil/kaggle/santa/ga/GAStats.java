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
	public double fixes;

	public synchronized void updateStats(boolean b, int g, int fixes) {
		if (b) {
			++convergence;
		}
		++runs;
		this.fixes += fixes;
		generations += g;

	}

	public synchronized void print() {
		System.out.println("--------------");
		System.out.println("Convergence rate : " + (convergence / runs));
		System.out.println("Average generations : " + (generations / runs));
		System.out.println("Fixes per run : " + (fixes/ runs));
		System.out.println("Overall convergence rate : " + (overallConvergence / overallRuns));
		System.out.println("Total runs : " + (overallRuns));
	}

	public synchronized void overallStats(boolean b) {
		overallRuns++;
		if (b)
			overallConvergence++;
	}

}
