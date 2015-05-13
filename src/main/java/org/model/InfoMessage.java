package org.model;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class InfoMessage implements JSONAware {

	private String nameUser;
	private int id;
	private String text;
	private boolean deleted = false;
	private boolean changed = false;
	private String requst;
	
	public InfoMessage() {
		nameUser = "none";
		text = "";
		id = -1;		
	}
	public InfoMessage(String text,String nameUser) {
		this.nameUser = nameUser;
		this.text = text;
		id = -1;		
	}
	public InfoMessage(int id, String nameUser, String message) {
		this.id = id;
		this.nameUser = nameUser;
		this.text = message;
	}
	public InfoMessage(int id, String nameUser, String message, String requst) {
		this.id = id;
		this.nameUser = nameUser;
		this.text = message;
		this.requst = requst;
	}
	public void setRequst(String requst) {
		this.requst = requst;
	}
	public void setMessage(String message) {
		this.text = message;
	}
	public void setNameUser(String nameUser) {
		this.nameUser = nameUser;
	}
	public void setID(int id) {
		this.id = id;
	}
	public void setDelete(boolean deleted) {
		this.deleted = deleted;
	}
	public void setChange(boolean changed) {
		this.changed = changed;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getRequst() {
		return requst;
	}
	public String getNameUser() {
		return nameUser;
	}
	public int getID() {
		return id;
	}
	public String getText() {
		return text;
	}
	public boolean isDelete() {
		return deleted;
	}
	public boolean getChange() {
		return changed;
	}
	
	public void deleteMessage() {
		if(deleted != true) {
			this.text = "message has deleted.";
			this.setDelete(true);
		}
	}
	public static InfoMessage parseInfoMessage(JSONObject obj) {
		InfoMessage info = new InfoMessage();
		
		info.setNameUser((String)obj.get("user"));
		info.setMessage((String)obj.get("message"));
		if(obj.get("id") != null) {
		info.setID(Integer.parseInt(obj.get("id").toString()));
		}
		return info;
	}	

	@Override
	public String toJSONString() {
		JSONObject obj = new JSONObject();
		obj.put("user", nameUser);
		obj.put("message", text);
		obj.put("id", id);
		obj.put("requst", requst);
		return obj.toString();
	}
	@Override
	public String toString() {
		return nameUser+" : "+text;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InfoMessage other = (InfoMessage) obj;
        if (id != other.id)
            return false;
        
        return true;
	}
	@Override
    public int hashCode() {
        return 1*id+2*text.hashCode()+3*nameUser.hashCode();
    }
}
