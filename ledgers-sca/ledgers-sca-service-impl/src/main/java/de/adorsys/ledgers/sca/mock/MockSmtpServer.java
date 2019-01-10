package de.adorsys.ledgers.sca.mock;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Starts SMTP server to be used during development that logs outbound emails
 */
public class MockSmtpServer implements ApplicationRunner {

    private final static Logger log = LoggerFactory.getLogger(MockSmtpServer.class);

    @Value("${spring.mail.port}")
    private int port;

    @Override
    @SuppressWarnings("PMD.ShortMethodName")
    public void run(ApplicationArguments args) {

        SMTPServer server = new SMTPServer(new SimpleMessageListenerAdapter(new SimpleMessageListener() {
            @Override
            public boolean accept(String from, String recipient) {
                return true;
            }

            @Override
            public void deliver(String from, String recipient, InputStream data) throws IOException {
                log.info(IOUtils.toString(data, UTF_8));
            }
        }));
        server.setPort(port);
        server.start();
    }
}
