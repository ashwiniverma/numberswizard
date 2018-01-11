package com.num.wiz.aws.lambda.handler;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.num.wiz.aws.lambda.constants.Constants;
import com.num.wiz.aws.lambda.service.GameServiceHelper;
import com.num.wiz.aws.lambda.service.MathHelperService;
import com.num.wiz.aws.lambda.service.PointsMappingService;
import com.num.wiz.aws.lambda.service.enums.GameSate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelHandlerIntent implements IntentRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(LevelHandlerIntent.class);
    public static final String GAME_START_TEXT = "In this level you will be challenged to answer %s of numbers. You will be scoring %s points for each correct answers. Let's start, what is the result when %s %s %s";
    public static final String GAME_LEVEL_REPROMPT_TEXT = "Please tell me which difficulty level you want? Choose one from easy, medium and hard.";
    public static final String GAME_LEVEL_ERROR = "Please start games by saying, game name and then the level . Like, Addition with level Easy .";

    @Override
    public boolean canHandle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        String state = (String)requestEnvelope.getSession().getAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE);
        String intentName = requestEnvelope.getRequest().getIntent().getName();
        logger.info("Intent Request called with state {} and intentName {}" , state, intentName);
        if(StringUtils.isNotBlank(state) && GameSate.GAME_LEVEL.name().equalsIgnoreCase(state)) {
            logger.info("canHandle true");
            return true;
        }
        return false;
    }

    @Override
    public SpeechletResponse handle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        Session session = requestEnvelope.getSession();
        String gameLevel = requestEnvelope.getRequest().getIntent().getSlot(Constants.GAME_LEVEL_INTENT_SLOT).getValue();
        String intentName = requestEnvelope.getRequest().getIntent().getName();
        try {
        if(StringUtils.isNotBlank(gameLevel)) {
            String gameName = (String)session.getAttribute(Constants.GAME_NAME_SESSION_ATTRIBUTE);

            if(StringUtils.isBlank(gameName)) {
                return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, GAME_LEVEL_ERROR, GAME_LEVEL_ERROR);
            }

            Triple triple = MathHelperService.getTheGameForLevel(gameName, gameLevel);

            session.setAttribute(Constants.GAME_LEVEL_SESSION_ATTRIBUTE, gameLevel);
            session.setAttribute(Constants.GAME_TYPE_RESULT_SESSION_ATTRIBUTE, triple.getRight());

            Integer points = PointsMappingService.getPointGameMapping().get(gameName + PointsMappingService.SEPARATOR + gameLevel);
            String responseString = String.format(GAME_START_TEXT, gameName, points, triple.getLeft(), GameServiceHelper.getGameJarganMap().get(gameName.toUpperCase()), triple.getMiddle());
            session.setAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE, GameSate.INPROGRESS.name());
            return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, responseString, responseString);
        }
        } catch (Exception e) {
            logger.error("Exception in handling the intent {} with exception {}", intentName, e.fillInStackTrace());
        }
        return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, GAME_LEVEL_REPROMPT_TEXT, GAME_LEVEL_REPROMPT_TEXT);

    }
}
