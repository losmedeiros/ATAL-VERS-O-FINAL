package main.strategies;

import java.util.List;
import main.game.map.Map;
import main.game.map.Point;

public class FewerObstacles implements Strategy {

	@Override
	public Point evaluatePossbileNextStep(List<Point> possibleNextStep, Map map) {
		int minObstacleCount = Integer.MAX_VALUE;
		Point bestPoint = null;

		// Avalia todos os próximos pontos possíveis e encontra o que tem menos obstáculos
		for (Point nextPoint : possibleNextStep) {
			int obstacleCount = countAdjacentObstacles(nextPoint, map);
			if (obstacleCount < minObstacleCount) {
				minObstacleCount = obstacleCount;
				bestPoint = nextPoint;
				break;
			}
		}
		return bestPoint;
	}

	private int countAdjacentObstacles(Point point, Map map) {
		int count = 0;
		int x = point.getPositionX();
		int y = point.getPositionY();

		// Obtém as dimensões do mapa
		int[] mapSize = map.getScenarioSize();  // [largura, altura]
		int mapWidth = mapSize[0];
		int mapHeight = mapSize[1];

		// Verifica todos os 8 quadrantes ao redor do ponto (incluindo diagonais)
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				if (dx == 0 && dy == 0) continue; // Ignora o ponto central

				int newX = x + dx;
				int newY = y + dy;

				// Verifica se as novas coordenadas estão dentro dos limites do mapa
				if (newX >= 0 && newX < mapWidth && newY >= 0 && newY < mapHeight) {
					// Obtém o conteúdo do ponto adjacente
					String content = map.get(new Point(newX, newY));
					// Considera como obstáculo qualquer coisa diferente de "*"
					if (content != null && !content.equals("*")) {
						count++; // Conta como um obstáculo
					}
				}
				break;
			}
		}
		return count;
	}
}