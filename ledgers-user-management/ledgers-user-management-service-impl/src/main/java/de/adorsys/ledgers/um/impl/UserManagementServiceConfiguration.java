package de.adorsys.ledgers.um.impl;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.adorsys.ledgers.um.db.EnableUserManagmentRepository;

@Configuration
@ComponentScan(basePackageClasses=UserManagementServiceBasePackage.class)
@EnableUserManagmentRepository
public class UserManagementServiceConfiguration {}