package com.num.wiz.aws.lambda.constants;

public interface Constants {
    String CARD_TITLE = "Numbers Wizard";



    String GAME_NAME_INTENT = "GameNameCapture";
    String GAME_RESULT_INTENT = "GameStarted";
    String GAME_LEVEL_INTENT = "GameLevelCapture";
    String NICK_NAME_INTENT = "NickNameCapture";
    String GAME_RESUME_INTENT = "Resume";
    String GAME_SCORE_INTENT = "GetScoreIntent";
    String SAVED_GAME_START_INTENT = "SavedGameStart";

    String GAME_RESUME_INTENT_SLOT = "GameResume";
    String NAME_INTENT_SLOT = "USNickName";
    String GAME_NAME_INTENT_SLOT = "GameNames";
    String GAME_LEVEL_INTENT_SLOT = "GameLevel";

    String GAME_NAME_SESSION_ATTRIBUTE = "gameName";
    String CURRENT_GAME_NAME_SESSION_ATTRIBUTE = "currentGameName";
    String GAME_POINTS_SESSION_ATTRIBUTE = "points";
    String USER_NAME_SESSION_ATTRIBUTE = "userName";
    String GAME_LEVEL_SESSION_ATTRIBUTE = "gameLevel";
    String GAME_TYPE_RESULT_SESSION_ATTRIBUTE = "result";
    String USER_DATA_SESSION_ATTRIBUTE = "userData";
    String GAME_STATE_SESSION_ATTRIBUTE = "GameState";
    String GAME_LEVEL_RESULT_INTENT_SLOT = "GameResult";





}
