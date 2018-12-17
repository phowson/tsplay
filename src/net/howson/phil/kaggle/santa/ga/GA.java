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

	private final GAEnvironment environment;

	private final CrossoverOperator crossoverOperator;

	private final MutationOperator mutationOperator;

	private final SplittableRandom sr = new SplittableRandom();

	private final FixOperator fixOperator;

	private double totalScore;
	private final int eliteSize;

	private final MutationOperator twinMutationOperator;
	private final CrossoverSelection crossoverSelection;

	public GA(final double mutationRate, final double eliteRatio, final int populationSize, final GAEnvironment env,
			final CrossoverOperator crossoverOperator, final MutationOperator mutationOperator,

			final MutationOperator twinMutationOperator, final FixOperator fixOperator,
			final CrossoverSelection crossoverSelection) {

		this.eliteSize = (int) Math.round(populationSize * eliteRatio);
		mutationRateInt = (int) Math.round(populationSize * mutationRate);
		population = new GAPopulationElement[populationSize];
		newPopulation = new GAPopulationElement[populationSize];
		this.environment = env;
		this.crossoverOperator = crossoverOperator;
		this.mutationOperator = mutationOperator;
		this.fixOperator = fixOperator;
		this.twinMutationOperator = twinMutationOperator;
		this.crossoverSelection = crossoverSelection;

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

	public void applyOneOffFix(final FixOperator fixer) {
		final int l = population.length;
		for (int i = 0; i < l; ++i) {
			fixer.fix(population[i]);
		}
		Arrays.sort(population);
	}

	public void setup(final GAPopulationElement seed) {

		final TotalRandomisationMutation m = new TotalRandomisationMutation();
		for (int i = 0; i < population.length; ++i) {
			final int[] a = new int[environment.permissableNodes.size()];
			int k = 0;
			for (final int z : environment.permissableNodes.toArray()) {
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

	public void runOneGeneration(final boolean fix) {

		final int popSize = population.length;

		crossoverSelection.init(population, eliteSize);

		for (int i = eliteSize; i < popSize; ++i) {
			final int a = crossoverSelection.nextElement(eliteSize);
			final int b = crossoverSelection.nextElement(eliteSize);
			newPopulation[i] = crossoverOperator.crossOver(population[a], population[b]);
		}

		System.arraycopy(newPopulation, eliteSize, population, eliteSize, popSize - eliteSize);

		for (int i = 0; i < mutationRateInt; ++i) {
			final int a = sr.nextInt(popSize - eliteSize) + eliteSize;
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
			final GAPopulationElement c = population[i];
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

	
	public GAPopulationElement getBestItem() {
		return population[0];
	}

	public void insert(final GAPopulationElement orig) {
		population[population.length - 1] = new GAPopulationElement(environment,
				Arrays.copyOf(orig.items, orig.items.length));

		Arrays.sort(population);
	}

}
