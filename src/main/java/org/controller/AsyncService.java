package org.controller;

import org.json.simple.JSONObject;
import org.storage.xml.XMLHistoryUtil;
import org.xml.sax.SAXException;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.PrintWriter;

import static org.util.MessageUtil.*;

/**
 * Created by HP on 16.05.2015.
 */
public class AsyncService implements Runnable {
    private AsyncContext aContext;
    private int isModifiedStorage;

    public AsyncService(AsyncContext aContext, int isModifiedStorage) {
        this.aContext = aContext;
        this.isModifiedStorage = isModifiedStorage;
    }

    public void run() {
        PrintWriter out = null;

        String token = aContext.getRequest().getParameter(TOKEN);
        if (token != null && !"".equals(token)) {
            int index = getIndex(token);

            if(isModifiedStorage != index || isModifiedStorage == 0) {
                String messages = null;
                try {
                    messages = formResponse(0);//all messages
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    out = aContext.getResponse().getWriter();
                    out.print(messages);

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    out.close();
                }
            }
            aContext.complete();
        }
    }
    private String formResponse(int index) throws SAXException, IOException, ParserConfigurationException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MESSAGES, XMLHistoryUtil.getSubTasksByIndex(index));
        jsonObject.put(TOKEN, getToken(isModifiedStorage));
        return jsonObject.toJSONString();
    }
}
