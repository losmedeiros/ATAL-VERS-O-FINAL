
package main.strategies;

import java.util.List;
import java.util.Random;

import main.game.Player;
import main.game.map.Map;
import main.game.map.Point;
import main.game.map.TreasureChest;

public class Sort implements Strategy {

	private Random random;

	public Sort() {
		this.random = new Random();
	}

	@Override
	public Point evaluatePossbileNextStep(List<Point> possibleNextSteps, Map map) {
		if (possibleNextSteps.isEmpty()) {
			return null; // No valid moves available
		}
		// Select a random valid next step
		int index = random.nextInt(possibleNextSteps.size());
		return possibleNextSteps.get(index);
	}
}
