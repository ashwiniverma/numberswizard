package com.num.wiz.aws.lambda.handler;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.*;
import com.amazon.speech.ui.Image;
import com.num.wiz.aws.lambda.models.NumberWizardModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NumberWizardBySpeechlet implements Speechlet {

    private static final Logger logger = LoggerFactory.getLogger(NumberWizardHandler.class);
    private static final String CARD_TITLE = "Number Wizard";

    /** TEXT FOR THE INTENTS **/
    private static final String GAME_PLAY_TEXT = "Hello %s let me know which game you want to play? Addition, Subtraction, Multiplication or Division? Please choose one of these.";
    private static final String GAME_LEVEL_TEXT = "Great, Please tell me which difficulty level you want? Please choose one from easy, medium and hard by saying I want difficulty level and then the difficulty level.";
    private static final String GAME_START_TEXT = "Sounds Good, In this level you will be challenged to answer %s of numbers. Let's start, what is the result when %s %s %s";
    private static final String CONTINUE_GAME_TEXT = "Alright!! next question, what is the result when %s %s %s";
    private static final String GAME_RESULT_CORRECT_TEXT = "You are right, the result is %s .";
    private static final String GAME_RESUME_TEXT = "Welcome Back %s to Number Wizard! Please say New game to start a new game or say resume to continue playing the previous game.";
    private static final String GAME_MESSAGE_TEXT = "Game %s . %s with difficulty level %s .";
    private static final String SAVED_GAME_START_TEXT = " Please say which game you want o resume? You can start the saved games by saying, game name and then the level . Like, Addition with level Easy .";

    private static final String GAME_RESULT_WRONG_TEXT = "Sorry it's the wrong answer. The correct answer is %s .";
    private static final Map<String, String> GAME_JARGAN_MAP = new HashMap(4);

    /** INTENT NAMES **/
    private static final String NICK_NAME_INTENT = "NickNameCapture";
    private static final String GAME_NAME_INTENT = "GameNameCapture";
    private static final String GAME_LEVEL_INTENT = "GameLevelCapture";
    private static final String GAME_RESULT_INTENT = "GameStarted";
    private static final String GAME_RESUME_INTENT = "Resume";
    private static final String SAVED_GAME_START_INTENT = "SavedGameStart";

    /** SLOT NAMES **/
    public static final String NAME_INTENT_SLOT = "USNickName";
    public static final String GAME_NAME_INTENT_SLOT = "GameNames";
    public static final String GAME_LEVEL_INTENT_SLOT = "GameLevel";
    public static final String GAME_LEVEL_RESULT_INTENT_SLOT = "GameResult";
    public static final String GAME_STATUS_INTENT_SLOT = "GameResume";


    /** SESSION ATTRIBUTE NAMES **/
    public static final String USER_NAME_SESSION_ATTRIBUTE = "userName";
    public static final String GAME_LEVEL_SESSION_ATTRIBUTE = "gameLevel";
    public static final String GAME_NAME_SESSION_ATTRIBUTE = "gameName";
    public static final String GAME_TYPE_RESULT_SESSION_ATTRIBUTE = "result";
    public static final String GAME_POINTS_SESSION_ATTRIBUTE = "points";
    public static final String USER_DATA_SESSION_ATTRIBUTE = "userData";
    public static final String CURRENT_GAME_NAME_SESSION_ATTRIBUTE = "currentGameName";

    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Welcome to Number Wizard!! The skill that challenges yours Numbering skill on four areas, Addition, Subtraction, Multiplication and Division ." +
                "You will be scoring 10 point for each correct answer and 5 points for wrong answer. Based on your points you will be ranked and also achieved the badges starting from Newbie," +
                "Novice,Graduate,Expert,Master and Guru.  But first, I would like to get to know you better. Tell me your nickname by saying, My nickname is, and then your nickname.";

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

        String usrId = session.getUser().getUserId();//getAttribute(USER_ID_SESSION_ATTRIBUTE);
        List<NumberWizardModel> userDataList = AwsServiceHelper.getSavedGame(usrId);
        if(userDataList.size() > 0) {

            session.setAttribute(USER_DATA_SESSION_ATTRIBUTE, userDataList);
            return getAskResponse(CARD_TITLE, String.format(GAME_RESUME_TEXT,userDataList.get(0).getNickname()));

        } else {
            return getWelcomeResponse();
        }
    }

    @Override public SpeechletResponse onIntent(IntentRequest intentRequest, Session session)
            throws SpeechletException {

        logger.info("onIntent requestId={}, sessionId={}", intentRequest.getRequestId(), session.getSessionId());

        Intent intent = intentRequest.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if (NICK_NAME_INTENT.equals(intentName)) { // START of intent first time
            String userName = intent.getSlot(NAME_INTENT_SLOT).getValue();
            session.setAttribute(USER_NAME_SESSION_ATTRIBUTE,userName);

            return getAskResponse(CARD_TITLE, String.format(GAME_PLAY_TEXT, userName));

        } else if (GAME_NAME_INTENT.equals(intentName)) { // Choose game type (+,-,*,/) intent
            String gameName = intent.getSlot(GAME_NAME_INTENT_SLOT).getValue();
            session.setAttribute(GAME_NAME_SESSION_ATTRIBUTE,gameName.toUpperCase());
            logger.info("onIntent gameName={}",gameName);

            return getAskResponse(CARD_TITLE, GAME_LEVEL_TEXT);

        } else if (GAME_LEVEL_INTENT.equals(intentName)) { // Choose game level (high,medium,easy) intent
            String gameLevel = intent.getSlot(GAME_LEVEL_INTENT_SLOT).getValue();
            session.setAttribute(GAME_LEVEL_SESSION_ATTRIBUTE,gameLevel);
            logger.info("onIntent gameLevel={}",gameLevel);

            String gameName = (String)session.getAttribute(GAME_NAME_SESSION_ATTRIBUTE);

            if (null == gameName) {
                gameName = GameType.ADDITION.name();
                session.setAttribute(GAME_NAME_SESSION_ATTRIBUTE, gameName.toUpperCase() );
            }

            Triple triple = MathHelper.getTheGameForLevel(gameName, gameLevel);
            session.setAttribute(GAME_TYPE_RESULT_SESSION_ATTRIBUTE,triple.getRight());

            String response = String.format(GAME_START_TEXT, gameName, triple.getLeft(), GAME_JARGAN_MAP.get(gameName.toUpperCase()), triple.getMiddle());

            return getAskResponse(CARD_TITLE, response);

        } else if (GAME_RESULT_INTENT.equals(intentName)) { // result section of the game
            String userGameResultValue = intent.getSlot(GAME_LEVEL_RESULT_INTENT_SLOT).getValue();

            String gameName = (String)session.getAttribute(GAME_NAME_SESSION_ATTRIBUTE);
            String gameLevel = (String)session.getAttribute(GAME_LEVEL_SESSION_ATTRIBUTE);
            int actualGameResult = (Integer)session.getAttribute(GAME_TYPE_RESULT_SESSION_ATTRIBUTE);
            logger.info("onIntent {} gameName={}, gameLevel={}, actualGameResult={}",GAME_RESULT_INTENT, gameName, gameLevel, actualGameResult);

            if (null == gameName) {
                gameName = GameType.ADDITION.name();
                session.setAttribute(GAME_NAME_SESSION_ATTRIBUTE, gameName);
            }

            if (null == gameLevel) {
                gameLevel = GameLevel.easy.name();
                session.setAttribute(GAME_LEVEL_SESSION_ATTRIBUTE,gameLevel);
            }

            Triple triple = MathHelper.getTheGameForLevel(gameName, gameLevel);
            session.setAttribute(GAME_TYPE_RESULT_SESSION_ATTRIBUTE,triple.getRight());

            String response;
            if(userGameResultValue.equals(String.valueOf(actualGameResult))) { // if answer is correct

                Integer gamePoint = (Integer)session.getAttribute(GAME_POINTS_SESSION_ATTRIBUTE);
                gamePoint = getWinningScore(gameName+gameLevel, gamePoint);
                //INFO add and update the score
                session.setAttribute(GAME_POINTS_SESSION_ATTRIBUTE, gamePoint);
                session.setAttribute(CURRENT_GAME_NAME_SESSION_ATTRIBUTE, gameName+"."+gameLevel);

                response = String.format(GAME_RESULT_CORRECT_TEXT + CONTINUE_GAME_TEXT, actualGameResult, triple.getLeft(), GAME_JARGAN_MAP.get(gameName.toUpperCase()), triple.getMiddle());

            } else {
                response = String.format(GAME_RESULT_WRONG_TEXT + CONTINUE_GAME_TEXT, actualGameResult, triple.getLeft(), GAME_JARGAN_MAP.get(gameName.toUpperCase()), triple.getMiddle());;
            }

            return getAskResponse(CARD_TITLE, response);

        } else if (GAME_RESUME_INTENT.equals(intentName)) {
            //When player wants to go to a new game or resume the old game

            String userGameStatus = intent.getSlot(GAME_STATUS_INTENT_SLOT).getValue();
            String userNickName = (String)session.getAttribute(USER_NAME_SESSION_ATTRIBUTE);
            if ("resume".equalsIgnoreCase(userGameStatus)) {


                List<NumberWizardModel> gameList = (List<NumberWizardModel>) session.getAttribute(USER_DATA_SESSION_ATTRIBUTE);
                int count = 0;
                String gameNames = "";
                for(NumberWizardModel model : gameList) {
                    count++;
                    String gameType = model.getSaved_games().split(".")[0];
                    String gameLevel = model.getSaved_games().split(".")[1];
                    gameNames = gameNames + "," + String.format(GAME_MESSAGE_TEXT, String.valueOf(count), gameType, gameLevel);;

                }
                return getAskResponse(CARD_TITLE, gameNames + SAVED_GAME_START_TEXT);

            } else {//shwo the intent to start the new game
                return getAskResponse(CARD_TITLE, String.format(GAME_PLAY_TEXT, userNickName));
            }

        } else if (SAVED_GAME_START_INTENT.equals(intentName)) {
            //INFO take the game name and level, and begin the saved game
            String savedGameName = intent.getSlot(GAME_NAME_INTENT_SLOT).getValue();
            String savedGameLevel = intent.getSlot(GAME_LEVEL_INTENT_SLOT).getValue();

            Triple triple = MathHelper.getTheGameForLevel(savedGameName, savedGameLevel);
            session.setAttribute(GAME_TYPE_RESULT_SESSION_ATTRIBUTE,triple.getRight());

            String response = String.format(GAME_START_TEXT, savedGameName, triple.getLeft(), GAME_JARGAN_MAP.get(savedGameName.toUpperCase()), triple.getMiddle());

            return getAskResponse(CARD_TITLE, response);


        }  else if ("AMAZON.HelpIntent".equals(intentName) || "AMAZON.NoIntent".equals(intentName)) {
            return getWelcomeResponse();

        } else if ("AMAZON.StopIntent".equals(intentName) || "AMAZON.CancelIntent".equals(intentName)) {
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml("<speak>" + "Good Bye, I will let you know your score shortly" + "</speak>");
            return SpeechletResponse.newTellResponse(outputSpeech);

        } else {
            return getWelcomeResponse();
        }

    }

    private int getWinningScore(String gameNameAndLevel, Integer currentScore) {
        if (null == currentScore) {
            currentScore = 0;
        }
        int points;
        Map<String, Integer> pointsForGameMapping = PointsMapping.getPointGameMapping();
        points = pointsForGameMapping.get(gameNameAndLevel);
        return points+currentScore;
    }

    @Override public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session)
            throws SpeechletException {

        //INFO get the score and the update the db, also calculate the badge
        String currentGame = (String) session.getAttribute(CURRENT_GAME_NAME_SESSION_ATTRIBUTE);
        String userId = session.getUser().getUserId();
        Integer totalScore = (Integer)session.getAttribute(GAME_POINTS_SESSION_ATTRIBUTE);
        String nickName = (String) session.getAttribute(USER_NAME_SESSION_ATTRIBUTE);

        NumberWizardModel numberWizardModel = new NumberWizardModel();
        numberWizardModel.setUser_id(userId);
        numberWizardModel.setSaved_games(currentGame);
        numberWizardModel.setNickname(nickName);
        numberWizardModel.setProfile_score(totalScore);
        numberWizardModel.setProfile_badge(PointsMapping.getBadge(totalScore));
        AwsServiceHelper.updateDataIntoDb(numberWizardModel);

        logger.info("onSessionEnded Object Saved={}, sessionId={}", numberWizardModel, session.getSessionId());
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
