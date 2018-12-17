package net.howson.phil.kaggle.santa.ga;

import java.util.Arrays;
import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class GA {

	private static final Logger logger = LogManager.getLogger(GA.class);

	private final int mutationRateInt;
	private final GAPopulationElement[] population;
	private final GAPopulationElement[] newPopulation;
	private final double[] popScores;
	private final GAEnvironment environment;

	private CrossoverOperator crossoverOperator;

	private MutationOperator mutationOperator;

	private SplittableRandom sr = new SplittableRandom();

	private FixOperator fixOperator;

	private double totalScore;
	private int eliteSize;

	private MutationOperator twinMutationOperator;

	public GA(double mutationRate, double eliteRatio, int populationSize, GAEnvironment env,
			CrossoverOperator crossoverOperator, MutationOperator mutationOperator, 
			
			MutationOperator twinMutationOperator, 
			FixOperator fixOperator) {

		this.eliteSize = (int) Math.round(populationSize * eliteRatio);
		mutationRateInt = (int) Math.round(populationSize * mutationRate);
		population = new GAPopulationElement[populationSize];
		newPopulation = new GAPopulationElement[populationSize];
		popScores = new double[populationSize];
		this.environment = env;
		this.crossoverOperator = crossoverOperator;
		this.mutationOperator = mutationOperator;
		this.fixOperator = fixOperator;
		this.twinMutationOperator = twinMutationOperator;

	}

	public double getBestSoFar() {
		return population[0].getLength();
	}

	public void fix() {
		final int l = population.length;
		for (int i = 0; i < l; ++i) {
			if (!population[i].fixed) {
				fixOperator.fix(population[i]);
			}
		}
		Arrays.sort(population);
	}
	
	public void applyOneOffFix(FixOperator fixer) {
		final int l = population.length;
		for (int i = 0; i < l; ++i) {
			fixer.fix(population[i]);
		}
		Arrays.sort(population);
	}

	public void setup(GAPopulationElement seed) {

		TotalRandomisationMutation m = new TotalRandomisationMutation();
		for (int i = 0; i < population.length; ++i) {
			int[] a = new int[environment.permissableNodes.size()];
			int k = 0;
			for (int z : environment.permissableNodes.toArray()) {
				a[k++] = z;
			}

			population[i] = new GAPopulationElement(environment, a);
			m.mutate(population[i]);
		}
		if (seed != null) {
			population[0] = new GAPopulationElement(environment, Arrays.copyOf(seed.items, seed.items.length));
		}

		Arrays.sort(population);

	}

	public void runOneGeneration(boolean fix) {
		final int popSize = population.length;


		totalScore = 0;
		for (int i = 0; i < population.length; ++i) {
			final double length = population[i].getLength();
			totalScore += popScores[i] = 1.0 / (length * length);
		}

		for (int i = eliteSize; i < popSize; ++i) {
			int a = nextElement();
			int b = nextElement();
			newPopulation[i] = crossoverOperator.crossOver(population[a], population[b]);
		}

		System.arraycopy(newPopulation, eliteSize, population, eliteSize, popSize - eliteSize);

		for (int i = 0; i < mutationRateInt; ++i) {
			int a = sr.nextInt(popSize - eliteSize) + eliteSize;
			mutationOperator.mutate(population[a]);
		}

		if (fix) {
			fix();
		} else {
			Arrays.sort(population);
		}

		boolean hadDups = false;
		// Eliminate duplicates from population
		GAPopulationElement last = population[0];
		for (int i = 1; i < popSize; ++i) {
			GAPopulationElement c = population[i];
			if (last.getLength() == c.getLength()) {
				twinMutationOperator.mutate(c);
				hadDups = true;
			} else {
				last = c;
			}
		}

		if (hadDups) {
			Arrays.sort(population);
		}

	}

	private int nextElement() {
		double v = sr.nextDouble(totalScore);
		int k = 0;
		final int n = popScores.length;
		while (k < n - 1) {
			v -= popScores[k];
			if (v < 0) {
				break;
			}
			++k;
		}
		return k;
	}

	public GAPopulationElement getBestItem() {
		return population[0];
	}

	public void insert(GAPopulationElement orig) {
		population[population.length - 1] = new GAPopulationElement(environment,
				Arrays.copyOf(orig.items, orig.items.length));

		Arrays.sort(population);
	}

}
