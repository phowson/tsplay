package net.howson.phil.kaggle.santa.nopt;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.howson.phil.kaggle.santa.ga.GATest2;

public class Rerunner {

	private static final Logger logger = LogManager.getLogger(Rerunner.class);
	
	public static void main(String[] args) throws FileNotFoundException, IOException {

		//FourOptPlay.main(args);
		
		NOptPointAndPathPlay.main(args);
		NOptPlay.main(args);
		GATest2.main(args);
	}
}
