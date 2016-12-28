package app.dao;

import javax.annotation.ManagedBean;

import app.entity.Folder;

@ManagedBean
public class AudioRepositoryImpl implements AudioRepository {

	public Folder findAll() {
		return new Folder();
	}
}
