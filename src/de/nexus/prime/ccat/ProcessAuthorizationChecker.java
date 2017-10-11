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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This Class checks whether the available process/Mappings file were used in Permission/Process Files files or not, 
 * If yes then it checks in the same Permission/Process file  , is that any "Role" for permissionEntry as attribute or not. if not ,
 * then it adds the Process file name in to the warning list.
 * @author Arsalan Akbarioroumieh
 *
 */
public class ProcessAuthorizationChecker {

	private File processFiles;
	private File permissionsFile ;
	private File rolesFile ;
	private List processNotAuthorizedList = new ArrayList();
	private String processFileName ="" ;

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	/**
	 * The Constructor of the Class  takes file-Path and warningsList as input and calls searchInProcessFile function, 
	 * then checks whether the warnings are available or not,if yes then adds them in to the warningsList
	 * @param filePath  The Path of available Config
	 * @param warningsList  List of all Warnings
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public ProcessAuthorizationChecker(String filePath , List warningsList) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		FilePath filePathInstance = new FilePath(filePath);

		setProcessFiles ( filePathInstance.getPrecessFiles());
		setPermissionsFile( new File( filePathInstance.getPermissionFiles() +"\\processes"));
		setRolesFile (filePathInstance.getRoleFiles());

		searchInProcessFile();
		addToWarningsList(warningsList);
	}



	/**
	 * This function goes through all process Files and then for each file it adds the "processName" to the "processNotAuthorizedList" and calls the  "searchInPermissionsFile" function.
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

			setProcessFileName(processDoc.getDocumentElement().getAttribute("processName"));
			processNotAuthorizedList.add(getProcessFileName());

			searchInPermissionsFile() ;
		}
	}


	/**
	 * This Function goes through all permission/process files ,and for each file it checks all "objectPermissions" Elements and if the attribute "id" is the same as "ProcessFileName" ,then if finds all
	 * children of objectPermissions Element and for each children (permissionEntry ) calls the searchInRolesFile function.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private void searchInPermissionsFile() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		XPath xPath = XPathFactory.newInstance().newXPath();

		for (File file : getPermissionsFile().listFiles()) { 
			if(!file.isFile()) {           
				continue;
			}

			DocumentBuilder permissionBuilder = factory.newDocumentBuilder();
			Document permissionDoc = permissionBuilder.parse(file.getAbsolutePath());

			NodeList permissionNodeList = (NodeList) xPath.evaluate("//objectPermissions", permissionDoc, XPathConstants.NODESET);

			for (int i = 0; i < permissionNodeList.getLength(); i++) {

				Element element = (Element) permissionNodeList.item(i);

				if (element.getAttribute("id").equals(getProcessFileName())) {
					NodeList childList = element.getChildNodes();  

					for (int j = 0; j < childList.getLength(); j++) {
						Node child = childList.item(j);

						if (child instanceof Element) {              //instanceof keyword is a binary operator used to test if an object (instance) is a subtype of a given Type.
							Element elementChild = (Element) child;
							searchInRolesFile( elementChild );

						}
					}
				}
			}
		}
	}


	/**
	 * This Function goes through all role files , it checks is there any roleFileName(name) that is the same az elementChild(sid) or not , if Yes then 
	 * it removes the ProcessFileName from processNotAuthorizedList
	 * @param elementChild The Name of Children of the objectPermissions Element.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void searchInRolesFile( Element elementChild) throws ParserConfigurationException, SAXException, IOException {


		for (File file : getRolesFile().listFiles()) { 
			if(!file.isFile()) {           
				continue;
			}

			DocumentBuilder rolesBuilder = factory.newDocumentBuilder();
			Document rolesDoc = rolesBuilder.parse(file.getAbsolutePath());

			if (rolesDoc.getDocumentElement().getAttribute("name").equals(elementChild.getAttribute("sid"))) {
				processNotAuthorizedList.remove(getProcessFileName());
			}
		}
	}


	/**
	 * This function adds available warnings to the WarningList
	 * @param warningsList
	 */

	private void addToWarningsList(List warningsList) {
		if(!processNotAuthorizedList.isEmpty()) {
			warningsList.add("###################### Processes that have not been authorized (Has no Role)  ######################" + "\n");

			for (int i = 0; i < processNotAuthorizedList.size(); i++) {
				warningsList.add(processNotAuthorizedList.get(i));
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



	public File getPermissionsFile() {
		return permissionsFile;
	}



	public void setPermissionsFile(File permissionsFile) {
		this.permissionsFile = permissionsFile;
	}



	public File getRolesFile() {
		return rolesFile;
	}



	public void setRolesFile(File rolesFile) {
		this.rolesFile = rolesFile;
	}
}