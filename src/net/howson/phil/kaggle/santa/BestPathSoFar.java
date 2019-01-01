package net.howson.phil.kaggle.santa;

import java.io.File;
import java.io.FileNotFoundException;

import net.howson.phil.kaggle.santa.path.Path;

public class BestPathSoFar {

	private Path bestPath;
	private String name;
	private String mainName = "out.csv";

	public BestPathSoFar(final Path bestPath) {
		this.bestPath = bestPath;
		
	}
	
	public BestPathSoFar(final Path bestPath, String name) {
		this.bestPath = bestPath;
		this.name = name;
		
	}
	
	public BestPathSoFar(final Path bestPath, String name, String mainName) {
		this.bestPath = bestPath;
		this.name = name;
		this.mainName =mainName;
		
	}

	public synchronized Path get() {
		return bestPath;
	}

	public synchronized void update(final int[] path, final double x) {
		if (x < bestPath.length) {
			System.out.println(
					System.currentTimeMillis() + " Updated best path " + x + " " + Thread.currentThread().getName());
			bestPath = Path.copy(path, x);
			try {
				new PathWriter().save(bestPath.steps, new File("./data/" + mainName));
				
				if (name!=null) {
					new PathWriter().save(bestPath.steps, new File("./data/"+name));	
				}
				
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			}

		}

	}

}