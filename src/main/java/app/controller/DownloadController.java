package app.controller;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import app.service.AudioService;

@Controller
public class DownloadController {
	private static final Logger logger = LogManager.getLogger(DownloadController.class.getName());

	@Autowired
	private AudioService audioService;

	@RequestMapping("/audio-dl/{url}")
	public void downloadAudio(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("url") String url) throws Exception {

		// De-escaping slashes due to URL security
		url = url.replaceAll("_", "/");

		// Decode URL from Base64 to String
		byte[] decodedArray = Base64.decodeBase64(url);
		String decodedUrl = new String(decodedArray);
		decodedUrl = URLDecoder.decode(decodedUrl, "UTF-8");

		// Deleting double dots and slashes at the beginning
		String safeUrl = decodedUrl.replaceAll("\\.\\.", "");
		while (safeUrl.startsWith("/")) {
			safeUrl = safeUrl.replaceAll("^/", "");
		}

		String ipAddress = request.getRemoteAddr();
		String method = request.getMethod();
		String path = request.getServletPath();
		logger.info("From IP {} {} {} ({})", ipAddress, method, path, safeUrl);

		audioService.downloadFile(safeUrl, response);
	}
}
