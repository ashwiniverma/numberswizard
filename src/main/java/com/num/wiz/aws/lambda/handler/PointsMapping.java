package com.num.wiz.aws.lambda.handler;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class PointsMapping {

    private static Map<String, Integer> pointsForGameMapping;
    private static Map<String, Pair<Integer,Integer>> pointsForBadgeMapping;
    private static final String SEPARATOR = ".";

    protected static Map<String, Integer> getPointGameMapping() {
        if(null == pointsForGameMapping){
            pointsForGameMapping = new HashMap<>();
            pointsForGameMapping.put(GameType.ADDITION.name()+SEPARATOR+GameLevel.easy.name(),3);
            pointsForGameMapping.put(GameType.SUBTRACTION.name()+SEPARATOR+GameLevel.easy.name(),3);
            pointsForGameMapping.put(GameType.MULTIPLICATION.name()+SEPARATOR+GameLevel.easy.name(),3);
            pointsForGameMapping.put(GameType.DIVISION.name()+SEPARATOR+GameLevel.easy.name(),3);

            pointsForGameMapping.put(GameType.ADDITION.name()+SEPARATOR+GameLevel.medium.name(),5);
            pointsForGameMapping.put(GameType.SUBTRACTION.name()+SEPARATOR+GameLevel.medium.name(),5);
            pointsForGameMapping.put(GameType.MULTIPLICATION.name()+GameLevel.medium.name(),11);
            pointsForGameMapping.put(GameType.DIVISION.name()+SEPARATOR+GameLevel.medium.name(),17);

            pointsForGameMapping.put(GameType.ADDITION.name()+SEPARATOR+GameLevel.hard.name(),11);
            pointsForGameMapping.put(GameType.SUBTRACTION.name()+SEPARATOR+GameLevel.hard.name(),11);
            pointsForGameMapping.put(GameType.MULTIPLICATION.name()+SEPARATOR+GameLevel.hard.name(),21);
            pointsForGameMapping.put(GameType.DIVISION.name()+SEPARATOR+GameLevel.hard.name(),27);
        }
        return pointsForGameMapping;
    }

    private static Map<String, Pair<Integer,Integer>> getPointBadgeMappings() {
        if(null == pointsForBadgeMapping) {
            pointsForBadgeMapping = new HashMap<>();
            pointsForBadgeMapping.put(GameBadge.Newbie.name(),new ImmutablePair<>(0,100));
            pointsForBadgeMapping.put(GameBadge.Novice.name(),new ImmutablePair<>(101,500));
            pointsForBadgeMapping.put(GameBadge.Graduate.name(),new ImmutablePair<>(501,1000));
            pointsForBadgeMapping.put(GameBadge.Expert.name(),new ImmutablePair<>(1001,3000));
            pointsForBadgeMapping.put(GameBadge.Master.name(),new ImmutablePair<>(3001,6000));
            pointsForBadgeMapping.put(GameBadge.Guru.name(),new ImmutablePair<>(6001,1000000));
        }
        return pointsForBadgeMapping;
    }

    protected static String getBadge(Integer gamePoints) {
        if (null != gamePoints) {
            Map<String, Pair<Integer, Integer>> pointBadgeMappings = getPointBadgeMappings();

            for (Map.Entry<String, Pair<Integer, Integer>> entry : pointBadgeMappings.entrySet()) {
                Pair<Integer, Integer> pointGap = entry.getValue();
                if (between(gamePoints, pointGap.getLeft(), pointGap.getRight())) {
                    return entry.getKey();
                }
            }
        }
        return GameBadge.Newbie.name();
    }

    private static boolean between(int i, int minValueInclusive, int maxValueInclusive) {
        return (i >= minValueInclusive && i <= maxValueInclusive);
    }
}
