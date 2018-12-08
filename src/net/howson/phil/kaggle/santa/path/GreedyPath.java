package net.howson.phil.kaggle.santa.path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.GeometryItemDistance;
import com.vividsolutions.jts.index.strtree.ItemDistance;
import com.vividsolutions.jts.index.strtree.STRtree;

import net.howson.phil.kaggle.santa.map.CityPoint;
import net.howson.phil.kaggle.santa.map.WorldMap;

public class GreedyPath {

	private static final Logger logger = LogManager.getLogger(GreedyPath.class);

	public int[] createGreedyPath(final WorldMap map) {
		final int[] out = new int[map.size() - 1];

		final STRtree tree = new STRtree();
		final ItemDistance id = new GeometryItemDistance();
		final CityPoint[] points = map.getCityPoints();
		final Envelope[] e = new Envelope[points.length];
		for (int i = 0; i < points.length; ++i) {
			final CityPoint p = points[i];
			e[i] = new Envelope(new Coordinate(p.x, p.y));
			tree.insert(e[i], p);
		}

		final int current = 0;
		while (!tree.isEmpty()) {
			final CityPoint o = (CityPoint) tree.nearestNeighbour(e[current], points[current], id);
			System.out.println(o);
			tree.remove(new Envelope(new Coordinate(o.x, o.y)), o);
		}

		return out;
	}

}
