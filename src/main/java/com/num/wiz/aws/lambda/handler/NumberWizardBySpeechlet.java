package com.num.wiz.aws.lambda.handler;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class NumberWizardBySpeechlet implements Speechlet {

    private static final Logger logger = LoggerFactory.getLogger(NumberWizardHandler.class);
    private static final String CARD_TITLE = "Number Wizard";

    /** TEXT FOR THE INTENTS **/
    private static final String GAME_PLAY_TEXT = "Hello %s let me know which game you want to play Addition, Subtraction, Multiplication or Division? Please choose one of these.";
    private static final String GAME_LEVEL_TEXT = "Great, Please tell me which difficulty level you want? Please choose one from easy, medium and hard by saying I want difficulty level and then the difficulty level.";
    private static final String GAME_START_TEXT = "Sounds Good, In this level you will be challenged to answer %s of numbers. Let's start, what is the result when %s %s %s";
    private static final String CONTINUE_GAME_TEXT = "%s Alright!! next question, what is the result when %s %s %s";
    private static final String GAME_RESULT_CORRECT_TEXT = "You are right, the result is %s.";
    private static final String GAME_RESULT_WRONG_TEXT = "Sorry it's the wrong answer. The correct answer is %s.";
    private static final Map<String, String> GAME_JARGAN_MAP = new HashMap(4);

    /** INTENT NAMES **/
    private static final String NICK_NAME_INTENT = "NickNameCapture";
    private static final String GAME_NAME_INTENT = "GameNameCapture";
    private static final String GAME_LEVEL_INTENT = "GameLevelCapture";
    private static final String GAME_RESULT_INTENT = "GameStarted";

    /** SLOT NAMES **/
    public static final String NAME_INTENT_SLOT = "USNickName";
    public static final String GAME_NAME_INTENT_SLOT = "GameNames";
    public static final String GAME_LEVEL_INTENT_SLOT = "GameLevel";
    public static final String GAME_LEVEL_RESULT_INTENT_SLOT = "GameResult";

    /** SESSION ATTRIBUTE NAMES **/
    public static final String USER_NAME_SESSION_ATTRIBUTE = "UserName";
    public static final String GAME_LEVEL_SESSION_ATTRIBUTE = "GameLevel";
    public static final String GAME_NAME_SESSION_ATTRIBUTE = "GameName";
    public static final String GAME_TYPE_RESULT_SESSION_ATTRIBUTE = "Result";

    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Welcome to Number Wizard!! The skill that challenges yours Numbering skill on four areas Addition, Subtraction, Multiplication and Division. \n" +
                "You will be scoring 10 point for each correct answer and 5 points for wrong answer. Based on your points you will be ranked and also achieved the badges starting from Newbie\n" +
                "Novice,Graduate,Expert,Master and Guru.  But first, I\\'d like to get to know you better. Tell me your nickname by saying: My nickname is, and then your nickname . ', 'Tell me your name by saying: My nickname is, and then your nickname'.";

        if (GAME_JARGAN_MAP.size() != 4) {
            GAME_JARGAN_MAP.put(GameType.ADDITION.name(), "added to");
            GAME_JARGAN_MAP.put(GameType.SUBTRACTION.name(), "subtracted from");
            GAME_JARGAN_MAP.put(GameType.MULTIPLICATION.name(), "multiplied by");
            GAME_JARGAN_MAP.put(GameType.DIVISION.name(), "divided");
        }

        return getAskResponse(CARD_TITLE, speechText);
    }

    @Override public void onSessionStarted(SessionStartedRequest sessionStartedRequest, Session session)
            throws SpeechletException {
        logger.info("onSessionStarted requestId={}, sessionId={}",sessionStartedRequest.getRequestId(),session.getSessionId());
    }

    @Override public SpeechletResponse onLaunch(LaunchRequest launchRequest, Session session)
            throws SpeechletException {
        logger.info("onLaunch requestId={}, sessionId={}",launchRequest.getRequestId(),session.getSessionId());
        //TODO check is the user's data already exists if yes redirect to the new speech
        return getWelcomeResponse();
    }

    @Override public SpeechletResponse onIntent(IntentRequest intentRequest, Session session)
            throws SpeechletException {

        logger.info("onIntent requestId={}, sessionId={}",intentRequest.getRequestId(),session.getSessionId());
        logger.info("onIntent Complete Intent={}",intentRequest.getIntent());

        Intent intent = intentRequest.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if (NICK_NAME_INTENT.equals(intentName)) { // START of intent first time
            String userName = intent.getSlot(NAME_INTENT_SLOT).getValue();
            session.setAttribute(USER_NAME_SESSION_ATTRIBUTE,userName);

            return getAskResponse(CARD_TITLE, String.format(GAME_PLAY_TEXT, userName));

        } else if (GAME_NAME_INTENT.equals(intentName)) { // Choose game type (+,-,*,/) intent
            String gameName = intent.getSlot(GAME_NAME_INTENT_SLOT).getValue();
            session.setAttribute(GAME_NAME_SESSION_ATTRIBUTE,gameName);

            return getAskResponse(CARD_TITLE, GAME_LEVEL_TEXT);

        } else if (GAME_LEVEL_INTENT.equals(intentName)) { // Choose game level (high,medium,easy) intent
            String gameLevel = intent.getSlot(GAME_LEVEL_INTENT_SLOT).getValue();
            session.setAttribute(GAME_LEVEL_SESSION_ATTRIBUTE,gameLevel);

            String gameName = (String)session.getAttribute(GAME_NAME_SESSION_ATTRIBUTE);

            Triple triple = MathHelper.getTheGameForLevel(gameName, gameLevel);
            session.setAttribute(GAME_TYPE_RESULT_SESSION_ATTRIBUTE,triple.getLeft());

            String response = String.format(GAME_START_TEXT, gameName, triple.getLeft(), GAME_JARGAN_MAP.get(gameName.toUpperCase()), triple.getMiddle());

            return getAskResponse(CARD_TITLE, response);

        } else if (GAME_RESULT_INTENT.equals(intentName)) { // result section of the game
            String userGameResultValue = intent.getSlot(GAME_LEVEL_RESULT_INTENT_SLOT).getValue();

            String gameName = (String)session.getAttribute(GAME_NAME_SESSION_ATTRIBUTE);
            String gameLevel = (String)session.getAttribute(GAME_LEVEL_SESSION_ATTRIBUTE);
            String gameResult = (String)session.getAttribute(GAME_TYPE_RESULT_SESSION_ATTRIBUTE);

            Triple triple = MathHelper.getTheGameForLevel(gameName, gameLevel);
            session.setAttribute(GAME_TYPE_RESULT_SESSION_ATTRIBUTE,triple.getLeft());

            String response;
            if(userGameResultValue == gameResult) { // if answer is correct
                //TODO add and update the score
                response = String.format(GAME_RESULT_CORRECT_TEXT + CONTINUE_GAME_TEXT, gameResult, triple.getLeft(), GAME_JARGAN_MAP.get(gameName.toUpperCase()), triple.getMiddle());
            } else {
                response = String.format(GAME_RESULT_WRONG_TEXT + CONTINUE_GAME_TEXT, gameResult, triple.getLeft(), GAME_JARGAN_MAP.get(gameName.toUpperCase()), triple.getMiddle());;
            }

            return getAskResponse(CARD_TITLE, response);

        } else if ("AMAZON.HelpIntent".equals(intentName) || "AMAZON.NoIntent".equals(intentName)) {
            return getWelcomeResponse();

        } else if ("AMAZON.StopIntent".equals(intentName) || "AMAZON.CancelIntent".equals(intentName)) {
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml("<speak>" + "Good Bye, I will let you know your score shortly" + "</speak>");
            return SpeechletResponse.newTellResponse(outputSpeech);

        } else {
            return getWelcomeResponse();
        }

    }

    @Override public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session)
            throws SpeechletException {
        logger.info("onSessionEnded requestId={}, sessionId={}",sessionEndedRequest.getRequestId(),session.getSessionId());
    }

    private SpeechletResponse getAskResponse(String cardTitle, String speechText) {
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

        Reprompt reprompt = getReprompt(outputSpeech);

        return SpeechletResponse.newAskResponse(outputSpeech, reprompt,standardCard);
    }

    private Reprompt getReprompt(OutputSpeech outputSpeech) {
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
