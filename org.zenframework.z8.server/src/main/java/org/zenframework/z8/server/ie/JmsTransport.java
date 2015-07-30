package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.ServerRuntime;

public class JmsTransport extends AbstractTransport implements ExceptionListener, Properties.Listener {

    public static final String PROTOCOL = "jms";

    private AtomicBoolean propertyChanged = new AtomicBoolean(false);

    private Connection connection;
    private Session session;
    private Destination self;
    private MessageConsumer consumer = null;

    @Override
    public void onPropertyChange(String key, String value) {
        if (ServerRuntime.ConnectionFactoryProperty.equalsKey(key) || ServerRuntime.ConnectionUrlProperty.equalsKey(key)) {
            propertyChanged.set(true);
        }
    }

    @Override
    public void connect(TransportContext context) throws TransportException {
        if (propertyChanged.getAndSet(false)) {
            close();
        }
        if (connection == null) {
            try {
                String jmsFactoryClass = Properties.getProperty(ServerRuntime.ConnectionFactoryProperty);
                String jmsUrl = Properties.getProperty(ServerRuntime.ConnectionUrlProperty);
                String selfAddress = context.getProperty(TransportContext.SelfAddressProperty);
                ConnectionFactory connectionFactory = getConnectionFactory(jmsFactoryClass, jmsUrl);
                connection = connectionFactory.createConnection();
                connection.start();
                connection.setExceptionListener(this);
                session = connection.createSession(true, -1 /* arg not used */);
                self = session.createQueue(selfAddress);
                consumer = session.createConsumer(self);
                Trace.logEvent("JMS transport: Connected to '" + jmsUrl + "'");
                Trace.logEvent("JMS Transport: Listening to '" + selfAddress + "'");
            } catch (JMSException e) {
                throw new TransportException("Can't open JMS connection", e);
            }
        }
    }

    @Override
    public void commit() throws TransportException {
        try {
            session.commit();
        } catch (JMSException e) {
            throw new TransportException("Can't commit JMS session", e);
        }
    }

    @Override
    public void rollback() throws TransportException {
        try {
            session.rollback();
        } catch (JMSException e) {
            throw new TransportException("Can't rollback JMS session", e);
        }
    }

    @Override
    public void init() {
        Properties.addListener(this);
    }

    @Override
    public void shutdown() {
        Properties.removeListener(this);
        close();
    }

    @Override
    public void send(Message message) throws TransportException {
        try {
            prepareFiles(message);
            Destination destination = session.createQueue(message.getAddress());
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            javax.jms.Message jmsMessage = session.createObjectMessage(message);
            jmsMessage.setJMSReplyTo(self);
            producer.send(jmsMessage);
            session.commit();
            Trace.logEvent("Send IE message [" + message.getId() + "] to " + getUrl(message.getAddress()));
        } catch (JMSException e) {
            throw new TransportException("Can't send IE message [" + message.getId() + "] to '" + message.getAddress()
                    + "' by JMS transport", e);
        }
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public boolean usePersistency() {
        return true;
    }

    @Override
    public void onException(JMSException e) {
        Trace.logError("JMS exception occured", e);
    }

    @Override
    public Message receive() throws TransportException {
        Message message = null;
        try {
            javax.jms.Message jmsMessage = consumer.receive(100);
            if (jmsMessage != null) {
                if (jmsMessage instanceof ObjectMessage) {
                    Object messageObject = ((ObjectMessage) jmsMessage).getObject();
                    if (messageObject instanceof Message) {
                        message = (Message) messageObject;
                    } else if (messageObject != null) {
                        Trace.logError(new Exception("Incorrect JMS message type: "
                                + messageObject.getClass().getCanonicalName()));
                    }
                } else {
                    Trace.logError(new Exception("Incorrect JMS message object type: "
                            + jmsMessage.getClass().getCanonicalName()));
                }
            }
        } catch (JMSException e) {
            throw new TransportException("Can't receive JMS message", e);
        }
        return message;
    }

    @Override
    public void close() {
        if (consumer != null) {
            try {
                consumer.close();
                consumer = null;
            } catch (JMSException e) {
                Trace.logError("Can't close JMS message consumer", e);
            }
        }
        if (session != null) {
            try {
                session.close();
                session = null;
            } catch (JMSException e) {
                Trace.logError("Can't close JMS session", e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (JMSException e) {
                Trace.logError("Can't close JMS connection", e);
            }
        }
        self = null;
    }

    private ConnectionFactory getConnectionFactory(String jmsFactoryClass, String jmsUrl) {
        try {
            Hashtable<String, String> props = new Hashtable<String, String>();
            props.put(Context.INITIAL_CONTEXT_FACTORY, jmsFactoryClass);
            props.put(Context.PROVIDER_URL, jmsUrl);
            javax.naming.Context ctx = new InitialContext(props);
            return (ConnectionFactory) ctx.lookup("ConnectionFactory");
        } catch (NamingException e) {
            throw new RuntimeException("Can't get connection factory '" + jmsFactoryClass + "' to '" + jmsUrl + "'", e);
        }
    }

    private static void prepareFiles(Message message) {
        Collection<FileInfo> fileInfos = IeUtil.filesToFileInfos(message.getExportEntry().getFiles().getFile());
        for (FileInfo fileInfo : fileInfos) {
            try {
                message.getFiles().add(Files.getFile(fileInfo));
            } catch (IOException e) {
                Trace.logError("Can't export file " + fileInfo, e);
            }
        }
    }

}