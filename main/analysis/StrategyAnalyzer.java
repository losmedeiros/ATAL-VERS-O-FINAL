package main.analysis;

import main.game.Game;
import main.game.Player;
import main.game.map.Map;
import main.game.map.Point;
import main.game.map.TreasureChest;
import main.strategies.*;

import java.util.HashMap;

public class StrategyAnalyzer {
    private static final int NUMBER_OF_GAMES = 100;
    
    public static class StrategyMetrics {
        private int totalSteps;
        private int victories;
        private int treasureFound;      // True Positives
        private int falsePositives;     // Found trap chest
        private int falseNegatives;     // Failed to find treasure when it was reachable
        
        public double getPrecision() {
            if (treasureFound + falsePositives == 0) return 0.0;
            return (double) treasureFound / (treasureFound + falsePositives);
        }
        
        public double getRecall() {
            if (treasureFound + falseNegatives == 0) return 0.0;
            return (double) treasureFound / (treasureFound + falseNegatives);
        }
        
        public double getAverageSteps() {
            return (double) totalSteps / NUMBER_OF_GAMES;
        }
        
        public double getVictoryRate() {
            return (double) victories / NUMBER_OF_GAMES;
        }
    }
    
    public static class GameResult {
        String finalState;
        int steps;
        boolean wasReachable;
        
        public GameResult(String finalState, int steps, boolean wasReachable) {
            this.finalState = finalState;
            this.steps = steps;
            this.wasReachable = wasReachable;
        }
    }
    
    public static java.util.Map<String, StrategyMetrics> analyzeStrategies() {
        java.util.Map<String, Strategy> strategies = new HashMap<>();
        strategies.put("FewerObstacles", new FewerObstacles());
        strategies.put("ShortestDistance", new ShortestDistance());
        strategies.put("Sort", new Sort());
        strategies.put("Rollback", new Rollback());
        strategies.put("BinaryTreeDFS", new BinaryTreeDFS());
        strategies.put("Votacao", new Votacao());
        
        java.util.Map<String, StrategyMetrics> results = new HashMap<>();
        
        for (java.util.Map.Entry<String, Strategy> entry : strategies.entrySet()) {
            String strategyName = entry.getKey();
            Strategy strategy = entry.getValue();
            
            StrategyMetrics metrics = analyzeStrategy(strategy);
            results.put(strategyName, metrics);
        }
        
        return results;
    }
    
    private static StrategyMetrics analyzeStrategy(Strategy strategy) {
        StrategyMetrics metrics = new StrategyMetrics();
        
        for (int i = 0; i < NUMBER_OF_GAMES; i++) {
            GameResult result = playGame(strategy);
            
            metrics.totalSteps += result.steps;
            
            if (result.finalState != null && result.finalState.equals(TreasureChest.CHEST_TRESURE_CHARACTER)) {
                metrics.victories++;
                metrics.treasureFound++;
            } else if (result.finalState != null && result.finalState.equals(TreasureChest.CHEST_TRAP_CHARACTER)) {
                metrics.falsePositives++;
            } else if (result.wasReachable) {
                metrics.falseNegatives++;
            }
        }
        
        return metrics;
    }
    
    private static GameResult playGame(Strategy strategy) {
        CustomGame game = new CustomGame(strategy);
        return game.runAndAnalyze();
    }
    
    public static void main(String[] args) {
        java.util.Map<String, StrategyMetrics> results = analyzeStrategies();
        
        System.out.println("Strategy Performance Analysis (100 games per strategy):\n");
        
        for (java.util.Map.Entry<String, StrategyMetrics> entry : results.entrySet()) {
            String strategyName = entry.getKey();
            StrategyMetrics metrics = entry.getValue();
            
            System.out.println("Strategy: " + strategyName);
            System.out.printf("Average Steps: %.2f\n", metrics.getAverageSteps());
            System.out.printf("Victory Rate: %.2f%%\n", metrics.getVictoryRate() * 100);
            System.out.printf("Precision: %.2f%%\n", metrics.getPrecision() * 100);
            System.out.printf("Recall: %.2f%%\n", metrics.getRecall() * 100);
            System.out.println("Total Victories: " + metrics.victories);
            System.out.println("----------------------------------------\n");
        }
    }
}

class CustomGame {
    private Map map;
    private Player player;
    private int steps;
    private String finalState;
    private boolean treasureReachable;
    
    public CustomGame(Strategy strategy) {
        this.map = new Map(8, 8);
        this.player = new Player(strategy);
        this.steps = 0;
        this.treasureReachable = checkTreasureReachability();
    }
    
    private boolean checkTreasureReachability() {
        // Simplified check - you might want to implement proper pathfinding
        return true;
    }
    
    public StrategyAnalyzer.GameResult runAndAnalyze() {
        while (true) {
            Point nextPoint = this.player.evaluatePossbileNextStep(map);
            if (nextPoint == null) {
                return new StrategyAnalyzer.GameResult(null, steps, treasureReachable);
            }
            
            steps++;
            String space = this.map.get(nextPoint);
            
            if (space != null && space.equals(TreasureChest.CHARACTER)) {
                this.map.openTreasureChest(nextPoint);
                // Store the final state (treasure, trap, or empty)
                finalState = this.map.get(nextPoint);
                return new StrategyAnalyzer.GameResult(finalState, steps, treasureReachable);
            }
            
            this.map.moveRobot(nextPoint);
            
            // Optional: Add maximum steps limit to prevent infinite loops
            if (steps > 100) {
                return new StrategyAnalyzer.GameResult(null, steps, treasureReachable);
            }
        }
    }
}