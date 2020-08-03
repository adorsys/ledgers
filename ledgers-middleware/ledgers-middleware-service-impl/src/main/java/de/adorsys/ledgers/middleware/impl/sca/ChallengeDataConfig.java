package de.adorsys.ledgers.middleware.impl.sca;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.middleware.api.domain.sca.ChallengeDataTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.annotation.RequestScope;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequestScope
@Configuration
public class ChallengeDataConfig {
    private static final String DEFAULT_CHALLENGE_DATA_PATH = "classpath:sca_challenge_data.json";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String FILE_PREFIX = "file:";

    @Value("${ledgers.sca.challenge_data.path:}")
    private String customChallengeDataPath;

    @JsonAnySetter
    private Map<String, ChallengeDataTO> challengeDatas = new HashMap<>();

    public ChallengeDataConfig(ObjectMapper mapper, ResourceLoader resourceLoader) {
        Resource resource = resourceLoader.getResource(resolveChallengeDataPath());
        try (InputStream is = resource.getInputStream()) {
            String content = IOUtils.toString(is, StandardCharsets.UTF_8);
            if (StringUtils.isNoneBlank(content)) {
                challengeDatas = mapper.readValue(content, new TypeReference<Map<String, ChallengeDataTO>>() {
                });
            }
        } catch (IOException e) {
            log.error("Could not read challenge data");
        }
    }

    @Bean
    @RequestScope
    public Map<String, ChallengeDataTO> challengeDatas() {
        return challengeDatas;
    }

    private String resolveChallengeDataPath() {
        if (StringUtils.isBlank(customChallengeDataPath)) {
            return DEFAULT_CHALLENGE_DATA_PATH;
        } else {
            if (customChallengeDataPath.startsWith(CLASSPATH_PREFIX)
                        || customChallengeDataPath.startsWith(FILE_PREFIX)) {
                return customChallengeDataPath;
            }
            return FILE_PREFIX + customChallengeDataPath;
        }
    }
}
