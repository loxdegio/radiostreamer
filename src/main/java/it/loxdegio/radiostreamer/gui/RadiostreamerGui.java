package it.loxdegio.radiostreamer.gui;

import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.loxdegio.radiostreamer.services.PlayerService;
import it.loxdegio.radiostreamer.services.WebDriverService;

@Component
public class RadiostreamerGui extends JPanel {

	private static final long serialVersionUID = -4119048476727150916L;

	private static final String uri = "http://radio-in-diretta.com";

	@Autowired
	private WebDriverService webDriverService;

	@Autowired
	private PlayerService playerService;

	public RadiostreamerGui(LayoutManager rsGridLayout) throws Exception {

		super(rsGridLayout);

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
			final RadiostreamerButton button = new RadiostreamerButton(link.get(0).absUrl("href"), link.get(0).text());
			button.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					try {

						webDriverService.openBrowser();

						webDriverService.driver.get(button.getUri());

						String src = null;
						try {
							WebElement audio = new WebDriverWait(webDriverService.driver, 10).until(ExpectedConditions
									.presenceOfElementLocated(By.cssSelector("#jp_audio_0[src*=\"http\"]")));
							src = audio.getAttribute("src");
						} catch (Exception ex) {
							WebElement script = new WebDriverWait(webDriverService.driver, 10)
									.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
											".//*/script[contains(text(), 'mp3:')]")));

							src = StringUtils.substringBetween(script.getAttribute("innerHTML"),
									"$(document).ready(function(){var stream={mp3:\"", "\"}");

						}

						playerService.stop();

						playerService.play(src);

					} catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						webDriverService.closeBrowser();
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

}
