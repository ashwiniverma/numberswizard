package com.num.wiz.aws.lambda.handler;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.*;
import com.num.wiz.aws.lambda.models.NumberWizardModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NumberWizardBySpeechlet implements Speechlet {

    private static final Logger logger = LoggerFactory.getLogger(NumberWizardBySpeechlet.class);
    private static final String CARD_TITLE = "Number Wizard";

    /** TEXT FOR THE INTENTS **/
    private static final String GAME_PLAY_TEXT = "Hello %s , let me know which game you want to play? Addition, Subtraction, Multiplication or Division? Please choose one of these.";
    private static final String GAME_LEVEL_TEXT = "Great, Please tell me which difficulty level you want? Please choose one from easy, medium and hard by saying I want difficulty level and then the difficulty level.";
    private static final String GAME_START_TEXT = "Sounds Good, In this level you will be challenged to answer %s of numbers. You will be scoring %s points for each correct answers. Let's start, what is the result when %s %s %s";
    private static final String GAME_RESTART_TEXT = "Restarting game of %s , with current points %s .What is the result when %s %s %s";
    private static final String CONTINUE_GAME_TEXT = "Alright!! next question, what is the result when %s %s %s";
    private static final String GAME_RESULT_CORRECT_TEXT = "You are right, the result is %s .";
    private static final String GAME_RESUME_TEXT = "Welcome Back %s , to Number Wizard! Please say New game to start a new game or say resume to continue playing the previous game.";
    private static final String GAME_MESSAGE_TEXT = "Game %s . %s with difficulty level %s .";
    private static final String SAVED_GAME_START_TEXT = " Please say which game you want to resume? You can start the saved games by saying, game name and then the level . Like, Addition with level Easy .";
    private static final String GAME_SCORE_TEXT = "Your current score for %s , level %s is %s . You have earned %s badge .";
    private static final String GAME_EXIT_MESSAGE = "Good Bye, your total score is %s . And you have earned a %s badge.";

    private static final String GAME_RESULT_WRONG_TEXT = "Sorry it's the wrong answer. The correct answer is %s .";
    private static final Map<String, String> GAME_JARGAN_MAP = new HashMap(4);

    /** INTENT NAMES **/
    private static final String NICK_NAME_INTENT = "NickNameCapture";
    private static final String GAME_NAME_INTENT = "GameNameCapture";
    private static final String GAME_LEVEL_INTENT = "GameLevelCapture";
    private static final String GAME_RESULT_INTENT = "GameStarted";
    private static final String GAME_RESUME_INTENT = "Resume";
    private static final String SAVED_GAME_START_INTENT = "SavedGameStart";
    private static final String GAME_SCORE_INTENT = "GetScoreIntent";


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

    private Map getGameJarganMap() {
        if (GAME_JARGAN_MAP.size() != 4) {
            GAME_JARGAN_MAP.put(GameType.ADDITION.name(), "added to");
            GAME_JARGAN_MAP.put(GameType.SUBTRACTION.name(), "subtracted from");
            GAME_JARGAN_MAP.put(GameType.MULTIPLICATION.name(), "multiplied by");
            GAME_JARGAN_MAP.put(GameType.DIVISION.name(), "divided");
        }
        return GAME_JARGAN_MAP;
    }

    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Welcome to Number Wizard!! The skill that challenges yours Numbering skill on four areas, Addition, Subtraction, Multiplication and Division ." +
                "You will be scoring points for each correct answer and 0 points for a wrong answer. Based on your points you will be ranked and also achieve the badges starting from Newbie," +
                "Novice,Graduate,Expert,Master and Guru.  But first, I would like to get to know you better. Tell me your nickname by saying, My nickname is, and then your nickname.";
        return getAskResponse(CARD_TITLE, speechText);
    }

    private SpeechletResponse getHelpResponse( String speechText) {
        return getAskResponse(CARD_TITLE, speechText);
    }

    @Override public void onSessionStarted(SessionStartedRequest sessionStartedRequest, Session session)
            throws SpeechletException {
        logger.info("onSessionStarted requestId={}, sessionId={}",sessionStartedRequest.getRequestId(),session.getSessionId());
        String usrId = session.getUser().getUserId();//getAttribute(USER_ID_SESSION_ATTRIBUTE);
        List<NumberWizardModel> userDataList = AwsServiceHelper.getSavedGame(usrId);
        if (userDataList.size() >0) {
            session.setAttribute(USER_DATA_SESSION_ATTRIBUTE, userDataList);
            String nickName = userDataList.get(0).getNickname();
            session.setAttribute(USER_NAME_SESSION_ATTRIBUTE, nickName);
        }
    }

    @Override public SpeechletResponse onLaunch(LaunchRequest launchRequest, Session session)
            throws SpeechletException {
        String usrId = session.getUser().getUserId();//getAttribute(USER_ID_SESSION_ATTRIBUTE);
        List<NumberWizardModel> userDataList = AwsServiceHelper.getSavedGame(usrId);
        if(userDataList.size() > 0) {

            session.setAttribute(USER_DATA_SESSION_ATTRIBUTE, userDataList);
            String nickName = userDataList.get(0).getNickname();
            session.setAttribute(USER_NAME_SESSION_ATTRIBUTE,nickName);

            return getAskResponse(CARD_TITLE, String.format(GAME_RESUME_TEXT,nickName));

        } else {
            return getWelcomeResponse();
        }
    }

    @Override public SpeechletResponse onIntent(IntentRequest intentRequest, Session session)
            throws SpeechletException {

        String userName = (String) session.getAttribute(USER_NAME_SESSION_ATTRIBUTE);
        try {
            Intent intent = intentRequest.getIntent();
            String intentName = (intent != null) ? intent.getName() : null;
            logger.info("onIntent requestId={}, sessionAttributes={} with intent={}", intentRequest.getRequestId(), session.getAttributes(), intentName);

            if (NICK_NAME_INTENT.equals(intentName)) { // START of intent first time
                userName = (StringUtils.isNotBlank(userName)? userName : intent.getSlot(NAME_INTENT_SLOT).getValue());
                session.setAttribute(USER_NAME_SESSION_ATTRIBUTE, userName);

                return getAskResponse(CARD_TITLE, String.format(GAME_PLAY_TEXT, userName));

            } else if (GAME_NAME_INTENT.equals(intentName)) { // Choose game type (+,-,*,/) intent
                String gameName = intent.getSlot(GAME_NAME_INTENT_SLOT).getValue();
                session.setAttribute(GAME_NAME_SESSION_ATTRIBUTE, gameName.toUpperCase());
                logger.info("onIntent gameName={}", gameName);

                return getAskResponse(CARD_TITLE, GAME_LEVEL_TEXT);

            } else if (GAME_LEVEL_INTENT.equals(intentName)) { // Choose game level (high,medium,easy) intent
                String gameLevel = intent.getSlot(GAME_LEVEL_INTENT_SLOT).getValue();
                session.setAttribute(GAME_LEVEL_SESSION_ATTRIBUTE, gameLevel);
                logger.info("onIntent gameLevel={}", gameLevel);

                String gameName = (String) session.getAttribute(GAME_NAME_SESSION_ATTRIBUTE);

                if (null == gameName) {
                    gameName = GameType.ADDITION.name();
                    session.setAttribute(GAME_NAME_SESSION_ATTRIBUTE, gameName.toUpperCase());
                }

                Triple triple = MathHelper.getTheGameForLevel(gameName, gameLevel);
                session.setAttribute(GAME_TYPE_RESULT_SESSION_ATTRIBUTE, triple.getRight());
                session.setAttribute(CURRENT_GAME_NAME_SESSION_ATTRIBUTE, gameName + PointsMapping.SEPARATOR + gameLevel);
                Integer points = PointsMapping.getPointGameMapping().get(gameName + PointsMapping.SEPARATOR + gameLevel);

                String response = String.format(GAME_START_TEXT, gameName, points, triple.getLeft(), getGameJarganMap().get(gameName.toUpperCase()), triple.getMiddle());

                return getAskResponse(CARD_TITLE, response);

            } else if (GAME_RESULT_INTENT.equals(intentName)) { // result section of the game
                String response;
                try {
                    String userGameResultValue = intent.getSlot(GAME_LEVEL_RESULT_INTENT_SLOT).getValue();

                    String gameName = (String) session.getAttribute(GAME_NAME_SESSION_ATTRIBUTE);
                    String gameLevel = (String) session.getAttribute(GAME_LEVEL_SESSION_ATTRIBUTE);
                    List<Object> userDataList = (List<Object>)session.getAttribute(USER_DATA_SESSION_ATTRIBUTE);
                    int gamePoint = getTheCurrentGameScore(userDataList, gameName+"."+gameLevel);
                    session.setAttribute(GAME_POINTS_SESSION_ATTRIBUTE, gamePoint);

                    int actualGameResult = (Integer) session.getAttribute(GAME_TYPE_RESULT_SESSION_ATTRIBUTE);
                    logger.info("onIntent {} gameName={}, gameLevel={}, actualGameResult={}", GAME_RESULT_INTENT, gameName, gameLevel, actualGameResult);

                    if (null == gameName) {
                        gameName = GameType.ADDITION.name();
                        session.setAttribute(GAME_NAME_SESSION_ATTRIBUTE, gameName);
                    }

                    if (null == gameLevel) {
                        gameLevel = GameLevel.easy.name();
                        session.setAttribute(GAME_LEVEL_SESSION_ATTRIBUTE, gameLevel);
                    }

                    Triple triple = MathHelper.getTheGameForLevel(gameName, gameLevel);
                    session.setAttribute(GAME_TYPE_RESULT_SESSION_ATTRIBUTE, triple.getRight());

                    if (userGameResultValue.equals(String.valueOf(actualGameResult))) { // if answer is correct

                        gamePoint = gamePoint + (Integer) session.getAttribute(GAME_POINTS_SESSION_ATTRIBUTE);
                        gamePoint = getWinningScore(gameName.toUpperCase() + PointsMapping.SEPARATOR + gameLevel, gamePoint);
                        //INFO add and update the score
                        session.setAttribute(GAME_POINTS_SESSION_ATTRIBUTE, gamePoint);
                        session.setAttribute(CURRENT_GAME_NAME_SESSION_ATTRIBUTE, gameName + PointsMapping.SEPARATOR + gameLevel);

                        saveRecordToDb(session);
                        response = String.format(GAME_RESULT_CORRECT_TEXT + CONTINUE_GAME_TEXT, actualGameResult, triple.getLeft(), getGameJarganMap().get(gameName.toUpperCase()), triple.getMiddle());

                    } else {
                        response = String.format(GAME_RESULT_WRONG_TEXT + CONTINUE_GAME_TEXT, actualGameResult, triple.getLeft(), getGameJarganMap().get(gameName.toUpperCase()), triple.getMiddle());
                    }
                } catch (NumberFormatException | ClassCastException e) {
                    response = "Sorry !! I could not capture your response. Please try saying again.";
                }

                return getAskResponse(CARD_TITLE, response);

            } else if (GAME_RESUME_INTENT.equals(intentName)) {
                //When player wants to go to a new game or resume the old game

                String userGameStatus = intent.getSlot(GAME_STATUS_INTENT_SLOT).getValue();
                String userNickName = (String) session.getAttribute(USER_NAME_SESSION_ATTRIBUTE);
                if ("resume".equalsIgnoreCase(userGameStatus) || "saved".equalsIgnoreCase(userGameStatus)) {

                    logger.info("Inside GAME_RESUME_INTENT Resume Game");
                    List<Object> gameList = (List<Object>) session.getAttribute(USER_DATA_SESSION_ATTRIBUTE);
                    int count = 0;
                    String gameNames = "";

                    for (Object model : gameList) {
                        Map numWiz = (HashMap) model;
                        count++;
                        String [] savedGamesArray = ((String) numWiz.get("saved_games")).split("\\.");
                        String gameType = savedGamesArray[0];
                        String gameLevel = savedGamesArray[1];
                        gameNames = gameNames + "," + String.format(GAME_MESSAGE_TEXT, String.valueOf(count), gameType, gameLevel);

                    }
                    return getAskResponse(CARD_TITLE, gameNames + SAVED_GAME_START_TEXT);

                } else {//show the intent to start the new game
                    logger.info("Inside GAME_RESUME_INTENT New Game");
                    return getAskResponse(CARD_TITLE, String.format(GAME_PLAY_TEXT, userNickName));
                }

            } else if (SAVED_GAME_START_INTENT.equals(intentName)) {
                //INFO take the game name and level, and begin the saved game
                String savedGameName = intent.getSlot(GAME_NAME_INTENT_SLOT).getValue();
                String savedGameLevel = intent.getSlot(GAME_LEVEL_INTENT_SLOT).getValue();

                return getAskResponse(CARD_TITLE, startTheExistingGame(savedGameName, savedGameLevel, session, GAME_START_TEXT));

            } else if (GAME_SCORE_INTENT.equals(intentName)) {
                Integer gamePoint = (Integer) session.getAttribute(GAME_POINTS_SESSION_ATTRIBUTE);
                String gameName = (String) session.getAttribute(GAME_NAME_SESSION_ATTRIBUTE);
                String gameLevel = (String) session.getAttribute(GAME_LEVEL_SESSION_ATTRIBUTE);

                String badge = PointsMapping.getBadge(gamePoint);
                logger.info("GAME_SCORE_INTENT Points={} , gameName={} , gameLevel={} and badge={} ", gamePoint, gameName, gameLevel, badge);

                Triple triple = MathHelper.getTheGameForLevel(gameName, gameLevel);
                session.setAttribute(GAME_TYPE_RESULT_SESSION_ATTRIBUTE, triple.getRight());
                String continueGame = String.format(CONTINUE_GAME_TEXT, triple.getLeft(), getGameJarganMap().get(gameName.toUpperCase()), triple.getMiddle());
                String score = String.format(GAME_SCORE_TEXT, gameName, gameLevel, gamePoint, badge);

                return getAskResponse(CARD_TITLE, score + continueGame);

            } else if ("AMAZON.HelpIntent".equals(intentName) || "AMAZON.NoIntent".equals(intentName)) {
                return getWelcomeResponse();

            } else if ("AMAZON.StopIntent".equals(intentName) || "AMAZON.CancelIntent".equals(intentName)) {
                logger.info("Inside intentName={}", intentName);
                Integer totalScore = (Integer) session.getAttribute(GAME_POINTS_SESSION_ATTRIBUTE);
                totalScore = totalScore == null? 0:totalScore;
                String badge = PointsMapping.getBadge(totalScore);

                String goodByeMessage = String.format(GAME_EXIT_MESSAGE, totalScore, badge);
                logger.info("Good Bye Message {}", goodByeMessage);
                saveRecordToDb(session);

                SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
                outputSpeech.setSsml("<speak>" + goodByeMessage + "</speak>");
                return SpeechletResponse.newTellResponse(outputSpeech);

            } else {
                logger.info("Inside intentName={}", "Others");
                return getWelcomeResponse();
            }
        } catch (Exception e) {
            logger.error("There was an exception while processing the Intent {}", e.getStackTrace());
            String gameName = (String) session.getAttribute(GAME_NAME_SESSION_ATTRIBUTE);
            String gameLevel = (String) session.getAttribute(GAME_LEVEL_SESSION_ATTRIBUTE);

            if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(gameName) && StringUtils.isNotBlank(gameLevel)) {
                return getAskResponse(CARD_TITLE, "Sorry! There was a problem. " + startTheExistingGame(gameName, gameLevel, session, GAME_RESTART_TEXT));
            } else {
                return getHelpResponse("Sorry! There was a problem. Restarting the game again!!. Please let me know your nickname.");
            }
        }
    }

    private String startTheExistingGame(String gameName, String gameLevel, Session session, String message) {
        session.setAttribute(GAME_NAME_SESSION_ATTRIBUTE, gameName);
        session.setAttribute(GAME_LEVEL_SESSION_ATTRIBUTE, gameLevel);

        Integer points = PointsMapping.getPointGameMapping().get(gameName.toUpperCase() + PointsMapping.SEPARATOR + gameLevel);

        Triple triple = MathHelper.getTheGameForLevel(gameName, gameLevel);
        session.setAttribute(GAME_TYPE_RESULT_SESSION_ATTRIBUTE, triple.getRight());

        return String.format(message, gameName, points, triple.getLeft(), getGameJarganMap().get(gameName.toUpperCase()), triple.getMiddle());
    }

    private int getTheCurrentGameScore(List<Object> gameList, String savedGameName) {
        int currentGameScore = 0;
        for (Object model : gameList) {
            Map numWiz = (HashMap) model;
            if(((String) numWiz.get("saved_games")).equalsIgnoreCase(savedGameName)) {
                currentGameScore = (int) numWiz.get("profile_score");
            }
        }
        return currentGameScore;
    }

    private int getWinningScore(String gameNameAndLevel, Integer currentScore) {
        if (null == currentScore) {
            currentScore = 0;
        }
        int points;
        Map<String, Integer> pointsForGameMapping = PointsMapping.getPointGameMapping();
        points = pointsForGameMapping.get(gameNameAndLevel);
        logger.info("Inside getWinningScore method, for game name and level {}", gameNameAndLevel);
        return points+currentScore;
    }

    @Override public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session)
            throws SpeechletException {

        logger.info("onSessionEnded requestId={}, sessionId={}",sessionEndedRequest.getRequestId(),session.getSessionId());
        saveRecordToDb(session);


    }

    private void saveRecordToDb(Session session) {
        logger.info("Inside Save Record to db");
        //INFO get the score and the update the db, also calculate the badge
        String currentGame = (String) session.getAttribute(CURRENT_GAME_NAME_SESSION_ATTRIBUTE);
        String userId = session.getUser().getUserId();
        Integer totalScore = (Integer) session.getAttribute(GAME_POINTS_SESSION_ATTRIBUTE);
        String nickName = (String) session.getAttribute(USER_NAME_SESSION_ATTRIBUTE);

        if (StringUtils.isNotBlank(currentGame) && StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(nickName)) {
            NumberWizardModel numberWizardModel = new NumberWizardModel();
            numberWizardModel.setUser_id(userId);
            numberWizardModel.setSaved_games(currentGame);
            numberWizardModel.setNickname(nickName);
            numberWizardModel.setProfile_score((null == totalScore) ? 0 : totalScore);
            numberWizardModel.setProfile_badge(PointsMapping.getBadge(totalScore));
            AwsServiceHelper.updateDataIntoDb(numberWizardModel);
            logger.info("Saved Record to db", numberWizardModel, session.getSessionId());
        }
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
