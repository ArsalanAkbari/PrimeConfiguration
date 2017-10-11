package de.nexus.prime.ccat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
 * This Class check whether the UserTasks Translations are available or not if not then it adds the name of the UserTask in userTaskNotTranslatedList , and in WarningList .
 * @author Arsalan Akbarioroumieh
 *
 */
public class UserTasksTranslateChekcer {


	private File processFiles;
	private List userTaskNotTranslatedList = new ArrayList(); 
	private String userTaskFileName;

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
	/**
	 * The Constructor of the Class takes file-Path and errorsList as input and checks whether the errors(UserTasks without Translation) are available then adds them in to the errorsList
	 * @param filePath The Path of available Config.
	 * @param errorsList List List of all errors.
	 * @throws XPathExpressionException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	
	public  UserTasksTranslateChekcer(String filePath ,List errorsList) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {

		FilePath filePathInstance = new FilePath(filePath);
		setProcessFiles ( filePathInstance.getPrecessFiles());

		searchInProcessFiles(filePath);

		addToWarningsList(errorsList);
	}

	

	/**
	 *This Function search all the XML files in Process/Mappings directory ,it goes through all the XML file : First it get all "mappedTask" elements
	 * that have attribute "Type" and Type contains "USER_TASK" , and for each element find the attribute "name" and calls searchForGermanTranslation function.
	 * @param filePath
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
	private void searchInProcessFiles(String filePath) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {

		for (File file : getProcessFiles().listFiles()) {
			if(!file.isFile()) {           
				continue;
			}

			DocumentBuilder precessBuilder = factory.newDocumentBuilder();
			Document processDoc = precessBuilder.parse(file.getAbsolutePath());

			XPath xPath = XPathFactory.newInstance().newXPath();

			NodeList elementList = (NodeList) xPath.evaluate("//mappedTask[contains(@type , 'USER_TASK')]", processDoc, XPathConstants.NODESET); 

			for (int i = 0; i < elementList.getLength(); i++) {

				Element element = (Element) elementList.item(i);

				setUserTaskFileName(element.getAttribute("name")); 

				searchForGermanTranslation(filePath , getUserTaskFileName());
			}
		}
	}

	/**
	 * This Function goes through all the CSV file ,line to line, and finds the translation of The UserTask-Name ,and
	 * if the translation is not available , then it adds the name of the UserTask in to the userTaskNotTranslatedList.
	 * @param filePath
	 * @param userTaskFileName
	 * @throws IOException
	 */

	private void searchForGermanTranslation(String filePath, String userTaskFileName) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(filePath.toString()+"\\translations\\dictionary\\de.csv")); 
		String line = "";
		StringTokenizer st = null;

		while ((line = reader.readLine()) != null) {

			st = new StringTokenizer(line, ";");

			while (st.hasMoreTokens()) {
				if (st.nextToken().equals(userTaskFileName)) {
					userTaskFileName = null;
				}
			}
		}

		if (userTaskFileName != null) {
			userTaskNotTranslatedList.add(userTaskFileName);
		}
	}
	
	
	/**
	 * This function adds available warnings to the WarningList
	 * @param warningsList
	 */
	
	private void addToWarningsList(List errorsList) {
		if (!userTaskNotTranslatedList.isEmpty()) {

			errorsList.add("######################(ERROR) User_Tasks without any Translation (ERROR)######################" +"\n");

			for (int i = 0; i < userTaskNotTranslatedList.size(); i++) {

				errorsList.add(userTaskNotTranslatedList.get(i));

			}
		}
	}


	public String getUserTaskFileName() {
		return userTaskFileName;
	}

	public void setUserTaskFileName(String userTaskFileName) {
		this.userTaskFileName = userTaskFileName;
	}



	public File getProcessFiles() {
		return processFiles;
	}



	public void setProcessFiles(File processFiles) {
		this.processFiles = processFiles;
	}
}