package app.dao;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Repository;

import app.entity.FolderEntity;

@Repository
public interface FileRepository {
	FolderEntity findAll() throws Exception;
	void downloadFile(HttpServletRequest request, HttpServletResponse response, String url) throws IOException;
}
