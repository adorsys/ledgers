### Mockbank data upload module


Example of configuration file for properly bootstraping this module


```
spring:
  profiles:
    active: data-test

local:
  server:
    port: 8888
```

### Import with profile data-test

```
@EnableMockBankSimpleDataTest
public class LedgersApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(LedgersApplication.class).run(args);
    }
}

```



```
mvn spring-boot:run -Dspring.profiles.active=data-test -Dlocal.server.port=8888
```

The environment variable `local.server.port` is required. That environment has to point to a running instance of a `ledger-app`.