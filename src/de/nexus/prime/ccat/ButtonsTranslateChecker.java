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
 * This Class check whether the buttons Translations are available or not if not then it adds the name of the button in without-Germany-Translation-List or without-English-TranslationList , and 
 * all of this in WarningList .
 * @author Arsalan Akbarioroumieh
 *
 */

public class ButtonsTranslateChecker {
	
	private File processFiles;
	private String processName ="";
	private String buttonName = "";
	private boolean counter = false;
	private List buttonList = new ArrayList();                     //Creating a List to save the name of Buttons
	private List withoutGermanyTranslationList = new ArrayList();  //Creating a List to save the name of Button that NOT Translated in Germany
	private List withoutEnglishTranslationList = new ArrayList();  //Creating a List to save the name of Button that NOT Translated in English

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
	
	/**
	 * The Constructor of the Class takes file-Path and warningsList as input and checks whether the warnings(Buttons without Translation) are available then adds them in to the warningsList
	 * @param filePath The Path of available Config
	 * @param warnings List List of all Warnings
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public ButtonsTranslateChecker(String filePath, List warningsList) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		
		setProcessFiles (new FilePath(filePath).getPrecessFiles()); 
		
		searchInProcessFiles(filePath);

		addToWarningsList(warningsList);
		
	}

	
	/**
	 * This Function search all the XML files in Process/Mappings directory ,
	 * it goes through all the XML file : First it get all "mappedTask" elements that have attribute "Type" and Type contains "USER_TASK" ,
	 * Then it Checks if the User Task hat "taskButton" or not , If YES ,Then it get all the taskButtons Elements and save them in "buttonlist",
	 * at the end it Finds the Name of all Buttons and calls the other Function to check the Translation.
	 * @param filePath  The Path of available Config
	 * @param processFiles All XML files in Process/Mapping directory
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */

	private void searchInProcessFiles(String filePath ) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		for (File file : getProcessFiles().listFiles()) {  
			
			if(!file.isFile()) {           
				continue;
			}

			DocumentBuilder processBuilder = factory.newDocumentBuilder();
			Document processDoc = processBuilder.parse(file.getAbsolutePath());

			XPath xPath = XPathFactory.newInstance().newXPath();
			
			NodeList elementList = (NodeList) xPath.evaluate("//mappedTask[contains(@type , 'USER_TASK')]", processDoc, XPathConstants.NODESET);   

			for (int i = 0; i < elementList.getLength(); i++) {

				Element userTaskElements = (Element) elementList.item(i);

				NodeList buttonlist = userTaskElements.getElementsByTagName("taskButton");

				if (buttonlist.getLength() > 0) {  //Find the Name of Process if the UserTask hat Buttons

					setProcessName(processDoc.getDocumentElement().getAttribute("processName"));
				}
				
				for (int j = 0; j < buttonlist.getLength(); j++) {    //get all Button Name in userTask

					Element taskButtonElements = (Element) buttonlist.item(j);

					setButtonName(taskButtonElements.getAttribute("text"));

					searchForGermanTranslate(filePath ,counter );

					if (!buttonList.contains(buttonName)) {
						buttonList.add(buttonName);
					}


					searchForEnglishTranslate(filePath ,counter );

					if (!buttonList.contains(buttonName)) {
						buttonList.add(buttonName);
					}
				}
			}
		}
	}
	
	

	/**
	 * This Function goes through all the CSV file ,line to line, and finds the translation of The Buttons-Name in Germany ,and
	 * if the translation is not available , then it adds the name of the Button in the without_Germany_Translation_List.
	 * @param filePath The Path of available Config
	 * @param counter It is used just as checker
	 * @throws IOException
	 */
	private void searchForGermanTranslate(String filePath , boolean counter ) throws IOException	{

		BufferedReader reader = new BufferedReader(new FileReader(filePath + "\\translations\\dictionary\\de.csv"));  

		String line = "";
		StringTokenizer st = null;

		while ((line = reader.readLine()) != null) {   
			if (line.length() < 1) {
				// empty line as part of translation. need to skip it.
				continue;
			}

			st = new StringTokenizer(line, ";");

			if (st.hasMoreTokens() && st.nextToken().equals(buttonName) ) {
				counter = true;
			}
		}

		reader.close();

		if (counter == false && !withoutGermanyTranslationList.contains(buttonName)) {
			withoutGermanyTranslationList.add(buttonName);
		}
		counter = false;
	}
	
	
    /**
     * This Function goes through all the CSV file ,line to line, and finds the translation of The Buttons-Name in English ,and
	 * if the translation is not available , then it adds the name of the Button in the without_English_Translation_List.
     * @param filePath filePath The Path of available Config
     * @param counter It is used just as checker
     * @throws IOException
     */
	private void searchForEnglishTranslate(String filePath , boolean counter ) throws IOException{

		BufferedReader reader = new BufferedReader(new FileReader(filePath.toString() + "\\translations\\dictionary\\en.csv"));   

		String line = "";
		StringTokenizer st = null;

		while ((line = reader.readLine()) != null) {   
			if (line.length() < 1) {
				continue;
			}

			st = new StringTokenizer(line, ";");

			if (st.hasMoreTokens()) {

				if (st.nextToken().equals(buttonName)) {
					counter = true;
				}
			}
		}

		reader.close();

		if (counter == false && !withoutEnglishTranslationList.contains(buttonName) ) {
			withoutEnglishTranslationList.add(buttonName);
		}
		counter = false;
	}
	
	
	/**
	* This function adds available warnings to the WarningList
	* @param warningsList
	*/
	private void addToWarningsList(List warningsList) {
		if(!withoutGermanyTranslationList.isEmpty()) {
			warningsList.add("###################### List of all Buttons that have no German Translation ######################"+"\n");
			for (int i = 0; i < withoutGermanyTranslationList.size(); i++) {
				warningsList.add(withoutGermanyTranslationList.get(i));
			}
		}

		if(!withoutEnglishTranslationList.isEmpty()) {
			warningsList.add("###################### List of all Buttons that have no English Translation  ######################"+"\n");
			for (int i = 0; i < withoutGermanyTranslationList.size(); i++) {
				warningsList.add(withoutGermanyTranslationList.get(i));
			}
		}
	}


	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public String getButtonName() {
		return buttonName;
	}
	public void setButtonName(String buttonName) {
		this.buttonName = buttonName;
	}
	public File getProcessFiles() {
		return processFiles;
	}
	public void setProcessFiles(File processFiles) {
		this.processFiles = processFiles;
	}
}




