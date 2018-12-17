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
	public long startTime = System.currentTimeMillis();

	public synchronized void reset() {
		convergence = 0;
		runs = 0;
		generations = 0;

		overallRuns = 0;
		overallConvergence = 0;
		fixes = 0;
		startTime = System.currentTimeMillis();
	}

	public synchronized void updateStats(final boolean b, final int g, final int fixes) {
		if (b) {
			++convergence;
		}
		++runs;
		this.fixes += fixes;
		generations += g;

	}

	public synchronized void print() {
		System.out.println("--------------");
		final double cr = (convergence / runs);
		System.out.println("Convergence rate : " + cr);
		System.out.println("Average generations : " + (generations / runs));
		System.out.println("Fixes per run : " + (fixes / runs));
		System.out.println("Overall convergence rate : " + (overallConvergence / overallRuns));
		System.out.println("Total runs : " + (overallRuns));

		final long tds = System.currentTimeMillis() - startTime;
		final double td = tds / 60000.0;

		final double rpm = (overallRuns / td);
		System.out.println("Runs per minute : " + rpm);
		System.out.println("Convergence per minute : " + (cr * rpm));

	}

	public synchronized void overallStats(final boolean b) {
		overallRuns++;
		if (b)
			overallConvergence++;
	}

}
