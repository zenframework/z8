package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.List;
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
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.file.FilesFactory;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.utils.IOUtils;

public class JmsTransport extends AbstractTransport implements ExceptionListener, Properties.Listener {

    public static final String PROTOCOL = "jms";

    private static final String PROP_MODE = "mode";

    private static enum Mode {

        OBJECT, STREAM

    }

    private static final Mode DEFAULT_MODE = Mode.OBJECT;

    private AtomicBoolean propertyChanged = new AtomicBoolean(false);

    private Connection connection;
    private Session session;
    private Destination self;
    private MessageConsumer consumer = null;
    private Mode mode;

    public JmsTransport(TransportContext context) {
        super(context);
        Properties.addListener(this);
    }

    @Override
    public void onPropertyChange(String key, String value) {
        if (ServerRuntime.JmsConnectionFactoryProperty.equalsKey(key)
                || ServerRuntime.JmsConnectionUrlProperty.equalsKey(key) || ServerRuntime.JmsModeProperty.equalsKey(key)) {
            propertyChanged.set(true);
        }
    }

    @Override
    public void connect() throws TransportException {
        if (propertyChanged.getAndSet(false)) {
            close();
        }
        if (connection == null) {
            try {
                String jmsFactoryClass = Properties.getProperty(ServerRuntime.JmsConnectionFactoryProperty);
                String jmsUrl = Properties.getProperty(ServerRuntime.JmsConnectionUrlProperty);
                String selfAddress = context.getProperty(TransportContext.SelfAddressProperty);
                ConnectionFactory connectionFactory = getConnectionFactory(jmsFactoryClass, jmsUrl);
                connection = connectionFactory.createConnection();
                connection.start();
                connection.setExceptionListener(this);
                session = connection.createSession(true, -1 /* arg not used */);
                self = session.createQueue(selfAddress);
                consumer = session.createConsumer(self);
                mode = getMode(Properties.getProperty(ServerRuntime.JmsModeProperty));
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
            if (session != null) {
                session.rollback();
            } else {
                throw new JMSException("Session is null");
            }
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
            Destination destination = session.createQueue(message.getAddress());
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            javax.jms.Message jmsMessage;
            switch (mode) {
            case OBJECT:
                jmsMessage = createObjectMessage(session, message);
                break;
            case STREAM:
                jmsMessage = createStreamMessage(session, message);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported JMS send mode '" + mode + "'");
            }
            jmsMessage.setObjectProperty(PROP_MODE, mode);
            jmsMessage.setJMSReplyTo(self);
            producer.send(jmsMessage);
            Trace.logEvent("Send IE message [" + message.getId() + "] to " + getUrl(message.getAddress()));
        } catch (Exception e) {
            throw new TransportException("Can't send IE message [" + message.getId() + "] to '" + message.getAddress()
                    + "' by JMS transport", e);
        }
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void onException(JMSException e) {
        Trace.logError("JMS exception occured", e);
    }

    @Override
    public Message receive() throws TransportException {
        try {
            javax.jms.Message jmsMessage = consumer.receive(100);
            if (jmsMessage != null) {
                String messageId = jmsMessage.getJMSMessageID();
                Destination senderDest = jmsMessage.getJMSReplyTo();
                String sender = null;
                if (senderDest instanceof Queue) {
                    sender = ((Queue) senderDest).getQueueName();
                } else if (senderDest instanceof Topic) {
                    sender = ((Topic) senderDest).getTopicName();
                }
                try {
                    Mode mode = jmsMessage.propertyExists(PROP_MODE) ? (Mode) jmsMessage.getObjectProperty(PROP_MODE)
                            : DEFAULT_MODE;
                    switch (mode) {
                    case OBJECT:
                        return parseObjectMessage(jmsMessage, sender);
                    case STREAM:
                        return parseStreamMessage(jmsMessage, sender);
                    default:
                        break;

                    }
                } catch (JMSException e) {
                    throw new TransportException("Can't parse JMS message " + messageId + " from "
                            + (sender == null ? "<unknown>" : sender), e);
                }
            }
            return null;
        } catch (JMSException e) {
            throw new TransportException("Can't receive JMS message", e);
        }
    }

    @Override
    public void close() {
        if (consumer != null) {
            try {
                consumer.close();
            } catch (JMSException e) {
                Trace.logError("Can't close JMS message consumer", e);
            }
            consumer = null;
        }
        if (session != null) {
            try {
                session.close();
            } catch (JMSException e) {
                Trace.logError("Can't close JMS session", e);
            }
            session = null;
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                Trace.logError("Can't close JMS connection", e);
            }
            connection = null;
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

    private static javax.jms.Message createObjectMessage(Session session, Message message) throws JMSException, IOException {
        List<FileInfo> fileInfos = IeUtil.filesToFileInfos(message.getExportEntry().getFiles().getFile());
        for (FileInfo fileInfo : fileInfos) {
            message.getFiles().add(Files.getFile(fileInfo));
        }
        return session.createObjectMessage(message);
    }

    private static Message parseObjectMessage(javax.jms.Message jmsMessage, String sender) throws JMSException {
        if (jmsMessage instanceof ObjectMessage) {
            Object messageObject = ((ObjectMessage) jmsMessage).getObject();
            if (messageObject instanceof Message) {
                ((Message) messageObject).setSender(sender);
                return (Message) messageObject;
            } else if (messageObject == null) {
                return null;
            } else {
                throw new JMSException("Incorrect JMS message type: " + messageObject.getClass().getCanonicalName());
            }
        } else {
            throw new JMSException("Incorrect JMS message object type: " + jmsMessage.getClass().getCanonicalName());
        }
    }

    private static javax.jms.Message createStreamMessage(Session session, Message message) throws JMSException, IOException {
        StreamMessage streamMessage = session.createStreamMessage();
        // write message object
        byte[] buff = IOUtils.objectToBytes(message);
        streamMessage.writeInt(buff.length);
        streamMessage.writeBytes(buff);
        List<FileInfo> fileInfos = IeUtil.filesToFileInfos(message.getExportEntry().getFiles().getFile());
        for (FileInfo fileInfo : fileInfos) {
            fileInfo = Files.getFile(fileInfo);
            // write file length
            streamMessage.writeLong(fileInfo.file.getSize());
            // write file contents
            InputStream in = fileInfo.file.getInputStream();
            buff = new byte[IOUtils.DefaultBufferSize];
            try {
                int count;
                while ((count = in.read(buff)) != -1) {
                    if (count > 0) {
                        streamMessage.writeBytes(buff, 0, count);
                    }
                }
            } finally {
                in.close();
            }
        }
        return streamMessage;
    }

    private static Message parseStreamMessage(javax.jms.Message jmsMessage, String sender) throws JMSException {
        if (jmsMessage instanceof StreamMessage) {
            StreamMessage streamMessage = (StreamMessage) jmsMessage;
            // read message object
            byte[] buff = new byte[streamMessage.readInt()];
            int count = streamMessage.readBytes(buff);
            if (count < buff.length) {
                throw new RuntimeException("Unexpected eof");
            }
            Object messageObject = IOUtils.bytesToObject(buff);
            if (messageObject instanceof Message) {
                Message message = (Message) messageObject;
                message.setSender(sender);
                message.setFiles(IeUtil.filesToFileInfos(message.getExportEntry().getFiles().getFile()));
                for (FileInfo fileInfo : message.getFiles()) {
                    fileInfo.file = FilesFactory.createFileItem(fileInfo.name.get());
                    // read file size
                    long size = streamMessage.readLong();
                    buff = new byte[(int) Math.min(size, IOUtils.DefaultBufferSize)];
                    try {
                        OutputStream out = fileInfo.file.getOutputStream();
                        try {
                            while (size > 0) {
                                count = streamMessage.readBytes(buff);
                                if (count < buff.length) {
                                    throw new IOException("Unexpected eof");
                                }
                                out.write(buff);
                                size -= count;
                                if (size > 0 && size < buff.length) {
                                    buff = new byte[(int) size];
                                }
                            }
                        } finally {
                            out.close();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return message;
            } else if (messageObject == null) {
                return null;
            } else {
                throw new JMSException("Incorrect JMS message object type: " + messageObject.getClass().getCanonicalName());
            }
        } else {
            throw new JMSException("Incorrect JMS message type: " + jmsMessage);
        }
    }

    private static Mode getMode(String mode) {
        return mode == null || mode.isEmpty() ? Mode.OBJECT : Mode.valueOf(mode.toUpperCase());
    }

}