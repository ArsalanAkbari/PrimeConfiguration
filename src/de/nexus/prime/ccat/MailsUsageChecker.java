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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This Class check whether the Mails are used in Process XML files or not ,if not it adds the name of mail into the " mailsDontExistList " and then into the "warningsList"
 * @author Arsalan Akbarioroumieh
 *
 */
public class MailsUsageChecker {

	private File mailsFile;
	private File processFile;
	private String mailName;
	private List mailsNotUsedList = new ArrayList();
	private List mailsDontExistList  = new ArrayList();
	private List allMailsList  = new ArrayList();

	/**
	 * The Constructor of the Class takes file-Path and warningsList as input and checks whether the warnings (Mails that not Exist) are available then adds them in to the warningsList
	 * @param filePath The Path of available Config.
	 * @param warningsList  List of all Warnings
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public MailsUsageChecker(String filePath , List warningsList) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

		FilePath filePathInstance = new FilePath(filePath);

		setMailsFile( filePathInstance.getMailFiles());
		setProcessFile( filePathInstance.getPrecessFiles());

		searchInMailsFile();
		addToWarningsList(warningsList);
	}

	/**
	 * This function goes through all mail XML Files and find the name of the File , then it adds the name into the "mailsNotUsedList" and "allMailsList" , and call the other function. The second "for" loop 
	 * checks that every Mail-Name be entered  only once in the list.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	
	private void searchInMailsFile() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		for (File file : getMailsFile().listFiles()) {

			if(!file.isFile()) {           
				continue;
			}

			DocumentBuilder mailBuilder = factory.newDocumentBuilder();
			Document mailDoc = mailBuilder.parse(file.getAbsolutePath());

			setMailName (mailDoc.getDocumentElement().getAttribute("name"));

			mailsNotUsedList.add(getMailName());
			allMailsList.add(getMailName());

			searchInProcessFiles(getProcessFile() ,getMailName());
		}

		for (int i=0 ;i <allMailsList.size(); i++) {

			for (int j = 0; j < mailsDontExistList.size(); j++){

				if(mailsDontExistList.get(j).equals(allMailsList.get(i)) ){

					mailsDontExistList.removeAll(Collections.singleton(allMailsList.get(i)));
				}
			}
		}
	}

	/**
	 * This function goes through all Process/Mappings XMl files ,  First it finds all "mappedTask" Elements from type 'MAIL_TASK' , then for each Elemnt it finds the attribute "refKey" , then it compare 
	 * that with the Parameter "mailName" , and if they are equal then it removes the mailName from "mailsNotUsedList"
	 * @param mailName The Mail Xml File Name from previous function 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private void searchInProcessFiles(File processFile ,String mailName ) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xPath = xPathFactory.newXPath();

		for (File file: processFile.listFiles()) {
			if(!file.isFile()) {           
				continue;
			}

			if (file.isDirectory()) {
				searchInProcessFiles(file, mailName);
			}

			if(file.isFile() && file.getName().endsWith(".xml")){
				DocumentBuilder processFileBuilder = factory.newDocumentBuilder();
				Document processDoc = processFileBuilder.parse(file.getAbsolutePath());

				NodeList nodeList = (NodeList) xPath.evaluate("//mappedTask[@type ='MAIL_TASK']" ,processDoc , XPathConstants.NODESET);

				for (int i = 0; i < nodeList.getLength(); i++) {
					Element element = (Element) nodeList.item(i);

					String refKeyInProcess = element.getAttribute("refKey");

					mailsDontExistList.add(refKeyInProcess);

					if(refKeyInProcess.equals(mailName)) {
						mailsNotUsedList.remove(mailName);
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
		if(!mailsDontExistList.isEmpty()) {

			warningsList.add("###################### mail_dont Exist_InMails_List ######################"+ "\n");

			for (int i = 0; i < mailsDontExistList.size(); i++) {

				warningsList.add(mailsDontExistList.get(i));
			}
		}
	}


	public String getMailName() {
		return mailName;
	}
	public void setMailName(String mailName) {
		this.mailName = mailName;
	}

	public File getMailsFile() {
		return mailsFile;
	}

	public void setMailsFile(File mailsFile) {
		this.mailsFile = mailsFile;
	}

	public File getProcessFile() {
		return processFile;
	}

	public void setProcessFile(File processFile) {
		this.processFile = processFile;
	}

}
