package de.nexus.prime.ccat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class checks whether the process files are used in coretemplates file or not , if not it adds ProcessName into the warningsList
 * @author Arsalan Akbarioroumieh
 *
 */
public class ProcessUsageInCoretemplateChecker {

	private File processFiles;
	private File coretemplatesFile;
	private List processNotInCortemplateList = new ArrayList<>();
	private String processFileName;

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
	/**
	 * The Constructor of the Class  takes file-Path and warningsList as input and checks whether the warnings(the process files that not used in coretemplates) are available then adds them in to the warningsList.
	 * @param filePath The Path of available Config
	 * @param warningsList  List of all Warnings
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public ProcessUsageInCoretemplateChecker(String filePath , List warningsList) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		setProcessFiles ( new FilePath(filePath).getPrecessFiles());
		setCoretemplatesFile ( new FilePath(filePath).getCoretemplateFiles());

		searchInProcessFile();

		addToWarningsList(warningsList);
	}

	

	/**
	 * This function goes through all process Files and then for each file it finds the attribute "definitionKey"and calls searchInCoretemplatesFile function.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private void searchInProcessFile() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		for (File file : getProcessFiles().listFiles()) { 

			if(!file.isFile()) {           
				continue;
			}
			DocumentBuilder processBuilder = factory.newDocumentBuilder();
			Document processDoc = processBuilder.parse(file.getAbsolutePath());

			setProcessFileName( processDoc.getDocumentElement().getAttribute("definitionKey"));

			processNotInCortemplateList.add(getProcessFileName());

			searchInCoretemplatesFile(coretemplatesFile, getProcessFileName());
		}
	}


	/**
	 * This function goes through all coretemplates Files and for each file search in all Elements and attributes , and if it finds the attribute that is the same as processFileName ,
	 * then it removes processFileName "from processNotInCortemplateList"
	 * @param coretemplatesFile
	 * @param processFileName
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */

	private void searchInCoretemplatesFile(File coretemplatesFile, String processFileName) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		XPath xPath = XPathFactory.newInstance().newXPath();

		for (File file : coretemplatesFile.listFiles()) {

			if (file.isDirectory()) {
				searchInCoretemplatesFile(file, processFileName);
			}

			if (file.isFile() && file.getName().endsWith(".xml")) {

				DocumentBuilder coretemplateBuilder = factory.newDocumentBuilder();
				Document coretemplateDoc = coretemplateBuilder.parse(file.getAbsolutePath());

				NodeList attributsList = (NodeList) xPath.evaluate("//*/@*", coretemplateDoc, XPathConstants.NODESET); 

				for (int i = 0; i < attributsList.getLength(); i++) {

					Attr attr = (Attr) attributsList.item(i);

					if (attr.getValue().equals(processFileName)) {		                        	

						processNotInCortemplateList.removeAll(Collections.singleton(processFileName));
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
		if(!processNotInCortemplateList.isEmpty()) {

			warningsList.add("###################### process Not in Cortemplate ######################" + "\n");

			for (int i = 0; i < processNotInCortemplateList.size(); i++) {
				warningsList.add(processNotInCortemplateList.get(i));

			}
		}
	}
	
	public String getProcessFileName() {
		return processFileName;
	}

	public void setProcessFileName(String processFileName) {
		this.processFileName = processFileName;
	}



	public File getProcessFiles() {
		return processFiles;
	}



	public void setProcessFiles(File processFiles) {
		this.processFiles = processFiles;
	}



	public File getCoretemplatesFile() {
		return coretemplatesFile;
	}



	public void setCoretemplatesFile(File coretemplatesFile) {
		this.coretemplatesFile = coretemplatesFile;
	}

}
