package de.nexus.prime.ccat;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This Class checks whether the available datapoolMappings file are be used in Coretemplate/Process files or not, if not ,
 * then it adds the datapoolMapping file name in to the warning list.
 * @author Arsalan Akbarioroumieh
 *
 */

public class DatapoolsUsageChecker {

	private File datapoolMappingFiles;
	private File coretemplateProcessFiles;
	private List notUsedDatapoolList = new ArrayList(); 
	private String datapoolMappingFileName ="";
	private String coretemplateMappingFileName="";

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	
	/**
	 * The Constructor of the Class  takes file-Path and warningsList as input and checks whether the warnings(The datapoolMapping that not used) are available then adds them in to the warningsList
	 * @param filePath filePath The Path of available Config
	 * @param warningsList List of all Warnings
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */

	public DatapoolsUsageChecker(String filePath , List warningsList)throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

		FilePath filePathInstance = new FilePath(filePath);  

		setDatapoolMappingFiles (filePathInstance.getDatapoolMappingFiles()); 
		setCoretemplateProcessFiles (new File(filePathInstance.getCoretemplateFiles().getPath()+"\\processes"));
		
		searchInDatapoolMappingsFile(filePath );
		addToWarrningList(warningsList);
	}
	

    /**
     * This function goes through all-dataPool-Mappings File and then for each file it adds the "DatapoolMappingFileName" to the "notUsedDatapoolList" and calls the  "searchInCoretemplatesFile" function.
     * @param filePath The Path of available Config
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException
     */
	private void searchInDatapoolMappingsFile(String filePath ) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{

		for (File file : getDatapoolMappingFiles().listFiles()) { 

			if(!file.isFile()) {           
				continue;
			}

			DocumentBuilder datapoolMappingBuilder = factory.newDocumentBuilder();
			Document datapoolMappingDoc = datapoolMappingBuilder.parse(file.getAbsolutePath());

			setDatapoolMappingFileName(datapoolMappingDoc.getDocumentElement().getAttribute("name"));

			notUsedDatapoolList.add(getDatapoolMappingFileName());

			searchInCoretemplatesFile( getDatapoolMappingFileName());
			
		}
	}


	/**
	 * This Function goes through all Coretemplate/process files ,for each file it checks all "coreTemplateProcess" Elements and if the attribute "dataPoolMappingName" is the same as "datapoolMappingFileName" 
	 * the it removes it from notUsedDatapoolList .
	 * @param datapoolMappingFileName The name of datapoolMapping File.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private void searchInCoretemplatesFile(String datapoolMappingFileName) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xPath = xPathFactory.newXPath();
 
		for (File file : getCoretemplateProcessFiles().listFiles()) { 

			if(!file.isFile()) {           
				continue;
			}

			DocumentBuilder coretemplateBuilder = factory.newDocumentBuilder();
			Document coretemplateDoc = coretemplateBuilder.parse(file.getAbsolutePath());

			setCoretemplateMappingFileName(coretemplateDoc.getDocumentElement().getAttribute("coreTemplate"));

			NodeList nodeList = (NodeList) xPath.evaluate("//coreTemplateProcess", coretemplateDoc, XPathConstants.NODESET);   //Finding all elements with the name : coreTemplateProcess 

			for (int i = 0; i < nodeList.getLength(); i++) {  //Going through all elements and finding an Attribute that has the same name as datapoolMapping_File_Name

				Element element = (Element) nodeList.item(i);

				if (element.getAttribute("dataPoolMappingName").equals(datapoolMappingFileName)) {

					notUsedDatapoolList.remove(datapoolMappingFileName);  
				}
			}
		}
	}
	
	
 
	/**
	 * This function adds available warnings to the WarningList
	 * @param warningsList
	 */
	private void addToWarrningList(List warningsList) {
		if (!notUsedDatapoolList.isEmpty()) {

			warningsList.add("###################### datapoolMappings that Not Used ######################" + "\n");

			for (int i = 0; i < notUsedDatapoolList.size(); i++) {

				warningsList.add(notUsedDatapoolList.get(i));
			}
		}
	}
	

	public String getDatapoolMappingFileName() {
		return datapoolMappingFileName;
	}

	public void setDatapoolMappingFileName(String datapoolMappingFileName) {
		this.datapoolMappingFileName = datapoolMappingFileName;
	}

	public String getCoretemplateMappingFileName() {
		return coretemplateMappingFileName;
	}

	public void setCoretemplateMappingFileName(String coretemplateMappingFileName) {
		this.coretemplateMappingFileName = coretemplateMappingFileName;
	}

	public File getDatapoolMappingFiles() {
		return datapoolMappingFiles;
	}

	public void setDatapoolMappingFiles(File datapoolMappingFiles) {
		this.datapoolMappingFiles = datapoolMappingFiles;
	}

	public File getCoretemplateProcessFiles() {
		return coretemplateProcessFiles;
	}

	public void setCoretemplateProcessFiles(File coretemplateProcessFiles) {
		this.coretemplateProcessFiles = coretemplateProcessFiles;
	}
}
