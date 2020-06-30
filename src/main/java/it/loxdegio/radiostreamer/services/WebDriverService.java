package it.loxdegio.radiostreamer.services;

import static it.loxdegio.radiostreamer.utils.OSValidator.isMac;
import static it.loxdegio.radiostreamer.utils.OSValidator.isUnix;
import static it.loxdegio.radiostreamer.utils.OSValidator.isWindows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellResponse;

@Service
public class WebDriverService {

	private static final String CDN_BASEURL = "https://chromedriver.storage.googleapis.com";
	private static final String CDN_LATESTURL = CDN_BASEURL + "/LATEST_RELEASE_%s";
	private static final String CDN_VERSIONURL = CDN_BASEURL + "/%s/chromedriver_%s.zip";
	
	private static final File driverDir = new File(FileUtils.getTempDirectory(), "Driver");

	public ChromeDriver driver;

	public void initDriver() throws Exception {

		PowerShellResponse response = PowerShell.executeSingleCommand(
				"(Get-Item (Get-ItemProperty 'HKLM:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\chrome.exe').'(Default)').VersionInfo.ProductVersion");
		String chromeVersion = response.getCommandOutput();

		saveDriverFile(getChromeDriverResourceUrl(Optional.fromNullable(chromeVersion.split("\\.")[0])));

	}

	private URL getChromeDriverResourceUrl(Optional<String> majorVersion) throws IOException {
		URL lastestDriverVersionURL = new URL(String.format(CDN_LATESTURL, majorVersion.or("83")));
		System.out.println(lastestDriverVersionURL.toString());
		String lastestDriverVersion = IOUtils.toString(lastestDriverVersionURL.openStream(), Charset.defaultCharset());

		String chromeDriverURI = String.format(CDN_VERSIONURL, lastestDriverVersion, "%s");
		URL chromeDriverURL = null;
		if (isWindows()) {
			chromeDriverURL = new URL(String.format(chromeDriverURI, "win32"));
		} else if (isMac()) {
			chromeDriverURL = new URL(String.format(chromeDriverURI, "mac64"));
		} else if (isUnix()) {
			chromeDriverURL = new URL(String.format(chromeDriverURI, "linux64"));
		} else {
			System.err.println("OS not recognized");
			System.exit(1);
		}
		return chromeDriverURL;
	}

	private void saveDriverFile(URL url) throws IOException {
		if (!driverDir.exists()) {
			driverDir.mkdirs();
		}

		File chromeDriverZipFile = new File(driverDir, "chromedriver.zip");
		if (!chromeDriverZipFile.exists()) {
			chromeDriverZipFile.createNewFile();
			FileUtils.copyURLToFile(url, chromeDriverZipFile);

			File chromeDriverFile = unzip(chromeDriverZipFile, driverDir);

			if (isMac() || isUnix())
				Files.setPosixFilePermissions(Paths.get(chromeDriverFile.getAbsolutePath()),
						PosixFilePermissions.fromString("rwxr-xr--"));
			System.setProperty("webdriver.chrome.driver", chromeDriverFile.getAbsolutePath());
			chromeDriverFile.deleteOnExit();
		}
		chromeDriverZipFile.deleteOnExit();
		driverDir.deleteOnExit();
	}

	private File unzip(File zipFile, File destDir) {
		// create output directory if it doesn't exist
		FileInputStream fis;
		File newFile = null;
		// buffer for read and write data to file
		byte[] buffer = new byte[1024];
		try {
			fis = new FileInputStream(zipFile);
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String fileName = ze.getName();
				newFile = new File(destDir, fileName);
				System.out.println("Unzipping to " + newFile.getAbsolutePath());
				// create directories for sub directories in zip
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				// close this ZipEntry
				zis.closeEntry();
				ze = zis.getNextEntry();
			}
			// close last ZipEntry
			zis.closeEntry();
			zis.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newFile;

	}

	public void closeBrowser() {
		if (driver != null) {
			driver.quit();
			driver = null;
		}

	}

	public void openBrowser() {
		driver = new ChromeDriver(
				new ChromeDriverService.Builder().usingAnyFreePort().withWhitelistedIps("185.80.92.77").build(),
				getDefaultOptions());
	}

	private ChromeOptions getDefaultOptions() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		return options;
	}

}
