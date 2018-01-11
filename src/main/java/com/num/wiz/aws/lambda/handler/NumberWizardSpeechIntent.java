package com.num.wiz.aws.lambda.handler;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.*;
import com.num.wiz.aws.lambda.constants.Constants;
import com.num.wiz.aws.lambda.models.NumberWizardModel;
import com.num.wiz.aws.lambda.service.AwsServiceHelper;
import com.num.wiz.aws.lambda.service.HandlerFactory;
import com.num.wiz.aws.lambda.service.enums.GameSate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NumberWizardSpeechIntent implements SpeechletV2 {

    private static final Logger logger = LoggerFactory.getLogger(NumberWizardSpeechIntent.class);


    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> speechletRequestEnvelope) {
        Session session = speechletRequestEnvelope.getSession();
        logger.info("onSessionStarted requestId={}, sessionId={}",speechletRequestEnvelope.getRequest(),session.getSessionId());

        String usrId = session.getUser().getUserId();//getAttribute(USER_ID_SESSION_ATTRIBUTE);
        List<NumberWizardModel> userDataList = AwsServiceHelper.getSavedGame(usrId);
        if (userDataList.size() >0) {
            session.setAttribute(Constants.USER_DATA_SESSION_ATTRIBUTE, userDataList);
            String nickName = userDataList.get(0).getNickname();
            session.setAttribute(Constants.USER_NAME_SESSION_ATTRIBUTE, nickName);
        }
        session.setAttribute(Constants.GAME_STATE_SESSION_ATTRIBUTE, GameSate.STARTING.name());
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> speechletRequestEnvelope) {
        logger.info("onLaunch requestId={}" ,speechletRequestEnvelope.getRequest().getRequestId());
        return HandlerFactory.dispatchRequest(speechletRequestEnvelope);
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> speechletRequestEnvelope) {
        return HandlerFactory.dispatchRequest(speechletRequestEnvelope);
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> speechletRequestEnvelope) {
        Session session = speechletRequestEnvelope.getSession();
        logger.info("onSessionEnded requestId={}, sessionId={}",speechletRequestEnvelope.getRequest(),session.getSessionId());
        AwsServiceHelper.saveRecordToDb(session);
    }

    public static SpeechletResponse getAskResponse(String cardTitle, String speechText, String repromptString) {
        StandardCard standardCard = new StandardCard();

        if (StringUtils.isNotBlank(AwsServiceHelper.getImageUrl())) {
            String imageUrl = AwsServiceHelper.getImageUrl();
            if(!imageUrl.contains("https")) {
                imageUrl = "https" + imageUrl.substring(4);
            }
            logger.info("ImageUrl={}",imageUrl);
            Image image = new Image();
            image.setLargeImageUrl(imageUrl);
            image.setSmallImageUrl(imageUrl);
            standardCard.setImage(image);
            standardCard.setTitle(cardTitle);
        }

        SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml("<speak>" + speechText + "</speak>");

        SsmlOutputSpeech repromptOutputSpeech = new SsmlOutputSpeech();

        if (StringUtils.isNotBlank(repromptString)) {
            repromptOutputSpeech.setSsml("<speak>" + repromptString + "</speak>");
        } else {
            repromptOutputSpeech.setSsml("<speak>" + speechText + "</speak>");
        }

        Reprompt reprompt = getReprompt(repromptOutputSpeech);

        return SpeechletResponse.newAskResponse(outputSpeech, reprompt,standardCard);
    }

    private static Reprompt getReprompt(OutputSpeech outputSpeech) {
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(outputSpeech);

        return reprompt;
    }

    private PlainTextOutputSpeech getPlainTextOutputSpeech(String speechText) {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return speech;
    }

    private SimpleCard getSimpleCard(String title, String content) {
        SimpleCard card = new SimpleCard();
        card.setTitle(title);
        card.setContent(content);

        return card;
    }
}
