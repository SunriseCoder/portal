package app.dao;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Repository;

import app.entity.Folder;

@Repository
public interface AudioRepository {
	Folder findAll() throws Exception;
	void downloadFile(String url, HttpServletResponse response) throws Exception;
}
