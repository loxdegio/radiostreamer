package it.loxdegio.radiostreamer;

import java.awt.EventQueue;

import javax.swing.ImageIcon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import it.loxdegio.radiostreamer.core.Radiostreamer;
import it.loxdegio.radiostreamer.services.WebDriverService;

@SpringBootApplication
@ComponentScan("it.loxdegio")
public class Application implements CommandLineRunner {
	
	@Autowired
	private WebDriverService driverService;
	
	@Autowired
	private Radiostreamer frame;

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class).headless(false).run(args);
	}

	public void run(String... args) throws Exception {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					driverService.initDriver();
					
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
