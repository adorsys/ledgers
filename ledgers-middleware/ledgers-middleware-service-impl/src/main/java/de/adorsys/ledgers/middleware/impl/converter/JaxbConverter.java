package de.adorsys.ledgers.middleware.impl.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

@Slf4j
@Component
public class JaxbConverter {

    public <T> Optional<String> fromObject(T source) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(source.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();
            marshaller.marshal(source, sw);
            return Optional.ofNullable(sw.toString());
        } catch (JAXBException e) {
            log.info("Couldn't convert object to xml: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> toObject(String xmlPayload, Class<T> targetClass) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(targetClass);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return Optional.ofNullable((T) unmarshaller.unmarshal(new StringReader(xmlPayload)));
        } catch (JAXBException e) {
            log.info("Couldn't convert xml to object: {}", e.getMessage());
        }
        return Optional.empty();
    }
}
