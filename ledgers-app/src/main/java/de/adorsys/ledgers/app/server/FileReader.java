package de.adorsys.ledgers.app.server;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.Charset;

@Component
public class FileReader {

    public String getStringFromFile(String fileName) {
        try {
            return IOUtils.toString(getResourceAsStream(fileName), Charset.defaultCharset());
        } catch (Exception e) {
            throw new IllegalArgumentException("Exception during reading " + fileName + " file.");
        }
    }

    private InputStream getResourceAsStream(String resourcePath) {
        return FileReader.class.getClassLoader().getResourceAsStream(resourcePath);
    }
}
