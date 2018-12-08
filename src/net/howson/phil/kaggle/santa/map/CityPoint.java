package net.howson.phil.kaggle.santa.map;

public final class CityPoint {

	public CityPoint(final int i, final double d, final double e, final boolean isPrime) {
		this.id = i;
		this.x = d;
		this.y = e;
		this.isPrime = isPrime;
	}

	public final int id;
	public final double x;
	public final double y;
	public boolean isPrime;

	@Override
	public String toString() {
		return "CityPoint [id=" + id + ", x=" + x + ", y=" + y + ", isPrime=" + isPrime + "]";
	}

}
