package br.com.motiva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da API MOTIVA (Sprint 4).
 *
 * Na Sprint 3 o ponto de entrada era main.Main, que montava um menu de
 * console e imprimia tudo com System.out.println. Aqui o main() apenas
 * sobe o contexto do Spring: a partir deste momento quem "conversa" com o
 * sistema e o protocolo HTTP, e a saida do sistema e JSON.
 *
 * A anotacao @SpringBootApplication liga tres coisas de uma vez:
 *  - @Configuration        : esta classe pode definir beans
 *  - @EnableAutoConfiguration : o Boot configura Tomcat, DataSource,
 *                            Hibernate e Jackson olhando o classpath
 *  - @ComponentScan        : varre br.com.motiva procurando @Entity,
 *                            @Repository, @Service e @RestController
 */
@SpringBootApplication
public class MotivaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MotivaApplication.class, args);
    }
}
