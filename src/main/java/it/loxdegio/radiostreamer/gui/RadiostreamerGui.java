package it.loxdegio.radiostreamer.gui;

import static it.loxdegio.radiostreamer.utils.OSValidator.isMac;
import static it.loxdegio.radiostreamer.utils.OSValidator.isUnix;
import static it.loxdegio.radiostreamer.utils.OSValidator.isWindows;

import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.util.ResourceUtils;

import javazoom.jl.player.Player;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;

public class RadiostreamerGui extends JPanel {

	private static final long serialVersionUID = -4119048476727150916L;

	private static final String uri = "http://radio-in-diretta.com";

	private Future<?> playerThread;

	private ExecutorService executor;

	private Player player;

	private boolean isPlaying = false;

	private boolean stop = false;

	public RadiostreamerGui(LayoutManager layout) throws Exception {

		super(layout);

		initDriver();

		Document doc = Jsoup.connect(uri).get();

		Elements radioStations = doc.select("div.radio-item");

		Collections.sort(radioStations, new Comparator<Element>() {

			@Override
			public int compare(Element e1, Element e2) {
				final Elements link1 = e1.select("a"), link2 = e2.select("a");
				return link1.get(0).text().compareTo(link2.get(0).text());
			}

		});

		for (Element radioStation : radioStations) {
			final Elements link = radioStation.select("a");
			final RadiostreamButton button = new RadiostreamButton(link.get(0).absUrl("href"), link.get(0).text());
			button.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {

					WebDriver driver = null;
					try {

						ChromeOptions options = new ChromeOptions();
						options.addArguments("--headless");

						driver = new ChromeDriver(new ChromeDriverService.Builder().usingAnyFreePort()
								.withWhitelistedIps("185.80.92.77").build(), options);

						driver.get(button.getUri());

						String src = null;
						WebElement media = new WebDriverWait(driver, 10).until(ExpectedConditions
								.presenceOfElementLocated(By.cssSelector("#jp_audio_0[src*=\"http\"]")));
						if (media.getAttribute("src").contains("http://0")) {
							WebElement script = new WebDriverWait(driver, 10)
									.until(ExpectedConditions.presenceOfElementLocated(
											By.xpath(".//*/script[contains(text(), 'hls.loadSource')]")));

							src = StringUtils.substringBetween(script.getAttribute("innerHTML"), "hls.loadSource('",
									"');");

						} else
							src = media.getAttribute("src");

						stop();

						play(src);

					} catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						if (driver != null) {
							driver.quit();
						}
					}
				}

				public void mousePressed(MouseEvent e) {

				}

				public void mouseReleased(MouseEvent e) {

				}

				public void mouseEntered(MouseEvent e) {

				}

				public void mouseExited(MouseEvent e) {

				}
			});
			add(button);
		}

	}

	private void play(final String streamURI) {
		executor = Executors.newSingleThreadExecutor();
		playerThread = executor.submit(new Runnable() {

			@Override
			public void run() {
				try {
					// Playing
					if (StringUtils.endsWith(streamURI, ".mp3"))
						play(openConnection(streamURI));
					else if(StringUtils.endsWith(streamURI, ".m3u8"))
						playM3U8(streamURI);
					else
						throw new Exception("Stream non supportato");
						//playAAC(openConnection(streamURI));

				} catch (Exception ex) {
					System.err.println("Impossibile riprodurre stream [Errore: " + ex.getMessage() + "]");
					return;
				}
			}

			private void playM3U8(String streamURI) throws Exception {
				String mediaURI = null;
				String baseURI = StringUtils.substring(streamURI, 0, streamURI.lastIndexOf('/'));
				System.out.println(streamURI);
				System.out.println(baseURI);
				try (BufferedReader br = new BufferedReader(
						new InputStreamReader(openConnection(streamURI).openStream()))) {
					String line = null;
					while ((line = br.readLine()) != null) {
						if (!StringUtils.startsWith(line, "#")) {
							if (StringUtils.contains(line, ".m3u")) {
								playM3U8(baseURI + "/" + line);
							} else if (StringUtils.contains(line, ".aac")) {
								mediaURI = baseURI + "/" + line;
								break;
							}
						}
					}
					System.out.println(mediaURI);
					if (StringUtils.isNotBlank(mediaURI))
						playAAC(openConnection(mediaURI));
				}
			}

		});

	}

	private URL openConnection(String uri) throws IOException {
		URL url = new URL(uri);
		url.openConnection();
		return url;
	}

	private void initDriver() throws Exception {
		URL chromeDriverUrl = null;

		if (isWindows()) {
			chromeDriverUrl = ResourceUtils.getURL("classpath:bin/chromedriver.exe");
		} else if (isMac()) {
			chromeDriverUrl = ResourceUtils.getURL("classpath:bin/chromedriver-osx");
		} else if (isUnix()) {
			chromeDriverUrl = ResourceUtils.getURL("classpath:bin/chromedriver-amd64");
		} else {
			System.out.println("OS not recognized");
			System.exit(1);
		}

		File f = new File(FileUtils.getUserDirectory(), "Driver");
		if (!f.exists()) {
			f.mkdirs();
		}
		File chromeDriverFile = new File(f, "chromedriver.exe");
		if (!chromeDriverFile.exists()) {
			chromeDriverFile.createNewFile();
			FileUtils.copyURLToFile(chromeDriverUrl, chromeDriverFile);
		}
		if (isMac() || isUnix())
			Files.setPosixFilePermissions(Paths.get(chromeDriverFile.getAbsolutePath()),
					PosixFilePermissions.fromString("rwxr-xr--"));
		System.setProperty("webdriver.chrome.driver", chromeDriverFile.getAbsolutePath());
	}

	private void playAAC(URL streamURL) throws Exception {
		SourceDataLine line = null;
		byte[] b;
		final ADTSDemultiplexer adts = new ADTSDemultiplexer(streamURL.openStream());
		final Decoder dec = new Decoder(adts.getDecoderSpecificInfo());
		final SampleBuffer buf = new SampleBuffer();
		isPlaying = true;
		while (!stop) {
			b = adts.readNextFrame();
			dec.decodeFrame(b, buf);

			if (line == null) {
				final AudioFormat aufmt = new AudioFormat(buf.getSampleRate(), buf.getBitsPerSample(),
						buf.getChannels(), true, true);
				line = AudioSystem.getSourceDataLine(aufmt);
				line.open();
				line.start();
			}
			b = buf.getData();
			line.write(b, 0, b.length);
		}
		stop = false;
	}

	private void play(URL streamURL) throws Exception {
		player = new Player(streamURL.openStream());
		player.play();
	}

	private void stop() {
		if (player != null)
			player.close();
		else if(isPlaying) {
			stop = true;
			isPlaying = false;
		}
		if (playerThread != null)
			playerThread.cancel(true);
	}

}
