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
        // supportedApplicationIds.add("[unique-value-here]");
    }

    public NumberWizardHandler() {
        super(new NumberWizardBySpeechlet(), supportedApplicationIds);
        log.info("inside constructor");
    }
}
