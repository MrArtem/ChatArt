package org.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.model.InfoMessage;

public final class MessageUtil {
	public static final String TOKEN = "token";
	public static final String MESSAGES = "messages";
	private static final String TN = "TN";
	private static final String EN = "EN";
	private static final String ID = "id";
	private static final String DESCRIPTION = "description";
	private static final String DONE = "done";

	private MessageUtil() {
	}
	
	public static String getToken(int index) {
		Integer number = index;
		return TN + number + EN;
	}
	
	public static int getIndex(String token) {
		return (Integer.valueOf(token.substring(2, token.length() - 2)));
	}

	public static JSONObject stringToJson(String data) throws ParseException {
		JSONParser parser = new JSONParser();
		return (JSONObject) parser.parse(data.trim());
	}
	
	public static InfoMessage jsonToMessages(JSONObject json) {
		return InfoMessage.parseInfoMessage(json);
	}


}
