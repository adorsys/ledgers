package de.adorsys.ledgers.postings.impl;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.adorsys.ledgers.postings.db.EnablePostingsReporitory;

@Configuration
@ComponentScan(basePackages={"de.adorsys.ledgers.postings.impl"})
@EnablePostingsReporitory
public class PostingServiceConfiguration {

}
