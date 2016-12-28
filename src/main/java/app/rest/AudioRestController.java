package app.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.entity.Folder;
import app.service.AudioService;

@RestController
@RequestMapping("/rest/audio/")
public class AudioRestController {
	@Autowired
	AudioService service;

	@RequestMapping("/list")
	public Folder list() throws Exception {
		Folder data = service.getList();
		return data;
	}
}
