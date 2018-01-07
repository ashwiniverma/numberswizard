package com.num.wiz.aws.lambda.service;

import com.amazon.speech.speechlet.Session;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.google.gson.Gson;
import com.num.wiz.aws.lambda.constants.Constants;
import com.num.wiz.aws.lambda.models.NumberWizardModel;
import com.num.wiz.aws.lambda.service.enums.GameType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class AwsServiceHelper {

    private static final Logger log = LoggerFactory.getLogger(AwsServiceHelper.class);
    private static final String RECOGNITION_TABLE = "GAME_RECORD_TABLE";
    private static DynamoDB dynamoDB;
    private static String imageUrl = "";

    public static List<NumberWizardModel> getSavedGame(String userId) {
        Table table = getDynamoDbClient().getTable(RECOGNITION_TABLE);
        ScanSpec scanSpec = new ScanSpec()
                .withFilterExpression("user_id = :val1")
//                .withFilterExpression("user_id = :val1 and saved_games = :val2")
                .withValueMap(new ValueMap().withString(":val1", userId));
//                .withValueMap(new ValueMap().withString(":val2", userId));

        ItemCollection<ScanOutcome> items = table.scan(scanSpec);
        Iterator<Item> iter = items.iterator();
        List<NumberWizardModel> userDataList = new ArrayList<>();

        while (iter.hasNext()) {
            Item item = iter.next();
            NumberWizardModel numberWizardModel = new NumberWizardModel();
            numberWizardModel.setNickname(item.getString("nickname"));
            numberWizardModel.setProfile_badge(item.getString("profile_badge"));
            numberWizardModel.setProfile_score(Integer.valueOf(item.getString("profile_score")));
            numberWizardModel.setSaved_games(item.getString("saved_games"));
            numberWizardModel.setUser_id(item.getString("user_id"));
            userDataList.add(numberWizardModel);
        }
        return userDataList;
    }

    public static void updateDataIntoDb(NumberWizardModel jsonStringData) {
        try {
            Table table = getDynamoDbClient().getTable(RECOGNITION_TABLE);
            Item item = new Item();
            item.withPrimaryKey("user_id", jsonStringData.getUser_id());
            item.withString("nickname", jsonStringData.getNickname());
            item.withString("profile_badge", jsonStringData.getProfile_badge());
            item.withInt("profile_score", jsonStringData.getProfile_score());
            item.withString("saved_games", jsonStringData.getSaved_games());
            table.putItem(item);
            log.info("Done {}", new Gson().toJson(jsonStringData));
        } catch (Exception e) {
            log.error("Exception while updating JSON String - {} , error- {}" , jsonStringData.toString(), e.fillInStackTrace());
        }
    }

    private static DynamoDB getDynamoDbClient() {
        if (null == dynamoDB) {
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
            dynamoDB = new DynamoDB(client);
        }
        return dynamoDB;
    }

    public static void saveRecordToDb(Session session) {
        log.info("Inside Save Record to db");
        //INFO get the score and the update the db, also calculate the badge
        String gameName = (String) session.getAttribute(Constants.GAME_NAME_SESSION_ATTRIBUTE);
        String gameLevel = (String) session.getAttribute(Constants.GAME_LEVEL_SESSION_ATTRIBUTE);
        String userId = session.getUser().getUserId();
        Integer totalScore = (Integer) session.getAttribute(Constants.GAME_POINTS_SESSION_ATTRIBUTE);
        String nickName = (String) session.getAttribute(Constants.USER_NAME_SESSION_ATTRIBUTE);

        if (StringUtils.isNotBlank(gameName) && StringUtils.isNotBlank(gameLevel) && StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(nickName)) {
            NumberWizardModel numberWizardModel = new NumberWizardModel();
            numberWizardModel.setUser_id(userId);
            numberWizardModel.setSaved_games(gameName.toUpperCase()+PointsMappingService.SEPARATOR+gameLevel);
            numberWizardModel.setNickname(nickName);
            numberWizardModel.setProfile_score((null == totalScore) ? 0 : totalScore);
            numberWizardModel.setProfile_badge(PointsMappingService.getBadge(totalScore));
            updateDataIntoDb(numberWizardModel);
            log.info("Saved Record to db", numberWizardModel, session.getSessionId());
        }
    }

    public static String getImageUrl(){
        return imageUrl;
    }
}
