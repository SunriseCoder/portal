package app.service;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.dao.AudioRepository;
import app.entity.Folder;

@Service
public class AudioService {
	@Autowired
	AudioRepository repository;

	public Folder getList() throws Exception {
		Folder result = repository.findAll();
		return result;
	}

	public void downloadFile(String url, HttpServletResponse response) throws Exception {
		repository.downloadFile(url, response);
	}
}
