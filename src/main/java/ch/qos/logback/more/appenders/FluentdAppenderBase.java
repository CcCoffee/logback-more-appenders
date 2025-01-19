package ch.qos.logback.more.appenders;

import static ch.qos.logback.core.CoreConstants.CODES_URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.pattern.CallerDataConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

public abstract class FluentdAppenderBase<E> extends AppenderBase<E> {
    private static final String DATA_MESSAGE = "message";
    private static final String DATA_LOGGER = "logger";
    private static final String DATA_THREAD = "thread";
    private static final String DATA_LEVEL = "level";
    private static final String DATA_CALLER = "caller";
    private static final String DATA_THROWABLE = "throwable";

    private Encoder<E> encoder;
    protected Map<String, String> additionalFields;
    private String messageFieldKeyName = DATA_MESSAGE;
    private List<String> ignoredFields;

    protected Map<String, Object> createData(E event) {
        Map<String, Object> data = new HashMap<String, Object>();
        if (event instanceof ILoggingEvent) {
            ILoggingEvent loggingEvent = (ILoggingEvent) event;
            data.put(messageFieldKeyName, encoder != null ? encoder.encode(event) : loggingEvent.getFormattedMessage());
            data.put(DATA_LOGGER, loggingEvent.getLoggerName());
            data.put(DATA_THREAD, loggingEvent.getThreadName());
            data.put(DATA_LEVEL, loggingEvent.getLevel().levelStr);

            if (loggingEvent.hasCallerData()) {
                data.put(DATA_CALLER, new CallerDataConverter().convert(loggingEvent));
            }
            if (loggingEvent.getThrowableProxy() != null) {
                data.put(DATA_THROWABLE, ThrowableProxyUtil.asString(loggingEvent.getThrowableProxy()));
            }
            for (Map.Entry<String, String> entry : loggingEvent.getMDCPropertyMap().entrySet()) {
                data.put(entry.getKey(), entry.getValue());
            }
        } else {
            data.put(messageFieldKeyName, encoder != null ? encoder.encode(event) : event.toString());
        }

        if (additionalFields != null) {
            data.putAll(additionalFields);
        }

        if(ignoredFields != null) {
            ignoredFields.stream().forEach(data::remove);
        }

        return data;
    }

    public static class Field {
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private String tag;
    private String remoteHost;
    private int port;
    private boolean useEventTime;

    public void addAdditionalField(Field field) {
        if (additionalFields == null) {
            additionalFields = new HashMap<String, String>();
        }
        additionalFields.put(field.getKey(), field.getValue());
    }

    public void addIgnoredField(String fieldName) {
        if (ignoredFields == null) {
            ignoredFields = new ArrayList<String>();
        }
        ignoredFields.add(fieldName);
    }

    @Deprecated
    public void setLayout(Layout<E> layout) {
        addWarn("This appender no longer admits a layout as a sub-component, set an encoder instead.");
        addWarn("To ensure compatibility, wrapping your layout in LayoutWrappingEncoder.");
        addWarn("See also " + CODES_URL + "#layoutInsteadOfEncoder for details");
        LayoutWrappingEncoder<E> lwe = new LayoutWrappingEncoder<E>();
        lwe.setLayout(layout);
        lwe.setContext(context);
        this.setEncoder(lwe);
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isUseEventTime() {
        return this.useEventTime;
    }

    public void setUseEventTime(boolean useEventTime) {
        this.useEventTime = useEventTime;
    }

    public Encoder<E> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<E> encoder) {
        this.encoder = encoder;
    }

    public String getMessageFieldKeyName() {
        return messageFieldKeyName;
    }

    public void setMessageFieldKeyName(String messageFieldKeyName) {
        this.messageFieldKeyName = messageFieldKeyName;
    }
}
