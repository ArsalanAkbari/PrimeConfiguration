package de.nexus.prime.ccat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This Class check whether the forms are used in coretemplatesFile or not , If not it adds the form Name into the formsNotUsedInCoretemplateList.
 * @author Arsalan Akbarioroumieh
 *
 */
public class FormsUsageChecker {

	private File  formsFile;
	private File coretemplatesFile;
	private String formName ;
	private String coretemplateFileName;
	private  List formsNotUsedInCoretemplateList = new ArrayList();    // Creating a List to save the name of forms that NOT used In_Coretemplate

	/**
	 * The Constructor of the Class takes file-Path and warningsList as input and checks whether the warnings(forms taht not used in coretemplatesFile) are available then adds them in to the warningsList
	 * @param filePath The Path of available Config.
	 * @param warningsList List List of all Warnings.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public FormsUsageChecker(String filePath , List warningsList) throws ParserConfigurationException, SAXException, IOException {

		FilePath filePathInstance = new FilePath(filePath);

		setFormsFile (filePathInstance.getFormFiles());
		setCoretemplatesFile (filePathInstance.getCoretemplateFiles());

		searchInFormsFile(getFormsFile());
		addToWarningsList(warningsList);
	}

	/**
	 * This function goes through all forms and find the the name of the file and adds it into the "formsNotUsedInCoretemplateList" , the calls the "searchInCoretemplatesFile".
	 * @param formsFile All XML "form" files in Config.
	 * @param coretemplatesFile All XML "coretemplate" Files in Config.
	 * @param formsNotUsedInCoretemplateList The List that contains all the formNames that are not used.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */

	private void searchInFormsFile(File formsFile) throws ParserConfigurationException, SAXException, IOException {

		for (File file : formsFile.listFiles()) { 
			if (file.isDirectory()) {

				searchInFormsFile(file);
			}

			if (file.isFile() && file.getName().endsWith(".xml")) { 

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

				DocumentBuilder formsBuilder = factory.newDocumentBuilder();
				Document formsDoc = formsBuilder.parse(file.getAbsolutePath());

				setFormName (formsDoc.getDocumentElement().getAttribute("name"));

				formsNotUsedInCoretemplateList.add(getFormName()); 

				searchInCoretemplatesFile(getFormName() , getCoretemplatesFile() , formsNotUsedInCoretemplateList);
			}
		}
	}

	/**
	 * This function goes through all coretemplate files and find the the attribute "formName" from the file and compare it with the Parameter formName 
	 * and if the both strings are equal than it remove the formName from formsNotUsedInCoretemplateList
	 * @param formName The name of form file. 
	 * @param coretemplatesFile  All XML "coretemplate" Files in Config.
	 * @param formsNotUsedInCoretemplateList The List that contains all the formNames that are not used.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */

	private void searchInCoretemplatesFile( String formName , File  coretemplatesFile , List formsNotUsedInCoretemplateList) throws ParserConfigurationException, SAXException, IOException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		for (File file : coretemplatesFile.listFiles()) {

			if (file.isDirectory()) {

				searchInCoretemplatesFile(formName, file, formsNotUsedInCoretemplateList);
			}

			if (file.isFile()) {

				if (file.getName().endsWith(".xml")) {

					DocumentBuilder coretemplateBuilder = factory.newDocumentBuilder();
					Document coretemplateDoc = coretemplateBuilder.parse(file.getAbsolutePath());

					setCoretemplateFileName (coretemplateDoc.getDocumentElement().getAttribute("formName"));

					if (getCoretemplateFileName().equals(formName)) {

						formsNotUsedInCoretemplateList.remove(formName);

					}
				}
			}
		}
	}
	

	/**
	* This function adds available warnings to the WarningList
	* @param warningsList
	*/
	private void addToWarningsList(List warningsList) {
		warningsList.add("###################### the Forms that not Used ######################"+ "\n");

		for (int i=0 ; i<formsNotUsedInCoretemplateList.size() ; i++){

			warningsList.add(formsNotUsedInCoretemplateList.get(i));
		}
	}


	public String getFormName() {
		return formName;
	}


	public void setFormName(String formName) {
		this.formName = formName;
	}


	public String getCoretemplateFileName() {
		return coretemplateFileName;
	}


	public void setCoretemplateFileName(String coretemplateFileName) {
		this.coretemplateFileName = coretemplateFileName;
	}

	public File getFormsFile() {
		return formsFile;
	}

	public void setFormsFile(File formsFile) {
		this.formsFile = formsFile;
	}

	public File getCoretemplatesFile() {
		return coretemplatesFile;
	}

	public void setCoretemplatesFile(File coretemplatesFile) {
		this.coretemplatesFile = coretemplatesFile;
	}
}
