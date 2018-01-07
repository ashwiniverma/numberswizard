package com.num.wiz.aws.lambda.handler;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.num.wiz.aws.lambda.constants.Constants;
import com.num.wiz.aws.lambda.service.enums.GameSate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameNameHandlerIntent implements IntentRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(GameNameHandlerIntent.class);
    private static final String REPEAT_RESPONSE_TEXT = "Sorry !! I could not capture the game name. Please try saying again. Addition, Subtraction, Multiplication or Division?";
    private static final String GAME_PLAY_TEXT = "Great, Please tell me which difficulty level you want? Please choose one from easy, medium and hard.";
    private static final String GAME_PLAY_REPROMPT_TEXT = "Please choose one from easy, medium and hard.";


    @Override
    public boolean canHandle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        String state = (String)requestEnvelope.getSession().getAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE);
        String intentName = requestEnvelope.getRequest().getIntent().getName();
        logger.info("Intent Request called with state {} and intentName {}" , state, intentName);
        if(StringUtils.isNotBlank(state) && GameSate.GAME_NAME.name().equalsIgnoreCase(state)) {
            logger.info("canHandle true");
            return true;
        }
        return false;
    }

    @Override
    public SpeechletResponse handle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        Session session = requestEnvelope.getSession();
        String intentName = requestEnvelope.getRequest().getIntent().getName();
        try {
            String gameName = requestEnvelope.getRequest().getIntent().getSlot(Constants.GAME_NAME_INTENT_SLOT).getValue();

            if (StringUtils.isNotBlank(gameName)) {
                session.setAttribute(Constants.GAME_NAME_SESSION_ATTRIBUTE, gameName.toUpperCase());
                session.setAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE, GameSate.GAME_LEVEL.name());
                return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, GAME_PLAY_TEXT, GAME_PLAY_REPROMPT_TEXT);
            }
        } catch (Exception e) {
            logger.error("Exception in handling the intent {}", intentName);
        }
        return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, REPEAT_RESPONSE_TEXT, REPEAT_RESPONSE_TEXT);

    }
}
