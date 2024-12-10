package main.strategies;

import java.util.List;
import java.util.ArrayList;
import main.game.map.Map;
import main.game.map.Point;
import main.game.map.TreasureChest;

public class BinaryTreeDFS implements Strategy {
    private class TreeNode {
        Point point;
        double value;
        TreeNode left;
        TreeNode right;

        TreeNode(Point point, double value) {
            this.point = point;
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }

    @Override
    public Point evaluatePossbileNextStep(List<Point> possibleNextStep, Map map) {
        if (possibleNextStep.isEmpty()) {
            return null;
        }

        // Criar árvore binária com os possíveis próximos passos
        TreeNode root = buildTree(possibleNextStep, map);
        
        // Realizar busca em profundidade para encontrar o melhor caminho
        return findBestPath(root);
    }

    private TreeNode buildTree(List<Point> points, Map map) {
        if (points.isEmpty()) {
            return null;
        }

        // Calcular valor para cada ponto baseado em múltiplos fatores
        final Point selectedPoint = findBestPoint(points, map);
        final double selectedValue = evaluatePoint(selectedPoint, map);

        // Criar nó raiz com o melhor ponto
        TreeNode root = new TreeNode(selectedPoint, selectedValue);

        // Dividir pontos restantes em duas listas baseado em sua posição relativa
        List<Point> leftPoints = new ArrayList<>();
        List<Point> rightPoints = new ArrayList<>();
        
        for (Point point : points) {
            if (point.equals(selectedPoint)) {
                continue;
            }
            if (isLeftOf(point, selectedPoint)) {
                leftPoints.add(point);
            } else {
                rightPoints.add(point);
            }
        }

        // Recursivamente construir subárvores
        root.left = buildTree(leftPoints, map);
        root.right = buildTree(rightPoints, map);

        return root;
    }

    private Point findBestPoint(List<Point> points, Map map) {
        Point bestPoint = points.get(0);
        double bestValue = evaluatePoint(bestPoint, map);

        for (Point point : points) {
            double value = evaluatePoint(point, map);
            if (value > bestValue) {
                bestPoint = point;
                bestValue = value;
            }
        }

        return bestPoint;
    }

    private boolean isLeftOf(Point p1, Point p2) {
        // Determina se um ponto está à esquerda de outro
        // Primeiro compara X, depois Y em caso de empate
        if (p1.getPositionX() != p2.getPositionX()) {
            return p1.getPositionX() < p2.getPositionX();
        }
        return p1.getPositionY() < p2.getPositionY();
    }

    private double evaluatePoint(Point point, Map map) {
        double value = 0.0;
        String content = map.get(point);

        // Priorizar baús de tesouro
        if (TreasureChest.CHARACTER.equals(content)) {
            value += 100.0;
        }

        // Evitar obstáculos
        if (content != null && !content.equals("*")) {
            value -= 50.0;
        }

        // Considerar distância do centro do mapa
        int[] mapSize = map.getScenarioSize();
        int centerX = mapSize[0] / 2;
        int centerY = mapSize[1] / 2;
        double distanceToCenter = Math.sqrt(
            Math.pow(point.getPositionX() - centerX, 2) +
            Math.pow(point.getPositionY() - centerY, 2)
        );
        value -= distanceToCenter * 2;

        // Adicionar pequena aleatoriedade para evitar loops
        value += Math.random() * 5;

        return value;
    }

    private Point findBestPath(TreeNode root) {
        if (root == null) {
            return null;
        }

        // Inicializar com o valor do nó raiz
        Point bestPoint = root.point;
        double bestValue = root.value;

        // Busca em profundidade no lado esquerdo
        if (root.left != null) {
            double leftValue = dfsSearch(root.left);
            if (leftValue > bestValue) {
                bestValue = leftValue;
                bestPoint = root.left.point;
            }
        }

        // Busca em profundidade no lado direito
        if (root.right != null) {
            double rightValue = dfsSearch(root.right);
            if (rightValue > bestValue) {
                bestValue = rightValue;
                bestPoint = root.right.point;
            }
        }

        return bestPoint;
    }

    private double dfsSearch(TreeNode node) {
        if (node == null) {
            return Double.NEGATIVE_INFINITY;
        }

        // Valor do nó atual
        double value = node.value;

        // Recursivamente buscar o maior valor nas subárvores
        double leftValue = dfsSearch(node.left);
        double rightValue = dfsSearch(node.right);

        // Retornar o maior valor encontrado
        return Math.max(value, Math.max(leftValue, rightValue));
    }
}