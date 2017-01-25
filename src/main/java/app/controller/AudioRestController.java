package app.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.entity.Folder;
import app.service.AudioService;

@RestController
@RequestMapping("/rest/audio/")
public class AudioRestController {
	private static final Logger logger = LogManager.getLogger(AudioRestController.class.getName());

	@Autowired
	AudioService service;

	@RequestMapping("/list")
	public Folder list(HttpServletRequest request) throws Exception {
		String ipAddress = request.getRemoteAddr();
		String method = request.getMethod();
		String path = request.getServletPath();
		logger.info("From IP {} {} {}", ipAddress, method, path);

		Folder data = service.getList();
		return data;
	}
}
