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
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
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

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.api.Track;

public class RadiostreamerGui extends JPanel {

	private static final long serialVersionUID = -4119048476727150916L;

	private static final String uri = "http://radio-in-diretta.com";

	private WebDriver driver;

	private Future<?> playerThread, readAACThread;

	private ExecutorService executor = Executors.newFixedThreadPool(10);

	private MediaView mediaView = new MediaView();

	SourceDataLine sourceDataLine;

	List<byte[]> aacBuffer;

	private boolean isPlaying = false, isAAC = false;

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

		aacBuffer = Collections.synchronizedList(new LinkedList<>());

		for (Element radioStation : radioStations) {
			final Elements link = radioStation.select("a");
			final RadiostreamButton button = new RadiostreamButton(link.get(0).absUrl("href"), link.get(0).text());
			button.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
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

						final String url = src;
						playerThread = executor.submit(new Runnable() {

							@Override
							public void run() {
								while (true) {
									play(url);
									try {
										Thread.sleep(9900);
									} catch (InterruptedException e) {
									}
								}
							}
						});
					} catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						if (driver != null)
							driver.quit();
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
		try {
			isPlaying = true;
			if (StringUtils.contains(streamURI, "m3u")) {
				String baseURL = streamURI.substring(0, streamURI.lastIndexOf('/'));
				try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(streamURI).openStream()))) {
					String line, file = null;
					while ((line = br.readLine()) != null)
						file = line;
					if (StringUtils.isNotEmpty(file)) {
						System.out.println("Line: " + file);
						play(baseURL + "/" + file);
					}
				}
			} else if (StringUtils.contains(streamURI, "mp3"))
				play(new URL(streamURI));
			 else if (StringUtils.contains(streamURI, "aac"))
				 playAAC(new URL(streamURI));
			else {
				System.out.println(streamURI);
//				play(new URL(streamURI));
				ChromeOptions options = new ChromeOptions();
				options.addArguments("--headless");

				driver = new ChromeDriver(new ChromeDriverService.Builder().usingAnyFreePort()
						.withWhitelistedIps("194.71.95.177").build(), options);
				
				driver.get(streamURI);
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Impossibile riprodurre stream [Errore: " + ex.getMessage() + "]");
			return;
		}
	}

	private void playAAC(URL stream) {
		isPlaying = true;
		System.out.println("Reading: " + stream.toString());
		try {
			SourceDataLine sourceDataLine = null;
			byte[] b;
			final ADTSDemultiplexer adts = new ADTSDemultiplexer(stream.openStream());
			final Decoder dec = new Decoder(adts.getDecoderSpecificInfo());
			final SampleBuffer buf = new SampleBuffer();
			isPlaying = true;
			while ((b = adts.readNextFrame()) != null) {
				dec.decodeFrame(b, buf);

				if (sourceDataLine == null) {
					final AudioFormat aufmt = new AudioFormat(buf.getSampleRate(), buf.getBitsPerSample(),
							buf.getChannels(), true, true);
					sourceDataLine = AudioSystem.getSourceDataLine(aufmt);
					sourceDataLine.open();
					sourceDataLine.start();
				}
				b = buf.getData();
				System.out.println(b);
				sourceDataLine.write(b, 0, b.length);
			}
		} catch (IOException e) {
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	private static void playMP4(URL stream) throws Exception {
		SourceDataLine line = null;
		byte[] b;
		try {
			final MP4Container cont = new MP4Container(stream.openStream());
			final Movie movie = cont.getMovie();
			final List<Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
			if(tracks.isEmpty()) throw new Exception("movie does not contain any AAC track");
			final AudioTrack track = (AudioTrack) tracks.get(0);

			final Decoder dec = new Decoder(track.getDecoderSpecificInfo());

			Frame frame;
			final SampleBuffer buf = new SampleBuffer();
			while(track.hasMoreFrames()) {
				frame = track.readNextFrame();
				try {
					dec.decodeFrame(frame.getData(), buf);

					if (line == null) {
						final AudioFormat aufmt = new AudioFormat(buf.getSampleRate(), buf.getBitsPerSample(),
								buf.getChannels(), true, true);
						line = AudioSystem.getSourceDataLine(aufmt);
						line.open();
						line.start();
					}
					b = buf.getData();
					System.out.println(b);
					line.write(b, 0, b.length);
				} catch(AACException e) {
					e.printStackTrace();
				}
			}
		} finally {
			if(line!=null) {
				line.stop();
				line.close();
			}
		}
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
			System.err.println("OS not recognized");
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
		chromeDriverFile.deleteOnExit();
		f.deleteOnExit();
	}

	private void play(URL streamURL) throws Exception {
		boolean isM3U8 = StringUtils.contains(streamURL.toURI().toString(), "m3u");
		System.out.println(isM3U8);
		System.out.println(streamURL.toURI().toString());
		Media media = new Media(streamURL.toURI().toString());
		if (media.getError() == null) {
			media.setOnError(new Runnable() {
				public void run() {
					media.getError().printStackTrace();
				}
			});
			MediaPlayer player = new MediaPlayer(media);
			if (player.getError() == null) {
				player.setOnError(new Runnable() {
					public void run() {
						player.getError().printStackTrace();
					}
				});
				player.play();
			} else {
				media.getError().printStackTrace();
			}
		} else {
			media.getError().printStackTrace();
		}
	}

	private void stop() {
		if (isPlaying)
			isPlaying = false;
		if (readAACThread != null) {
			readAACThread.cancel(true);
			readAACThread = null;
			aacBuffer.clear();
		}
		if (playerThread != null) {
			playerThread.cancel(true);
			playerThread = null;
		}
	}

}
