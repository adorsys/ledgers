package de.adorsys.ledgers.um.db.repository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages={"de.adorsys.ledgers.um.db"})
@EnableJpaRepositories
@EntityScan(basePackages="de.adorsys.ledgers.um.db.domain")
public class UmRepositoryApplication {

    public static void main(String[] args) {

        SpringApplication.run(UmRepositoryApplication.class, args);
    }

}
