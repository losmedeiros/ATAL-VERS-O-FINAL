package main.strategies;

import java.util.List;
import java.util.Stack;
import java.util.HashSet;
import java.util.Set;
import main.game.map.Map;
import main.game.map.Point;
import main.game.map.TreasureChest;

public class Rollback implements Strategy {
    private Stack<Point> visitedPoints;
    private Set<String> visitedPositions;
    private int maxConsecutiveRollbacks;
    private int currentConsecutiveRollbacks;

    public Rollback() {
        this.visitedPoints = new Stack<>();
        this.visitedPositions = new HashSet<>();
        this.maxConsecutiveRollbacks = 3; // Limite de rollbacks consecutivos para evitar loops
        this.currentConsecutiveRollbacks = 0;
    }

    @Override
    public Point evaluatePossbileNextStep(List<Point> possibleNextSteps, Map map) {
        if (possibleNextSteps.isEmpty()) {
            return null;
        }

        Point currentPosition = map.getRobotLocation();
        String currentPosKey = getPositionKey(currentPosition);

        // Registra a posição atual se ainda não foi visitada
        if (!visitedPositions.contains(currentPosKey)) {
            visitedPositions.add(currentPosKey);
            visitedPoints.push(currentPosition);
        }

        // 1. Primeira prioridade: procura por baú de tesouro adjacente
        for (Point nextPoint : possibleNextSteps) {
            String content = map.get(nextPoint);
            if (TreasureChest.CHARACTER.equals(content)) {
                return nextPoint;
            }
        }

        // 2. Segunda prioridade: move para posição não visitada mais próxima
        Point unvisitedPoint = findClosestUnvisitedPoint(possibleNextSteps, map);
        if (unvisitedPoint != null) {
            currentConsecutiveRollbacks = 0;
            return unvisitedPoint;
        }

        // 3. Se todas as posições possíveis já foram visitadas, tenta rollback
        if (!visitedPoints.isEmpty() && currentConsecutiveRollbacks < maxConsecutiveRollbacks) {
            Point rollbackPoint = findValidRollbackPoint(possibleNextSteps);
            if (rollbackPoint != null) {
                currentConsecutiveRollbacks++;
                // Remove pontos do caminho atual até o ponto de rollback
                while (!visitedPoints.isEmpty() && !visitedPoints.peek().equals(rollbackPoint)) {
                    Point removed = visitedPoints.pop();
                    visitedPositions.remove(getPositionKey(removed));
                }
                return rollbackPoint;
            }
        }

        // 4. Se não conseguir fazer rollback, escolhe o ponto com menor peso
        currentConsecutiveRollbacks = 0;
        return findPointWithLowestWeight(possibleNextSteps, map);
    }

    private String getPositionKey(Point point) {
        return point.getPositionX() + "," + point.getPositionY();
    }

    private Point findClosestUnvisitedPoint(List<Point> possiblePoints, Map map) {
        Point robotLocation = map.getRobotLocation();
        Point closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Point point : possiblePoints) {
            String posKey = getPositionKey(point);
            if (!visitedPositions.contains(posKey)) {
                double distance = calculateDistance(robotLocation, point);
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = point;
                }
            }
        }
        return closest;
    }

    private Point findValidRollbackPoint(List<Point> possiblePoints) {
        Stack<Point> tempStack = new Stack<>();
        Point validPoint = null;

        // Procura no histórico por um ponto válido para rollback
        while (!visitedPoints.isEmpty() && validPoint == null) {
            Point previousPoint = visitedPoints.peek();
            if (possiblePoints.contains(previousPoint)) {
                validPoint = previousPoint;
            } else {
                tempStack.push(visitedPoints.pop());
            }
        }

        // Restaura os pontos removidos durante a busca
        while (!tempStack.isEmpty()) {
            visitedPoints.push(tempStack.pop());
        }

        return validPoint;
    }

    private Point findPointWithLowestWeight(List<Point> possiblePoints, Map map) {
        Point bestPoint = possiblePoints.get(0);
        double lowestWeight = calculateWeight(bestPoint, map);

        for (Point point : possiblePoints) {
            double weight = calculateWeight(point, map);
            if (weight < lowestWeight) {
                lowestWeight = weight;
                bestPoint = point;
            }
        }

        return bestPoint;
    }

    private double calculateDistance(Point p1, Point p2) {
        int dx = p1.getPositionX() - p2.getPositionX();
        int dy = p1.getPositionY() - p2.getPositionY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private double calculateWeight(Point point, Map map) {
        double weight = 0;
        String content = map.get(point);
        
        // Penaliza posições já visitadas
        if (visitedPositions.contains(getPositionKey(point))) {
            weight += 10;
        }

        // Penaliza obstáculos
        if (content != null && !content.equals("*") && !content.equals(TreasureChest.CHARACTER)) {
            weight += 20;
        }

        return weight;
    }
}