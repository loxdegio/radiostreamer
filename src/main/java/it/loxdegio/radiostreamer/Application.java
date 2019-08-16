package it.loxdegio.radiostreamer;

import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.ImageIcon;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;

import it.loxdegio.radiostreamer.core.Radiostreamer;

@SpringBootApplication
@ComponentScan("it.loxdegio")
public class Application implements CommandLineRunner {

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class).headless(false).run(args);
	}

	public void run(String... args) throws Exception {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					com.sun.javafx.application.PlatformImpl.startup(()->{});
					Radiostreamer frame = new Radiostreamer();
					frame.setTitle("Radiostreamer");
					frame.setIconImage(new ImageIcon(getClass().getClassLoader().getResource("images/radio.svg")).getImage());
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
