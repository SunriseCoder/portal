package app.dao;

import org.springframework.stereotype.Repository;

import app.entity.Folder;

@Repository
public interface AudioRepository {
	Folder findAll() throws Exception;
}
