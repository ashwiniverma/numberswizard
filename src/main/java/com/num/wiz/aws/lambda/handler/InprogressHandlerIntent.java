package com.num.wiz.aws.lambda.handler;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.num.wiz.aws.lambda.constants.Constants;
import com.num.wiz.aws.lambda.service.AwsServiceHelper;
import com.num.wiz.aws.lambda.service.GameServiceHelper;
import com.num.wiz.aws.lambda.service.MathHelperService;
import com.num.wiz.aws.lambda.service.PointsMappingService;
import com.num.wiz.aws.lambda.service.enums.GameSate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InprogressHandlerIntent implements IntentRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(InprogressHandlerIntent.class);
    public static final String CONTINUE_GAME_TEXT = "Next question, what is the result when %s %s %s";
    public static final String FIRST_GAME_TEXT = "Here is your question, what is the result when %s %s %s";
    public static final String REPROMPT_GAME_TEXT = "What is the result when %s %s %s";
    public static final String GAME_RESULT_CORRECT_TEXT = "You are right, the result is %s . Alright!! ";
    public static final String GAME_RESULT_WRONG_TEXT = "Sorry it's the wrong answer. The correct answer is %s . Alright!!";
    public static final String REPEAT_RESPONSE_TEXT = "Sorry !! I could not capture your response. Please try saying again.";



    @Override
    public boolean canHandle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        String state = (String)requestEnvelope.getSession().getAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE);
        String intentName = requestEnvelope.getRequest().getIntent().getName();
        if(StringUtils.isNotBlank(state) && GameSate.INPROGRESS.name().equalsIgnoreCase(state)) {
            logger.info("canHandle true");
            return true;
        }
        return false;
    }

    @Override
    public SpeechletResponse handle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        Session session = requestEnvelope.getSession();

        String gameName = (String) session.getAttribute(Constants.GAME_NAME_SESSION_ATTRIBUTE);
        String gameLevel = (String) session.getAttribute(Constants.GAME_LEVEL_SESSION_ATTRIBUTE);
        Integer expectedGameResult = (null == session.getAttribute(Constants.GAME_TYPE_RESULT_SESSION_ATTRIBUTE))? null : (Integer) session.getAttribute(Constants.GAME_TYPE_RESULT_SESSION_ATTRIBUTE);
        Integer currentGamePoints = (null == session.getAttribute(Constants.GAME_POINTS_SESSION_ATTRIBUTE))? null : (Integer) session.getAttribute(Constants.GAME_POINTS_SESSION_ATTRIBUTE);
        String response = REPEAT_RESPONSE_TEXT;;
        String reprompt;
        try {
        String actualResult = requestEnvelope.getRequest().getIntent().getSlot(Constants.GAME_LEVEL_RESULT_INTENT_SLOT).getValue();
            if(StringUtils.isNotBlank(actualResult)) {

                Triple triple = MathHelperService.getTheGameForLevel(gameName, gameLevel);
                session.setAttribute(Constants.GAME_TYPE_RESULT_SESSION_ATTRIBUTE, triple.getRight());

                if (null == expectedGameResult) {
                    response = String.format(FIRST_GAME_TEXT, triple.getLeft(), GameServiceHelper.getGameJarganMap().get(gameName.toUpperCase()), triple.getMiddle());

                } else if (String.valueOf(expectedGameResult).equals(actualResult)) { // if answer is correct

                    currentGamePoints = (null == currentGamePoints)? 0 : currentGamePoints;
                    currentGamePoints = MathHelperService.getWinningScore(gameName.toUpperCase() + PointsMappingService.SEPARATOR + gameLevel, currentGamePoints);
                    //INFO add and update the score
                    session.setAttribute(Constants.GAME_POINTS_SESSION_ATTRIBUTE, currentGamePoints);

                    AwsServiceHelper.saveRecordToDb(session);
                    response = String.format(GAME_RESULT_CORRECT_TEXT + CONTINUE_GAME_TEXT, actualResult, triple.getLeft(), GameServiceHelper.getGameJarganMap().get(gameName.toUpperCase()), triple.getMiddle());

                } else {
                    response = String.format(GAME_RESULT_WRONG_TEXT + CONTINUE_GAME_TEXT, expectedGameResult, triple.getLeft(), GameServiceHelper.getGameJarganMap().get(gameName.toUpperCase()), triple.getMiddle());
                }
                reprompt = String.format(REPROMPT_GAME_TEXT, triple.getLeft(), GameServiceHelper.getGameJarganMap().get(gameName.toUpperCase()), triple.getMiddle());
                return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, response, reprompt);
            }
        } catch (NumberFormatException | ClassCastException | NullPointerException e) {
            response = REPEAT_RESPONSE_TEXT;
        }
        return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, response, response);
    }
}
