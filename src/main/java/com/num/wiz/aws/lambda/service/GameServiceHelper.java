package com.num.wiz.aws.lambda.service;

import com.amazon.speech.speechlet.Session;
import com.num.wiz.aws.lambda.constants.Constants;
import com.num.wiz.aws.lambda.service.enums.GameType;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameServiceHelper {
    private static final Logger logger = LoggerFactory.getLogger(GameServiceHelper.class);

    private static final Map<String, String> GAME_JARGAN_MAP = new HashMap(4);

    public static String startTheExistingGame(String gameName, String gameLevel, Session session, String message) {
        session.setAttribute(Constants.GAME_NAME_SESSION_ATTRIBUTE, gameName);
        session.setAttribute(Constants.GAME_LEVEL_SESSION_ATTRIBUTE, gameLevel);

        Integer points = PointsMappingService.getPointGameMapping().get(gameName.toUpperCase() + PointsMappingService.SEPARATOR + gameLevel);

        Triple triple = MathHelperService.getTheGameForLevel(gameName, gameLevel);
        session.setAttribute(Constants.GAME_TYPE_RESULT_SESSION_ATTRIBUTE, triple.getRight());

        return String.format(message, gameName, points, triple.getLeft(), getGameJarganMap().get(gameName.toUpperCase()), triple.getMiddle());
    }

    public static int getTheCurrentGameScore(List<Object> gameList, String savedGameName) {
        int currentGameScore = 0;
        for (Object model : gameList) {
            Map numWiz = (HashMap) model;
            if(((String) numWiz.get("saved_games")).equalsIgnoreCase(savedGameName)) {
                currentGameScore = (int) numWiz.get("profile_score");
            }
        }
        return currentGameScore;
    }

    public static Map getGameJarganMap() {
        if (GAME_JARGAN_MAP.size() != 4) {
            GAME_JARGAN_MAP.put(GameType.ADDITION.name(), "added to");
            GAME_JARGAN_MAP.put(GameType.SUBTRACTION.name(), "minus");
            GAME_JARGAN_MAP.put(GameType.MULTIPLICATION.name(), "multiplied by");
            GAME_JARGAN_MAP.put(GameType.DIVISION.name(), "divided");
        }
        return GAME_JARGAN_MAP;
    }
}
