package com.num.wiz.aws.lambda.handler;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class NumberWizardHandler extends SpeechletRequestStreamHandler {

    private static final Logger log = LoggerFactory.getLogger(NumberWizardHandler.class);

    private static final Set<String> supportedApplicationIds;
    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds = new HashSet<String>();
        //supportedApplicationIds.add("amzn1.ask.skill.11138778-16ea-4e89-bd71-9befda6bfafa");
    }

    public NumberWizardHandler() {
        super(new NumberWizardSpeechIntent(), supportedApplicationIds);
        log.info("inside constructor");
    }
}
