package app.controller;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import app.service.AudioService;

@Controller
public class DownloadController {
	@Autowired
	private AudioService audioService;

	@RequestMapping("/audio-dl/{url}")
	public void downloadAudio(@PathVariable("url") String url, HttpServletResponse response) throws Exception {
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

		audioService.downloadFile(safeUrl, response);
	}
}
