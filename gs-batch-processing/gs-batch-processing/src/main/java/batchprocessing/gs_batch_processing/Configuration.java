package batchprocessing.gs_batch_processing;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@org.springframework.context.annotation.Configuration
@EnableBatchProcessing
public class Configuration {
	@Bean
    public JobCompletionNotificationListener jobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
        return new JobCompletionNotificationListener(jdbcTemplate);
    }

	
	@Bean
	public FlatFileItemReader<Person> reader() {
	  return new FlatFileItemReaderBuilder<Person>()
	    .name("personItemReader")
	    .resource(new ClassPathResource("sample-data.csv"))
	    .delimited()
	    .names("firstName", "lastName")
	    .targetType(Person.class)
	    .build();
	}
	
	@Bean
	public PersonItemProcessor processor() {
	  return new PersonItemProcessor();
	}
	
	@Bean
	public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
	  return new JdbcBatchItemWriterBuilder<Person>()
	    .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
	    .dataSource(dataSource)
	    .beanMapped()
	    .build();
	}
	
	@Bean
	public Job importUserJob(JobRepository jobRepository,Step step1, JobCompletionNotificationListener listener) {
	  return new JobBuilder("importUserJob", jobRepository)
	    .listener(listener)
	    .start(step1)
	    .build();
	}

	@Bean
	public Step step1(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
	          FlatFileItemReader<Person> reader, PersonItemProcessor processor, JdbcBatchItemWriter<Person> writer) {
	  return new StepBuilder("step1", jobRepository)
	    .<Person, Person> chunk(3, transactionManager)
	    .reader(reader)
	    .processor(processor)
	    .writer(writer)
	    .build();
	}
	
//	 @Bean
//	    public JobCompletionNotificationListener jobCompletionNotificationListener() {
//	        return new JobCompletionNotificationListener();
//	    }
}
