package com.num.wiz.aws.lambda.handler;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.num.wiz.aws.lambda.constants.Constants;
import com.num.wiz.aws.lambda.service.GameServiceHelper;
import com.num.wiz.aws.lambda.service.PointsMappingService;
import com.num.wiz.aws.lambda.service.enums.GameSate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResumeHandlerIntent implements IntentRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResumeHandlerIntent.class);
    public static final String GAME_MESSAGE_TEXT = "Game %s . %s with difficulty level %s .";
    public static final String SAVED_GAME_START_TEXT = " Please say which game you want to resume? You can start the saved games by saying, game name and then the level . Like, Addition with level Easy .";

    @Override
    public boolean canHandle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        String state = (String)requestEnvelope.getSession().getAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE);
        String intentName = requestEnvelope.getRequest().getIntent().getName();
        logger.info("Intent Request called with state {} and intentName {}" , state, intentName);

        if(StringUtils.isNotBlank(state) && GameSate.RESUME.name().equalsIgnoreCase(state)) {
            logger.info("canHandle true");
            return true;
        }
        return false;
    }

    @Override
    public SpeechletResponse handle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        Session session = requestEnvelope.getSession();
        String gameResumeText = requestEnvelope.getRequest().getIntent().getSlot(Constants.GAME_RESUME_INTENT_SLOT).getValue();
        String userName = (null == session.getAttribute(Constants.USER_NAME_SESSION_ATTRIBUTE))?"":(String)session.getAttribute(Constants.USER_NAME_SESSION_ATTRIBUTE);
        List<Object> gameList = (List<Object>) session.getAttribute(Constants.USER_DATA_SESSION_ATTRIBUTE);
        logger.info("Game resume text {}" , gameResumeText);
        if(StringUtils.isNotBlank(gameResumeText)) {

            if (("resume".equalsIgnoreCase(gameResumeText) || "saved".equalsIgnoreCase(gameResumeText)) && (null != gameList || !gameList.isEmpty())) {
                logger.info("Inside GAME_RESUME_INTENT Resume Game");
                int count = 0;
                String gameNames = "";

                if(gameList.size() ==1) {//if only one game is saved
                    session.setAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE, GameSate.INPROGRESS.name());

                    Map numWiz = (HashMap) gameList.get(0);
                    String [] savedGamesArray = ((String) numWiz.get("saved_games")).split("\\.");
                    String gameType = savedGamesArray[0];
                    String gameLevel = savedGamesArray[1];
                    session.setAttribute(Constants.GAME_POINTS_SESSION_ATTRIBUTE, numWiz.get("profile_score"));
                    String responseString = GameServiceHelper.startTheExistingGame(gameType, gameLevel, session, LevelHandlerIntent.GAME_START_TEXT);
                    return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE,responseString , responseString);
                }

                for (Object model : gameList) {
                    Map numWiz = (HashMap) model;
                    count++;
                    String [] savedGamesArray = ((String) numWiz.get("saved_games")).split("\\.");
                    String gameType = savedGamesArray[0];
                    String gameLevel = savedGamesArray[1];
                    gameNames = gameNames + "," + String.format(GAME_MESSAGE_TEXT, String.valueOf(count), gameType, gameLevel);
                }
                session.setAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE, GameSate.SAVED_GAME.name());
                return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, gameNames + SAVED_GAME_START_TEXT, SAVED_GAME_START_TEXT);

            } else {//show the intent to start the new game
                logger.info("Inside GAME_RESUME_INTENT New Game");

                String response = String.format(NickNameHandlerIntent.GAME_PLAY_TEXT, userName);
                session.setAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE, GameSate.GAME_NAME.name());
                return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, response, response);
            }
        }
        return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, String.format(WelcomeHandlerIntent.GAME_RESUME_TEXT,userName), null);
    }
}
