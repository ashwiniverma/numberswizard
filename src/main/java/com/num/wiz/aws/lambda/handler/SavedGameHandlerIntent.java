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

import java.util.List;

public class SavedGameHandlerIntent implements IntentRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(SavedGameHandlerIntent.class);

    @Override
    public boolean canHandle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        String state = (String)requestEnvelope.getSession().getAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE);
        String intentName = requestEnvelope.getRequest().getIntent().getName();
        logger.info("Intent Request called with state {} and intentName {}" , state, intentName);
        if(StringUtils.isNotBlank(state) && GameSate.SAVED_GAME.name().equalsIgnoreCase(state)) {
            logger.info("canHandle true");
            return true;
        }
        return false;
    }

    @Override
    public SpeechletResponse handle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        Session session = requestEnvelope.getSession();
        String savedGameName = requestEnvelope.getRequest().getIntent().getSlot(Constants.GAME_NAME_INTENT_SLOT).getValue();
        String savedGameLevel = requestEnvelope.getRequest().getIntent().getSlot(Constants.GAME_LEVEL_INTENT_SLOT).getValue();
        List<Object> gameList = (List<Object>) session.getAttribute(Constants.USER_DATA_SESSION_ATTRIBUTE);

        if(StringUtils.isNotBlank(savedGameName) && StringUtils.isNotBlank(savedGameLevel)) {

            int currentGamePreviousScore = GameServiceHelper.getTheCurrentGameScore(gameList,savedGameName.toUpperCase() + PointsMappingService.SEPARATOR + savedGameLevel);
            session.setAttribute(Constants.GAME_POINTS_SESSION_ATTRIBUTE, currentGamePreviousScore);
            session.setAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE, GameSate.INPROGRESS.name());
            String responseString = GameServiceHelper.startTheExistingGame(savedGameName, savedGameLevel, session, LevelHandlerIntent.GAME_START_TEXT);
            return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE,responseString ,responseString);
        }
        return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, ResumeHandlerIntent.SAVED_GAME_START_TEXT, ResumeHandlerIntent.SAVED_GAME_START_TEXT);
    }
}
