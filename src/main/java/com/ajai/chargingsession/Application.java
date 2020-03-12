package com.ajai.chargingsession;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Class that bootstraps the application.
 * 
 * @author ajai
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.ajai.chargingsession")
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
