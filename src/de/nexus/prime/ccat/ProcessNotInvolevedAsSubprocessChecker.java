
package de.nexus.prime.ccat;

import java.io.File;
import java.io.IOException;
import java.lang.NullPointerException;
import java.util.ArrayList;

import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.Node;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * This Class check whether are there any processName that are not in coretemplate files, or not in coretemplate/process files , or not as subprocesses (processes / *.bpmn -> calledElement) or not 
 * in processes / mappings / *. xml -> param1 at type = "MULTI_PROCESSING_TASK" .If such processes exist that not used in any of those Files ,then they are apparently not used , so a WARNING is appropriate.
 * @author Arsalan Akbarioroumieh
 *
 */
public class ProcessNotInvolevedAsSubprocessChecker {

	private File processMappingsFiles;      	 // Process/Mappings XML files
	private File processFiles;             	 // Process .bpmn Files
	private File coretemplateFiles;        	 // Xml Files in Coretemplate directory
	private File coretemplateProcessesFiles;	 //Coretemplate/Process xml Files
	private List inCoretemplateProcessList = new ArrayList(); 
	private List inCoretemplateList = new ArrayList();   		 
	private List inBpmnList = new ArrayList();	         
	private List notAsSubprocessesList = new ArrayList();
	private String processMappingsName;

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	/**
	 * The Constructor of the Class  takes file-Path and warningsList as input and calls function "searchInProcessMappingFiles" ,
	 * and checks whether the warnings are available then adds them in to the warningsList.
	 * @param filePath The Path of available Config
	 * @param warningsList List of all Warnings
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
	public ProcessNotInvolevedAsSubprocessChecker(String filePath , List warningsList) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

		 setProcessFiles (new File(filePath+"\\processes"));
		 setProcessMappingsFiles ( new FilePath(filePath).getPrecessFiles());
		 setCoretemplateFiles ( new FilePath(filePath).getCoretemplateFiles());
		 setCoretemplateProcessesFiles ( new File( filePath+ "\\coretemplates\\processes"));

		findProcessMappingFileName();

		addToWarningsList(warningsList);
	}

	/**
	 * This function goes through all processMapping Files and then for each file it adds the "ProcessMappingsName" to the "notAsSubprocessesList" band calls the  "searchInCoretemplateFiles" function. thrn
	 * it Check whether the ProcessMappingsName is in "Coretemplate file" or in "Coretemplate/Process files" or in "Process Bpmn" Files. 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private void findProcessMappingFileName()throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		for (File file : getProcessMappingsFiles().listFiles()) {  
			
			if(!file.isFile()) {           
				continue;
			}
			DocumentBuilder processMappingsBuilder = factory.newDocumentBuilder();
			Document processMappingsDoc = processMappingsBuilder.parse(file.getAbsolutePath());

			setProcessMappingsName(processMappingsDoc.getDocumentElement().getAttribute("definitionKey"));    

			notAsSubprocessesList.add(getProcessMappingsName());                                              

			searchInCoretemplateFiles() ; 

			if(!inCoretemplateList.contains(processMappingsName)) {										 

				searchInCoretemplateProcessFiles();

				if(!inCoretemplateProcessList.contains(getProcessMappingsName())) {													

					searchInProcessBpmnFiles();     
				}  
			}
		}
	}


	/**
	 * This function goes through all coretemplate Files and checks whether the name of Coretemplate file is the same as ProcessMappingsName or not , if yes it adds the name to the 
	 * inCoretemplateList and remove it from notAsSubprocessesList
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private void searchInCoretemplateFiles () throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		for (File file : getCoretemplateFiles().listFiles()) { 
			
			if(!file.isFile()) {           
				continue;
			}
			
			if(file.isFile()) {

				DocumentBuilder coretemplateBuilder = factory.newDocumentBuilder();
				Document coretemplateDoc = coretemplateBuilder.parse(file.getAbsolutePath());

				String coretemplateName = coretemplateDoc.getDocumentElement().getAttribute("newProcessDefinitionKey"); 

				if( coretemplateName.equals(getProcessMappingsName())){

					if(!inCoretemplateList.contains(getProcessMappingsName())) {

						inCoretemplateList.add(getProcessMappingsName());
					}
					notAsSubprocessesList.remove(getProcessMappingsName());
				}
			}
		}
	}


	/**
	 * This function goes through all coretemplateProcesses Files and find all the Elements that has processDefinitionKey attributes ,then checks whether the processDefinitionKey is the same az ProcessMappingsName
	 * or not , if yes , then it adds it in to inCoretemplateProcessList , remove from notAsSubprocessesList.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private void searchInCoretemplateProcessFiles () throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		XPath xPath = XPathFactory.newInstance().newXPath();

		for (File file : getCoretemplateProcessesFiles().listFiles()) {
			
			if(!file.isFile()) {           
				continue;
			}

			DocumentBuilder coretemplateBuilder = factory.newDocumentBuilder();
			Document coretemplateDoc = coretemplateBuilder.parse(file.getAbsolutePath());

			NodeList coretemplateNodeList = (NodeList) xPath.evaluate("//*[@processDefinitionKey] ", coretemplateDoc, XPathConstants.NODESET);

			if( coretemplateNodeList.getLength() >0) {

				for(int i=0 ; i< coretemplateNodeList.getLength() ; i++) {

					Element element = (Element) coretemplateNodeList.item(i);

					if( getProcessMappingsName().equals(element.getAttribute("processDefinitionKey"))){

						if(!inCoretemplateProcessList.contains(getProcessMappingsName())) {

							inCoretemplateProcessList.add(getProcessMappingsName());
						}
						notAsSubprocessesList.remove(getProcessMappingsName());
					}
				}
			}
		}
	}

	/**
	 * This function goes through all process(bpmn) Files and for each file finds the name of .Bpmn file (arrtibut id) , and finds all callActivity Elements , then
	 * it checks whether attribute "calledElement" is the same as "${processToCall}" and the processId is the same as ProcessMappingsName. If yes then it calls searchInProcessMappingsFile
	 * else if attribut "calledElement" is the same as ProcessMappingsName ,it adds it in to the "inBpmnList" and remove it from notAsSubprocessesList.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private void searchInProcessBpmnFiles () throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		XPath xPath = XPathFactory.newInstance().newXPath();

		for (File file : getProcessFiles().listFiles()) {
			
			if(!file.isFile()) {           
				continue;
			}

			if (file.isFile() && file.getName().endsWith(".bpmn")) {

				DocumentBuilder processBuilder = factory.newDocumentBuilder();
				Document processDoc = processBuilder.parse(file.getAbsolutePath());

				NodeList processNode = (NodeList) xPath.evaluate("/definitions/process[1]", processDoc, XPathConstants.NODESET);
				Element element = (Element) processNode.item(0);
				String processId = element.getAttribute("id");

				NodeList callActivityList = (NodeList) xPath.evaluate("//callActivity", processDoc, XPathConstants.NODESET); 

				for (int i = 0; i < callActivityList.getLength(); i++) {

					Element callActivity = (Element) callActivityList.item(i); 

					if (callActivity.getAttribute("calledElement").equals("${processToCall}") && getProcessMappingsName() == processId) {

						searchInProcessMappingsFile(file);
					}

					else {

						if (callActivity.getAttribute("calledElement").equals(getProcessMappingsName())) {

							if (!inBpmnList.contains(getProcessMappingsName())) {

								inBpmnList.add(getProcessMappingsName());
							}
							notAsSubprocessesList.remove(getProcessMappingsName());
						}
					}
				}
			}
		}
	}

	/**
	 * This function goes through all process/Mapping Files and finds all Elements that has attribute type ='MULTI_PROCESSING_TASK' ,then it find the name of The Element that has this attribute ,and in this 
     * Element finds the attribute "param1". If the "param1" is the same as ProcessMappingsName , the it adds ProcessMappingsName in to the "inBpmnList" and remove it from "notAsSubprocessesList".
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private void searchInProcessMappingsFile( File processBpmnFile )throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		XPath xPath = XPathFactory.newInstance().newXPath();

		for (File file : getProcessMappingsFiles().listFiles()) {
			
			if(!file.isFile()) {           
				continue;
			}

			DocumentBuilder processMappingBuilder = factory.newDocumentBuilder();
			Document processMappingDoc = processMappingBuilder.parse(processBpmnFile.getAbsolutePath());

			NodeList processNodeList = (NodeList) xPath.evaluate("//*[@type ='MULTI_PROCESSING_TASK']", processMappingDoc, XPathConstants.NODESET);

			if (processNodeList.getLength() > 0) {

				for (int i = 0; i < processNodeList.getLength(); i++) {

					Element mappedTaskElement = (Element) processNodeList.item(i);

					if (mappedTaskElement.getAttribute("param1").equals(getProcessMappingsName())) {

						if (!inBpmnList.contains(getProcessMappingsName())) {

							inBpmnList.add(getProcessMappingsName());
						}

						notAsSubprocessesList.remove(getProcessMappingsName());
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
		if(notAsSubprocessesList.size()>0) {

			warningsList.add("###################### processes_are Not_involved_asSubprocessest ######################" + "\n");

			for (int i = 0; i < notAsSubprocessesList.size(); i++) {

				warningsList.add(notAsSubprocessesList.get(i));
			}
		}
	}


	public String getProcessMappingsName() {
		return processMappingsName;
	}


	public void setProcessMappingsName(String processName) {
		this.processMappingsName = processName;
	}




	public File getProcessMappingsFiles() {
		return processMappingsFiles;
	}




	public void setProcessMappingsFiles(File processMappingsFiles) {
		this.processMappingsFiles = processMappingsFiles;
	}




	public File getProcessFiles() {
		return processFiles;
	}




	public void setProcessFiles(File processFiles) {
		this.processFiles = processFiles;
	}




	public File getCoretemplateFiles() {
		return coretemplateFiles;
	}




	public void setCoretemplateFiles(File coretemplateFiles) {
		this.coretemplateFiles = coretemplateFiles;
	}




	public File getCoretemplateProcessesFiles() {
		return coretemplateProcessesFiles;
	}




	public void setCoretemplateProcessesFiles(File coretemplateProcessesFiles) {
		this.coretemplateProcessesFiles = coretemplateProcessesFiles;
	}



}















