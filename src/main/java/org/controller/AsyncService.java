package org.controller;

import org.dao.MessageDao;
import org.dao.MessageDaoImpl;
import org.json.simple.JSONObject;
import org.model.InfoMessage;
import org.storage.xml.XMLHistoryUtil;
import org.xml.sax.SAXException;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static org.util.MessageUtil.*;

/**
 * Created by HP on 16.05.2015.
 */
public class AsyncService implements Runnable {
    private AsyncContext aContext;
    private Integer isModifiedStorage;
    private InfoMessage message;
    private MessageDao messageDao;

    public AsyncService(AsyncContext aContext, Integer isModifiedStorage, InfoMessage message) {
        this.aContext = aContext;
        this.isModifiedStorage = isModifiedStorage;
        this.message = message;
        this.messageDao = new MessageDaoImpl();
    }

    public void run() {
        PrintWriter out = null;

        String token = aContext.getRequest().getParameter(TOKEN);
        if (token != null && !"".equals(token)) {
            int index = getIndex(token);

            if(message != null) {
                try {
                    out = aContext.getResponse().getWriter();
                    out.print(message.toJSONString());

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    out.close();
                }
            } else {

                String messages = null;

                try {
                    messages = formResponse();//all messages
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

        }
    }
    private String formResponse() throws SAXException, IOException, ParserConfigurationException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MESSAGES,  messageDao.selectAll());
        jsonObject.put(TOKEN, getToken(isModifiedStorage));
        return jsonObject.toJSONString();
    }
}
