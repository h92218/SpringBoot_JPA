package study.datajpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;
import java.util.UUID;

@SpringBootApplication
//패키지가 다른 위치에 있으면 적어줘야 하지만 스프링부트를 사용하는 일반적인 경우는 안써도 됨
//@EnableJpaRepositories(basePackages = "study.datajpa.repository")

//스프링 데이터 JPA Auditing 기능 사용시 꼭 필요
//(modifyOnCreate = false) 하면 create시에 update 컬럼은 null
@EnableJpaAuditing
public class DataJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataJpaApplication.class, args);
	}


	@Bean
	public AuditorAware<String> auditorProvider(){
		return () -> Optional.of(UUID.randomUUID().toString());
	}
}
