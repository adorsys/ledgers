package de.adorsys.ledgers.middleware.rest.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

public class MultiReadHttpServletRequest extends HttpServletRequestWrapper {
    private String requestBody;
    private static final Logger logger = LoggerFactory.getLogger(MultiReadHttpServletRequest.class);

    public MultiReadHttpServletRequest(HttpServletRequest request) {
        super(request);
        try {
            requestBody = request.getReader()
                                  .lines()
                                  .collect(joining(lineSeparator()));
        } catch (IOException e) {
            logger.error("MultiReadHttpServletRequest exception: {}", e.getMessage());
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(requestBody.getBytes(UTF_8));
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), UTF_8));
    }
}
