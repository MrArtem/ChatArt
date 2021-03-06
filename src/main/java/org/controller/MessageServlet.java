package org.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.dao.MessageDao;
import org.dao.MessageDaoImpl;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.model.InfoMessage;
import org.storage.xml.XMLHistoryUtil;
import org.util.ServletUtil;
import org.xml.sax.SAXException;

import static org.util.MessageUtil.*;

@WebServlet(urlPatterns = {"/chat"}, loadOnStartup = 1, asyncSupported=true)
public class MessageServlet extends HttpServlet {     
	private static int ID;
	private Lock lock = new ReentrantLock();
	private int isModifiedStorage = 0;
	private static Logger logger = Logger.getLogger(MessageServlet.class.getName());
	private final static Queue<AsyncContext> STORAGE = new ConcurrentLinkedQueue<AsyncContext>();
	private MessageDao messageDao = new MessageDaoImpl();

	@Override
	public void init() throws ServletException {
		super.init();
		logger.info("intit have done.");
		ID = messageDao.selectAll().size();
		try {
			logger.info("started load histor to xml");
			loadHistory();
		} catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
			logger.error(e);
		}

	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("starte doPost");
		String data = ServletUtil.getMessageBody(request);
		InfoMessage message  = null;
		try {
			JSONObject json = stringToJson(data);
			message = jsonToMessages(json);
			logger.info(message.toJSONString());
			lock.lock();

			try {
				message.setID(ID++);
				message.setRequst("POST");
				isModifiedStorage++;
			} finally {
				lock.unlock();
			}
			logger.info("doPost has done.");

			try {
				messageDao.add(message);
			} catch (SQLException e) {
				logger.error(e);
				e.printStackTrace();
			}
		} catch (ParseException e) {
			logger.error(e);
		}
		logger.info("started answer for request");
		//removeAsContext(message);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String token = request.getParameter(TOKEN);
		logger.info("doGet");
		try {
			if (token != null && !"".equals(token)) {
				int index = getIndex(token);
				if(isModifiedStorage == index && isModifiedStorage != 0) {
					logger.info("GET - Response status: 304");
					response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				} else {
					String messages;
					messages = formResponse(0);
					response.setContentType(ServletUtil.APPLICATION_JSON);
					PrintWriter out = response.getWriter();
					out.print(messages);
					out.flush();
				}
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
		InfoMessage message = null;
		logger.info("doPut");
		try {
			JSONObject json = stringToJson(data);
			message = jsonToMessages(json);
			message.setRequst("PUT");
			messageDao.update(message);
			isModifiedStorage++;
		} catch (ParseException e) {
			e.printStackTrace();
			logger.error(e);
		}
		logger.info("started answer for request");
		//removeAsContext(message);
	}
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String data = ServletUtil.getMessageBody(request);
		InfoMessage message = null;
		logger.info("Delete");
		if (data != null) {

			JSONObject json = null;
			try {
				json = stringToJson(data);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			message = jsonToMessages(json);
			message.setRequst("DELETE");
			message.setText("message has deleted.");
			message.setDelete(true);
			messageDao.delete(message);
			isModifiedStorage++;
			logger.info("delete "+message.getID());
		}
		logger.info("started answer for request");
		//removeAsContext(message);

	}
	@SuppressWarnings("unchecked")
	private String formResponse(int index) throws SAXException, IOException, ParserConfigurationException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(MESSAGES,messageDao.selectAll());
		jsonObject.put(TOKEN, getToken(isModifiedStorage));
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
			}  catch (SAXException | IOException | TransformerException | ParserConfigurationException e) {
				System.err.print(e);
				logger.error(e);
			}
		}
	}
	@Override
	public void destroy() {
		super.destroy();
	}
/*	protected void processRequest(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {

		System.out.println("Async Servlet with thread: " + Thread.currentThread().toString());
		final AsyncContext ac = request.startAsync();
		ac.setTimeout(10*60*1000);
		ac.addListener(new AsyncListener() {
			public void onComplete(AsyncEvent event) throws IOException {
				System.out.println("Async complete");
				storage.remove(ac);
			}

			public void onTimeout(AsyncEvent event) throws IOException {
				System.out.println("Timed out...");
				storage.remove(ac);
			}

			public void onError(AsyncEvent event) throws IOException {
				System.out.println("Error...");
				storage.remove(ac);
			}

			public void onStartAsync(AsyncEvent event) throws IOException {
				System.out.println("Starting async...");

			}
		});
		String token = request.getParameter(TOKEN);
		int index = getIndex(token);
		if(isModifiedStorage == index && isModifiedStorage != 0) {
			storage.add(ac);
		} else {
			    isModifiedStorage++;
				new AsyncService(ac, isModifiedStorage,null).run();
				ac.complete();
		}
		System.out.println("Servlet completed request handling");
	}
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}
	public void removeAsContext(InfoMessage message) {
		for (AsyncContext asyncContext : storage) {
			 new AsyncService(asyncContext, isModifiedStorage,message).run();
			asyncContext.complete();
			storage.remove(asyncContext);
		}
	}*/
}
