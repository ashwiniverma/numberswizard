
package com.num.wiz.aws.lambda.service;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.num.wiz.aws.lambda.handler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The HandlerFactory class is responsible for finding a handler to handle
 * the request, and dispatching the request to that handler.
 */
public class HandlerFactory {
	private static final Logger logger = LoggerFactory.getLogger(HandlerFactory.class);
	private static List<IntentRequestHandler> handlers;

	static {
		handlers = new ArrayList<>();
		handlers.add(new WelcomeHandlerIntent());
		handlers.add(new HelpHandlerIntent());
		handlers.add(new NickNameHandlerIntent());
		handlers.add(new GameNameHandlerIntent());
		handlers.add(new LevelHandlerIntent());
		handlers.add(new InprogressHandlerIntent());
		handlers.add(new ResumeHandlerIntent());
		handlers.add(new SavedGameHandlerIntent());
		handlers.add(new ScoreHandlerIntent());
		handlers.add(new NewGameHandlerIntent());
	}

	public static SpeechletResponse dispatchRequest(SpeechletRequestEnvelope envelope) {

		if(envelope.getRequest() instanceof LaunchRequest) {
			logger.info("Launch Request called with params {}", envelope.getRequest());
			return handlers.get(0).handle(envelope);
		}

		for (IntentRequestHandler handler : handlers) {
			try {
				if (handler.canHandle(envelope)) {
					logger.info("Intent Request called check for handler {}" , handler.getClass().getName());
					SpeechletResponse response = handler.handle(envelope);
					logger.info("Dispatcher Response {}",new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response));
					return response;
				}
			} catch (Exception e) {
				logger.error("Exception in the dispatcher with request {} and exception",envelope.getRequest(), e.fillInStackTrace());
			}
		}
		return handlers.get(0).handle(envelope);
		//throw new RuntimeException("Exception Unhandled dispatcher request " + envelope);
	}
}
