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

public class NickNameHandlerIntent implements IntentRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(NickNameHandlerIntent.class);
    public static final String NICKNAME_REPROMPT = "Please tell me your nickname by saying, My nickname is, and then your nickname.";
    public static final String GAME_PLAY_TEXT = "Hello %s , let me know which game you want to play? Addition, Subtraction, Multiplication or Division? Please choose one of these.";

    @Override
    public boolean canHandle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        String state = (String)requestEnvelope.getSession().getAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE);
        String intentName = requestEnvelope.getRequest().getIntent().getName();
        logger.info("Intent Request called with state {} and intentName {}" , state, intentName);
        if(StringUtils.isNotBlank(state) && GameSate.STARTED.name().equalsIgnoreCase(state)) {
            logger.info("canHandle true");
            return true;
        }
        return false;
    }

    @Override
    public SpeechletResponse handle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        Session session = requestEnvelope.getSession();
        String userName = requestEnvelope.getRequest().getIntent().getSlot(Constants.NAME_INTENT_SLOT).getValue();

        if(StringUtils.isNotBlank(userName)) {
            session.setAttribute(Constants.USER_NAME_SESSION_ATTRIBUTE, userName);
            session.setAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE, GameSate.GAME_NAME.name());
            return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, String.format(GAME_PLAY_TEXT, userName), null);
        }
        return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, NICKNAME_REPROMPT, NICKNAME_REPROMPT);
    }
}
