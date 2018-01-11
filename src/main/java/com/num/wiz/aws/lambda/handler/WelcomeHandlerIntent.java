package com.num.wiz.aws.lambda.handler;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.num.wiz.aws.lambda.constants.Constants;
import com.num.wiz.aws.lambda.models.NumberWizardModel;
import com.num.wiz.aws.lambda.service.AwsServiceHelper;
import com.num.wiz.aws.lambda.service.HandlerFactory;
import com.num.wiz.aws.lambda.service.enums.GameSate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ContainerAdapter;
import java.util.List;

public class WelcomeHandlerIntent implements IntentRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeHandlerIntent.class);
    public static final String WELCOME_TEXT = "Welcome to Number Wizard!! The skill that challenges yours Numbering skill on four areas, Addition, Subtraction, Multiplication and Division ." +
            "You will be scoring points for each correct answer and 0 points for a wrong answer. Based on your points you will be ranked and also achieve the badges starting from Newbie," +
            "Novice,Graduate,Expert,Master and Guru.  But first, I would like to get to know you better. Tell me your nickname by saying, My nickname is, and then your nickname.";
    public static final String GAME_RESUME_TEXT = "Welcome Back %s , to Number Wizard! Please say New game to start a new game or say resume to continue playing the previous game.";
    public static final String GAME_NAME_CAPTURE_TEXT = "Before starting the game, I would like to get to know you better. Tell me your nickname by saying, My nickname is, and then your nickname.";


    @Override
    public boolean canHandle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        String state = (String)requestEnvelope.getSession().getAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE);
        String intentName = requestEnvelope.getRequest().getIntent().getName();
        logger.info("Intent Request called with state {} and intentName {}" , state, intentName);
        if (StringUtils.isBlank(state) ) {
            requestEnvelope.getSession().setAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE, GameSate.STARTING.name());
            logger.info("canHandle true");
            return true;
        }

        if(StringUtils.isNotBlank(state) && (GameSate.STARTING.name().equalsIgnoreCase(state) || GameSate.END.name().equalsIgnoreCase(state)
                || StringUtils.isBlank(intentName))) {
            logger.info("canHandle true");
            return true;
        }
        return false;
    }

    @Override
    public SpeechletResponse handle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        Session session = requestEnvelope.getSession();
        String usrId = session.getUser().getUserId();
        List<NumberWizardModel> userDataList = AwsServiceHelper.getSavedGame(usrId);
        SpeechletResponse response = null;
        if(userDataList.size() > 0) {
            session.setAttribute(Constants.USER_DATA_SESSION_ATTRIBUTE, userDataList);
            String nickName = (null == userDataList.get(0).getNickname())?"":userDataList.get(0).getNickname();
            session.setAttribute(Constants.USER_NAME_SESSION_ATTRIBUTE,nickName);
            session.setAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE, GameSate.RESUME.name());

            if (requestEnvelope.getRequest() instanceof IntentRequest) {
                response = HandlerFactory.validateAndSendToCorrectIntent(requestEnvelope);
            }
            if (null == response) {
                response = NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, String.format(GAME_RESUME_TEXT,nickName), null);
            }

        } else {
            session.setAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE, GameSate.STARTED.name());
            if (requestEnvelope.getRequest() instanceof IntentRequest) {
                response = HandlerFactory.validateAndSendToCorrectIntent(requestEnvelope);
            }
            if (null == response) {
                response =  NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, WELCOME_TEXT, WELCOME_TEXT);
            }

        }
        return response;
    }
}
