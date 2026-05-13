package com.musicstore.music_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MusicApiApplication {

	public static void main(String[] args) {


		SpringApplication.run(MusicApiApplication.class, args);
	}

}

