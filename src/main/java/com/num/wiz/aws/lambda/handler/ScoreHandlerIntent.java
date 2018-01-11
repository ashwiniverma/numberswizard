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

public class ScoreHandlerIntent implements IntentRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ScoreHandlerIntent.class);
    public static final String CONTINUE_GAME_TEXT = "Next question, what is the result when %s %s %s";
    public static final String NO_GAME_INPROGRESS = "Sorry !! You don't have any game in progress. Let me know which game you want to play? Addition, Subtraction, Multiplication or Division?";
    public static final String GAME_SCORE_TEXT = "Your current score for %s , level %s is %s . You have earned %s badge .";

    @Override
    public boolean canHandle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        String state = (String)requestEnvelope.getSession().getAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE);
        String intentName = requestEnvelope.getRequest().getIntent().getName();
        logger.info("Intent Request called with state {} and intentName {}" , state, intentName);
        if(StringUtils.isNotBlank(state) && GameSate.INPROGRESS.name().equalsIgnoreCase(state)) {
            logger.info("canHandle true");
            return true;
        }
        return false;
    }

    @Override
    public SpeechletResponse handle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        Session session = requestEnvelope.getSession();
        Integer gamePoint = (Integer) session.getAttribute(Constants.GAME_POINTS_SESSION_ATTRIBUTE);
        String gameName = (String) session.getAttribute(Constants.GAME_NAME_SESSION_ATTRIBUTE);
        String gameLevel = (String) session.getAttribute(Constants.GAME_LEVEL_SESSION_ATTRIBUTE);

        if (StringUtils.isBlank(gameName) || StringUtils.isBlank(gameLevel) || null == gamePoint) {
            session.setAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE, GameSate.GAME_NAME.name());
            return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, NO_GAME_INPROGRESS, NO_GAME_INPROGRESS);
        }

        String badge = PointsMappingService.getBadge(gamePoint);
        logger.info("GAME_SCORE_INTENT Points={} , gameName={} , gameLevel={} and badge={} ", gamePoint, gameName, gameLevel, badge);

        Triple triple = MathHelperService.getTheGameForLevel(gameName, gameLevel);
        session.setAttribute(Constants.GAME_TYPE_RESULT_SESSION_ATTRIBUTE, triple.getRight());
        session.setAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE, GameSate.INPROGRESS.name());
        String continueGame = String.format(CONTINUE_GAME_TEXT, triple.getLeft(), GameServiceHelper.getGameJarganMap().get(gameName.toUpperCase()), triple.getMiddle());
        String score = String.format(GAME_SCORE_TEXT, gameName, gameLevel, gamePoint, badge);

        return NumberWizardSpeechIntent.getAskResponse(Constants.CARD_TITLE, score+continueGame, continueGame);
    }
}
