package net.howson.phil.kaggle.santa.ga;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SplittableRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.map.WorldMap;
import net.howson.phil.kaggle.santa.path.Path;

public class PathSectionSelector {

	private static final Logger logger = LogManager.getLogger(PathSectionSelector.class);

	private final static class Item implements Comparable<Item> {
		final double score;
		final int location;

		public Item(double score, int location) {
			super();
			this.score = score;
			this.location = location;
		}

		@Override
		public int compareTo(Item o) {
			if (o.score > score) {
				return -1;
			}

			if (o.score < score) {
				return 1;
			}
			return 0;
		}
		
		@Override
		public String toString() {

			return Double.toString(score);
		}

	}

	private double length;
	private int[] path;

	private final WorldMap map;
	private final int sectionWidth;
	private final int selectNumber;

	private final Queue<Item> items = new PriorityQueue<>();

	private final SplittableRandom sr = new SplittableRandom();
	private Item[] itemsArray;

	public PathSectionSelector(WorldMap map, int sectionWidth, int selectNumber) {
		super();
		this.map = map;
		this.sectionWidth = sectionWidth;
		this.selectNumber = selectNumber;
	}

	public int selectNextIndex(Path best) {
		if (best.length != length) {
			recreateTable(best);
		}

		Item r = itemsArray[sr.nextInt(itemsArray.length)];

		return r.location + sr.nextInt(sectionWidth) - (sectionWidth/2);

	}
	

	public double[] calculatePenalties(WorldMap map, final int[] path, int numBuckets) {

		final double[] penaltiesOut = new double[numBuckets];
		int stepOffset = 1;
		int currentPosition = 0;
		int step = 0;
		double ratio = (double) penaltiesOut.length / (double) path.length;

		while (step < path.length) {

			double dist = map.distance(currentPosition, path[step], step + stepOffset);
			double distNoP = map.distanceNoPenalty(currentPosition, path[step]);
			
			penaltiesOut[(int) Math.floor(step * ratio)] += computePenalty(dist, distNoP);
			currentPosition = path[step];
			step++;
		}

		currentPosition = path[path.length - 1];
		double dist = map.distance(currentPosition, 0, path.length + 1);
		double distNoP = map.distanceNoPenalty(currentPosition, 0);

		penaltiesOut[penaltiesOut.length - 1] += computePenalty(dist, distNoP);

		return penaltiesOut;
	}

	private double computePenalty(double dist, double distNoP) {
		if (dist - distNoP > 1e-5) {
			return //1000+ 
					(dist - distNoP);	
		}
		return 0;
		
	}

	private void recreateTable(Path best) {
		this.length = best.length;
		this.path = best.steps;
		int nBuckets = 1 + path.length / sectionWidth;

		double[] penalties = this.calculatePenalties(this.map, this.path, nBuckets);
		double ratio = (double) path.length / (double) penalties.length;

		items.clear();
		for (int i = 0; i < penalties.length; ++i) {
			int index = (int) Math.floor(i * ratio);
			if (items.size() == selectNumber && items.peek().score < penalties[i]) {
				items.remove();
				items.add(new Item(penalties[i], index));
			} else if (items.size() < selectNumber) {
				items.add(new Item(penalties[i], index));
			}

		}

		itemsArray = items.toArray(new Item[0]);
		Arrays.sort(itemsArray);
		
//		for (int i =0 ;i <itemsArray.length; ++i) {
//			System.out.println(itemsArray[i].location +"," + itemsArray[i].score);
//		}
//		

	}

}
