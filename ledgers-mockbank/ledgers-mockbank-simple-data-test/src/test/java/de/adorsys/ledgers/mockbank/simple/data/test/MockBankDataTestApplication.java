package de.adorsys.ledgers.mockbank.simple.data.test;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(MockBankSimpleDataTestConfiguration.class)
public class MockBankDataTestApplication{
	
	@Configuration
	static class AppConfiguration {
		
		/**
		 * Mock entity manager. Since we do not have the whole ledger persistence context in this module.
		 * @return
		 */
		@Bean
		public EntityManager entityManager() {
			EntityManager em = Mockito.mock(EntityManager.class);
			Mockito.when(em.createNativeQuery(Mockito.anyString())).thenReturn(Mockito.mock(Query.class));
			return em;
		}
	}
	public static void main(String[] args) {
		new SpringApplicationBuilder(MockBankDataTestApplication.class).run(args);
	}
}
