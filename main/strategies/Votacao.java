package main.strategies;

import java.util.List;
import java.util.HashMap;
import main.game.map.Map;
import main.game.map.Point;

public class Votacao implements Strategy {
    private List<Strategy> strategies;

    public Votacao() {
        // Initialize all available strategies
        strategies = List.of(
            new FewerObstacles(),
            new ShortestDistance(),
            new BinaryTreeDFS(),
            new Sort(),
            new Rollback()
        );
    }

    @Override
    public Point evaluatePossbileNextStep(List<Point> possibleNextStep, Map map) {
        if (possibleNextStep.isEmpty()) {
            return null;
        }

        // Map to store votes for each point
        HashMap<String, VoteInfo> votes = new HashMap<>();

        // Initialize vote tracking for each possible point
        for (Point point : possibleNextStep) {
            String key = pointToString(point);
            votes.put(key, new VoteInfo(point));
        }

        // Collect votes from each strategy
        for (Strategy strategy : strategies) {
            Point recommendedPoint = strategy.evaluatePossbileNextStep(possibleNextStep, map);
            if (recommendedPoint != null) {
                String key = pointToString(recommendedPoint);
                VoteInfo voteInfo = votes.get(key);
                if (voteInfo != null) {
                    voteInfo.incrementVotes();
                }
            }
        }

        // Find the point with the most votes
        VoteInfo winningPoint = null;
        int maxVotes = -1;

        for (VoteInfo voteInfo : votes.values()) {
            if (voteInfo.getVotes() > maxVotes) {
                maxVotes = voteInfo.getVotes();
                winningPoint = voteInfo;
            }
        }

        return winningPoint != null ? winningPoint.getPoint() : possibleNextStep.get(0);
    }

    private String pointToString(Point p) {
        return p.getPositionX() + "," + p.getPositionY();
    }

    // Helper class to track votes for each point
    private static class VoteInfo {
        private Point point;
        private int votes;

        public VoteInfo(Point point) {
            this.point = point;
            this.votes = 0;
        }

        public void incrementVotes() {
            votes++;
        }

        public int getVotes() {
            return votes;
        }

        public Point getPoint() {
            return point;
        }
    }
}