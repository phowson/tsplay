package net.howson.phil.kaggle.santa.path;

import java.util.Arrays;

public class Path {

	public final int[] steps;
	public final double length;

	public Path(final int[] steps, final double length) {
		this.steps = steps;
		this.length = length;
	}

	public static Path copy(final int[] steps, final double length) {
		return new Path(Arrays.copyOf(steps, steps.length), length);
	}

	public static Path copy(final Path p) {
		return new Path(Arrays.copyOf(p.steps, p.steps.length), p.length);
	}

}
