package com.num.wiz.aws.lambda.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;


public class AwsServiceHelper {

    private static final Logger log = LoggerFactory.getLogger(NumberWizardHandler.class);
    private static final String RECOGNITION_TABLE = "GAME_RECORD_TABLE";
    private static String imageUrl = "";

    public static String getSavedGame(String userId){
        int count = 0;
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable(RECOGNITION_TABLE);
        ScanSpec scanSpec = new ScanSpec()
                .withFilterExpression("contains(#user_id , :value)")
                .withNameMap(new NameMap().with("#user_id", "user_id"))
                .withValueMap(new ValueMap().withString(":value", userId));

        ItemCollection<ScanOutcome> items = table.scan(scanSpec);
        Iterator<Item> iter = items.iterator();
        count = items.getAccumulatedItemCount();

        StringBuilder stringBuilder = new StringBuilder(0);

        while (iter.hasNext()) {
            Item item = iter.next();
            count++;
            log.info("Found Records: {}" ,item.toString());
        }

        if(0 == count){
            stringBuilder.append("Sorry, currently there are no saved games.");
        } else {
            stringBuilder.append("I found "+ count + " saved games");
        }
        return stringBuilder.toString();
    }

    public static String getImageUrl(){
        return imageUrl;
    }
}
