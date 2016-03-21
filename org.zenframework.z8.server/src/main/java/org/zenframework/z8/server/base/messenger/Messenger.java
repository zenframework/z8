package org.zenframework.z8.server.base.messenger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.request.RequestTarget;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.security.IUser;

public class Messenger extends RequestTarget {
    class Message {
        public String sender;
        public String senderEmail = null;
        public String recipient;
        public String recipientEmail = null;
        public String text;

        Message(String sender, String recipient, String text) {
            this.sender = sender;
            this.recipient = recipient;
            this.text = text;
        }

        Message(String sender, String senderEmail, String recipient, String recipientEmail, String text) {
            this.sender = sender;
            this.senderEmail = senderEmail;
            this.recipient = recipient;
            this.recipientEmail = recipientEmail;
            this.text = text;
        }
    }

    static class UserOnlineInfo {
        public Date date;
        public ISession session;

        public UserOnlineInfo(Date date, ISession session) {
            this.date = date;
            this.session = session;
        }
    }

    public final static String SmsAlertAddress = Resources.get("Messenger.smsAlertAddress");
    public final static String SmsAlertCC = Resources.get("Messenger.supportAddress");

    private static final int SecondsToExpire = 10;

    private static Map<String, UserOnlineInfo> usersOnline = Collections
            .synchronizedMap(new HashMap<String, UserOnlineInfo>());

    private static Map<String, List<Message>> newMessages = Collections
            .synchronizedMap(new HashMap<String, List<Message>>());

    public Messenger(String id) {
        super(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeResponse(JsonWriter writer) {
        IUser user = ApplicationServer.getUser();
        String userName = user.name();

        Map<String, String> requestParameters = getParameters();

        String type = requestParameters.get(Json.message);

        if(Json.get.equals(type)) {
            poll(ApplicationServer.getSession());

            List<String> activeUsers = new ArrayList<String>();

            Entry<String, UserOnlineInfo>[] entries = usersOnline.entrySet().toArray(new Entry[0]);

            for(Entry<String, UserOnlineInfo> userOnline : entries)
                activeUsers.add(userOnline.getKey());

            writer.startArray(Json.users);
            for(String activeUser : activeUsers)
            	writer.write(activeUser);
            writer.finishArray();

            List<Message> messages = null;

            synchronized(newMessages) {
                messages = newMessages.get(userName);
                newMessages.remove(userName);
            }

            if(messages != null) {
                writer.startArray(Json.message);
                for(Message message : messages) {
                    writer.startObject();
                    writer.writeProperty(Json.sender, message.sender);
                    writer.writeProperty(Json.text, message.text);
                    writer.finishObject();
                }
                writer.finishArray();
            }
        }
        else if(Json.send.equals(type)) {
            JsonArray recipients = new JsonArray(requestParameters.get(Json.recipient));
            for(int index = 0; index < recipients.length(); index++) {
                String recipient = recipients.getString(index);
                sendMessage(recipient);
            }
        }
    }

    private void sendMessage(String recipient) {
        IUser user = ApplicationServer.getUser();
        String userName = user.name();
        Map<String, String> requestParameters = getParameters();

        String senderEmail = usersOnline.get(userName).session.user().email();

        UserOnlineInfo recipientInfo = usersOnline.get(recipient);

        String recipientEmail = recipientInfo != null ? usersOnline.get(recipient).session.user().email() : null;
        Message message = new Message(userName, senderEmail, recipient, recipientEmail,
                requestParameters.get(Json.text));

        synchronized(newMessages) {
            List<Message> messages = newMessages.get(recipient);

            if(messages == null) {
                messages = new ArrayList<Message>();
            }
            messages.add(message);

            newMessages.put(recipient, messages);
        }
    }

    private static synchronized void poll(ISession userSession) {
        Calendar calendar = Calendar.getInstance();
        usersOnline.put(userSession.user().name(), new UserOnlineInfo(calendar.getTime(), userSession));

        calendar.add(Calendar.SECOND, -SecondsToExpire);

        Calendar other = Calendar.getInstance();

        List<String> expiredUsers = new ArrayList<String>();

        for(Entry<String, UserOnlineInfo> e : usersOnline.entrySet()) {
            other.setTime(e.getValue().date);
            if(other.before(calendar))
                expiredUsers.add(e.getKey());
        }

        for(String expiredUser : expiredUsers) {
            usersOnline.remove(expiredUser);
        }
    }
}
