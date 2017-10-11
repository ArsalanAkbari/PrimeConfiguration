package de.nexus.prime.ccat;

import java.io.File;

/**
 * This class contains all available filePathes from Config directory
 * @author Arsalan Akbarioroumieh
 *
 */

public class FilePath {

	private File precessFiles ;
	private File datapoolMappingFiles; 
	private File coretemplateFiles ;
	private File formFiles ;  
	private File mailFiles ; 
	private File permissionFiles ;
	private File roleFiles;
	private File searchConfigFiles; 

	/**
	 * This Constructor takes the Configs FilePath as input and generates Config Files
	 * @param filePath The Path of available Config
	 */
	public FilePath(String filePath){

		precessFiles = new File(filePath+"\\processes\\mappings");
		datapoolMappingFiles = new File(filePath+"\\datapoolmappings"); 
		coretemplateFiles = new File(filePath+"\\coretemplates");
		formFiles = new File(filePath+"\\forms");  
		mailFiles = new File(filePath+"\\mails"); 
		permissionFiles = new File(filePath+ "\\permissions");
		roleFiles = new File(filePath + "\\roles");
		searchConfigFiles = new File(filePath+"\\searchconfig");
		
	}

	public File getPrecessFiles() {
		return precessFiles;
	}

	public void setPrecessFiles(File precessFiles) {
		this.precessFiles = precessFiles;
	}

	public File getDatapoolMappingFiles() {
		return datapoolMappingFiles;
	}

	public void setDatapoolMappingFiles(File datapoolMappingFiles) {
		this.datapoolMappingFiles = datapoolMappingFiles;
	}

	public File getCoretemplateFiles() {
		return coretemplateFiles;
	}

	public void setCoretemplateFiles(File coretemplateFiles) {
		this.coretemplateFiles = coretemplateFiles;
	}

	public File getFormFiles() {
		return formFiles;
	}

	public void setFormFiles(File formFiles) {
		this.formFiles = formFiles;
	}

	public File getMailFiles() {
		return mailFiles;
	}

	public void setMailFiles(File mailFiles) {
		this.mailFiles = mailFiles;
	}

	public File getPermissionFiles() {
		return permissionFiles;
	}

	public void setPermissionFiles(File permissionFiles) {
		this.permissionFiles = permissionFiles;
	}

	public File getRoleFiles() {
		return roleFiles;
	}

	public void setRoleFiles(File roleFiles) {
		this.roleFiles = roleFiles;
	}

	public File getSearchConfigFiles() {
		return searchConfigFiles;
	}

	public void setSearchConfigFiles(File searchConfigFiles) {
		this.searchConfigFiles = searchConfigFiles;
	}


}


