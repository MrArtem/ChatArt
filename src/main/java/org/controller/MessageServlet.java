package org.controller;

import static org.util.MessageUtil.TASKS;
import static org.util.MessageUtil.TOKEN;
import static org.util.MessageUtil.getIndex;
import static org.util.MessageUtil.getToken;
import static org.util.MessageUtil.jsonToMessages;
import static org.util.MessageUtil.stringToJson;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.model.InfoMessage;
import org.storage.xml.XMLHistoryUtil;
import org.util.ServletUtil;
import org.xml.sax.SAXException;

@WebServlet(value = "/chat", loadOnStartup = 1)
public class MessageServlet extends HttpServlet {     
	private static final long serialVersionUID = 1L;
	private static int ID;
	private Lock lock = new ReentrantLock();
	private static Logger logger = Logger.getLogger(MessageServlet.class.getName());

	@Override
	public void init() throws ServletException {
		super.init();
		logger.info("intit have done.");
		try {
			loadHistory();
		} catch (SAXException e) {
			logger.error(e);
			System.err.print(e);
		} catch (IOException e) {
			logger.error(e);
			System.err.print(e);
		} catch (ParserConfigurationException e) {
			logger.error(e);
			System.err.print(e);
		} catch (TransformerException e) {
			logger.error(e);
			System.err.print(e);
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("starte doPost");
		String data = ServletUtil.getMessageBody(request);
		try {
			JSONObject json = stringToJson(data);
			InfoMessage message = jsonToMessages(json);
			logger.info(message.toJSONString());
			lock.lock();
			try {
				message.setID(ID++);
				message.setRequst("POST");
			} finally {
				lock.unlock();
			}
			System.out.println(message.toJSONString());
			logger.info("doPost has done.");
			try {
				XMLHistoryUtil.addData(message);
			} catch (ParserConfigurationException e) {
				logger.error(e);
				e.printStackTrace();
			} catch (SAXException e) {
				logger.error(e);
				e.printStackTrace();
			} catch (TransformerException e) {
				logger.error(e);
				e.printStackTrace();
			}
		} catch (ParseException e) {
			System.err.println("Invalid user message: " + e.getMessage());
			logger.error(e);
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String token = request.getParameter(TOKEN);
		logger.info("doGet");
		try {
			if (token != null && !"".equals(token)) {
				int index = getIndex(token);
				
				String messages;
				messages = formResponse(index);
				response.setContentType(ServletUtil.APPLICATION_JSON);
				PrintWriter out = response.getWriter();
				out.print(messages);
				out.flush();
			} else {
				logger.error("'token' parameter needed");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'token' parameter needed");
			}
		} catch (SAXException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"error");
			logger.error(e);
		} catch (ParserConfigurationException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "error");
			logger.error(e);
		}
	}
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String data = ServletUtil.getMessageBody(request);
		logger.info("doPut");
		try {
			JSONObject json = stringToJson(data);
			InfoMessage message = jsonToMessages(json);
			message.setRequst("PUT");
			logger.info(message.toJSONString());
			try {
				XMLHistoryUtil.updateData(message);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				logger.error(e);
			} catch (SAXException e) {
				e.printStackTrace();
				logger.error(e);
			} catch (TransformerException e) {
				e.printStackTrace();
				logger.error(e);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
				logger.error(e);
			}
		} catch (ParseException e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
		String token = request.getParameter(TOKEN);
		logger.info("Delete");
		if (token != null && !"".equals(token)) {
			int index = getIndex(token);
			try {
				XMLHistoryUtil.deleteDate(index);
				logger.info("delete "+index);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				logger.error(e);
			} catch (SAXException e) {
				e.printStackTrace();
				logger.error(e);
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(e);
			} catch (TransformerException e) {
				e.printStackTrace();
				logger.error(e);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
				logger.error(e);
			}
		}

	}
	@SuppressWarnings("unchecked")
	private String formResponse(int index) throws SAXException, IOException, ParserConfigurationException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(TASKS, XMLHistoryUtil.getSubTasksByIndex(index));
		ID = XMLHistoryUtil.getSubTasksByIndex(index).size();
		jsonObject.put(TOKEN, getToken(XMLHistoryUtil.getStorageSize()));
		return jsonObject.toJSONString();
	}
	
	private void loadHistory() throws SAXException, IOException, ParserConfigurationException, TransformerException {
		if (!XMLHistoryUtil.doesStorageExist()) { // creating storage and history if not exist
			XMLHistoryUtil.createStorage();
			//addStubData();
		}
	}
	
	private void addStubData() throws ParserConfigurationException, TransformerException {
		InfoMessage[] stubTasks = { 
				new InfoMessage(4, "user","Write The Chat !") };
		for (InfoMessage task : stubTasks) {
			try {
				XMLHistoryUtil.addData(task);
			} catch (ParserConfigurationException e) {
				System.err.print(e);
				logger.error(e);
			} catch (SAXException e) {
				System.err.print(e);
				logger.error(e);
			} catch (IOException e) {
				System.err.print(e);
				logger.error(e);
			} catch (TransformerException e) {
				System.err.print(e);
				logger.error(e);
			}
		}
	}
	@Override
	public void destroy() {
		System.out.print("Hi");
		super.destroy();
	}
}
