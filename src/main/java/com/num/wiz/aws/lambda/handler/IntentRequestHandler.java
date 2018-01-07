package com.num.wiz.aws.lambda.handler;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.SpeechletResponse;

public interface IntentRequestHandler {
     boolean canHandle(final SpeechletRequestEnvelope<IntentRequest> requestEnvelope);
     SpeechletResponse handle(final SpeechletRequestEnvelope<IntentRequest> requestEnvelope);
}
