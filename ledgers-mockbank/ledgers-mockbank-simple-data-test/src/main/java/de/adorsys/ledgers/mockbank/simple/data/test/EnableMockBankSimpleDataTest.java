package de.adorsys.ledgers.mockbank.simple.data.test;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * 
 * Enable the Mock Bank Simple Data Test Beans registration.
 *
 * @author bwa
 *
 */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = {java.lang.annotation.ElementType.TYPE})
@Documented
@Import({
	MockBankSimpleDataTestConfiguration.class
})
public @interface EnableMockBankSimpleDataTest {
}
