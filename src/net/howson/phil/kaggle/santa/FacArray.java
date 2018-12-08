package net.howson.phil.kaggle.santa;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FacArray {

	public static interface FacFunctor {
		public void onArray(int[] arr);
	}

	private static final Logger logger = LogManager.getLogger(FacArray.class);
	private final int[] comb ;
	private final int width;
	
	public FacArray(final int width) {
		comb = new int[width];
		this.width = width;
	}

	public void applyFac(final int[] inputs, final FacFunctor func) {


		facInternal(0, comb, inputs, func);

	}

	private void facInternal(final int i, final int[] comb, final int[] inputs, final FacFunctor func) {
		


		for (int j = 0; j < width; ++j) {
			if (comb[j] == 0) {
				comb[j] = inputs[i];
				final int i1 = i + 1;
				facInternal(i1, comb, inputs, func);
				if (i1 == width) {
					func.onArray(comb);
					comb[j] = 0;
					return;
				}
				comb[j] = 0;
			}
		}
	}

	public static void main(final String[] args) {
		new FacArray(8).applyFac(new int[] { 1, 2, 3, 4, 5, 6, 7, 8 }, new FacFunctor() {

			@Override
			public void onArray(final int[] arr) {
				System.out.println(Arrays.toString(arr));
			}
		});
	}

}
