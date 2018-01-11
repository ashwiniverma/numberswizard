package com.num.wiz.aws.lambda.handler;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.num.wiz.aws.lambda.constants.Constants;
import com.num.wiz.aws.lambda.service.AwsServiceHelper;
import com.num.wiz.aws.lambda.service.HandlerFactory;
import com.num.wiz.aws.lambda.service.PointsMappingService;
import com.num.wiz.aws.lambda.service.enums.GameSate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelpHandlerIntent implements IntentRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(HelpHandlerIntent.class);
    private static final String GAME_EXIT_MESSAGE = "Good Bye, your total score is %s . And you have earned a %s badge.";

    @Override
    public boolean canHandle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        String intentName = requestEnvelope.getRequest().getIntent().getName();
        if("AMAZON.StopIntent".equalsIgnoreCase(intentName) || "AMAZON.CancelIntent".equalsIgnoreCase(intentName)
                || "AMAZON.NoIntent".equalsIgnoreCase(intentName) || "AMAZON.HelpIntent".equalsIgnoreCase(intentName)) {
            logger.info("canHandle true");
            return true;
        }
        return false;
    }

    @Override
    public SpeechletResponse handle(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        Session session = requestEnvelope.getSession();
        String intentName = requestEnvelope.getRequest().getIntent().getName();

        if("AMAZON.StopIntent".equalsIgnoreCase(intentName) || "AMAZON.CancelIntent".equalsIgnoreCase(intentName)) {
            session.setAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE, GameSate.END.name());
            logger.info("Inside intentName={}", intentName);
            Integer totalScore = (Integer) session.getAttribute(Constants.GAME_POINTS_SESSION_ATTRIBUTE);
            String gameName = (String) session.getAttribute(Constants.GAME_NAME_SESSION_ATTRIBUTE);
            String gameLevel = (String) session.getAttribute(Constants.GAME_LEVEL_SESSION_ATTRIBUTE);
            totalScore = totalScore == null? 0:totalScore;
            String badge = PointsMappingService.getBadge(totalScore);
            //int rank = AwsServiceHelper.getTheCurrentRank(requestEnvelope.getSession().getUser().getUserId(),badge, gameName.toUpperCase()+PointsMappingService.SEPARATOR+gameLevel);

            String goodByeMessage = String.format(GAME_EXIT_MESSAGE, totalScore, badge);
            logger.info("Good Bye Message {}", goodByeMessage);
            AwsServiceHelper.saveRecordToDb(session);

            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml("<speak>" + goodByeMessage + "</speak>");
            return SpeechletResponse.newTellResponse(outputSpeech);
        }
        session.setAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE, GameSate.STARTING.name());
        return HandlerFactory.dispatchRequest(requestEnvelope);
    }
}
