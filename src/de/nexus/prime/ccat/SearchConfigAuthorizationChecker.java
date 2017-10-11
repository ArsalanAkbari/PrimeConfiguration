package de.nexus.prime.ccat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
 * This Class check whether the searchConfigs are used in permissions XML files or not ,if yes , then check whether the role for this permission available ist or not , 
 * if not it adds the name of searchconfig file into the into the "warningsList"
 * @author Arsalan Akbarioroumieh
 *
 */
public class SearchConfigAuthorizationChecker {


	private File searchConfigsFile;
	private File permissionsFile;
	private File rolesFile;
	private List searchConfigNotInPermissionList =  new ArrayList();
	private String searchConfigFileName;

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	/**
	 * The Constructor of the Class  takes file-Path and warningsList as input and calls "searchInSearchConfigFiles" function , and checks 
	 * whether the warnings are available then adds them in to the warningsList.
	 * @param filePath The Path of available Config
	 * @param warningsList List of all Warnings
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public SearchConfigAuthorizationChecker(String filePath ,List warningsList) throws XPathExpressionException, ParserConfigurationException, IOException  {
		
		try {

			FilePath filePathInstance = new FilePath(filePath);

			setSearchConfigsFile( filePathInstance.getSearchConfigFiles());
			setPermissionsFile(new File (filePathInstance.getPermissionFiles() +"\\searchconfig"));
			setRolesFile(filePathInstance.getRoleFiles());

			searchInSearchConfigFiles ();

			addToWarningsList(warningsList);

		}catch(SAXException e){

			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			System.out.println(sw.toString());

			System.out.println( e.getStackTrace().toString());
		}
	}


	/**
	 * This function goes through all searchConfig Files and then for each file finds the name of searchConfigs file , then it adds the name in to the "searchConfigNotInPermissionList" ,
	 * and calls the  "searchInPermissionsFile" function
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void searchInSearchConfigFiles() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

		for (File file : getSearchConfigsFile().listFiles()) {  

			if(!file.isFile()) {           
				continue;
			}

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document searchConfigDoc = builder.parse(file.getAbsolutePath());

			setSearchConfigFileName(searchConfigDoc.getDocumentElement().getAttribute("name"));

			searchConfigNotInPermissionList.add(getSearchConfigFileName());

			searchInPermissionsFile( getSearchConfigFileName());
		}
	}


	/**
	 * This function goes through all permission Files and then for each file finds the Element "objectPermissions" and for each elemnt finds the name of "id" attribute ,
	 * and checks whether the "id" is the same as "searchConfigFileName" ,if yes  then it finds all "permissionEntry" elements and for each Element calls searchInRolesFile function.
	 * @param searchConfigFileName The name of available searchConfig file from previous function
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */

	private void searchInPermissionsFile( String searchConfigFileName) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xPath = xPathFactory.newXPath();

		for (File file : getPermissionsFile().listFiles()) {  

			if(!file.isFile()) {           
				continue;
			}

			DocumentBuilder permissionBuilder = factory.newDocumentBuilder();
			Document permissionDoc = permissionBuilder.parse(file.getAbsolutePath());

			NodeList elementObjectPermissionslist = (NodeList) xPath.evaluate("//objectPermissions", permissionDoc, XPathConstants.NODESET); 

			for (int i = 0; i < elementObjectPermissionslist.getLength(); i++) { 

				Element objectPermissionsElement = (Element) elementObjectPermissionslist.item(i);

				if (searchConfigFileName.equals(objectPermissionsElement.getAttribute("id"))) {

					NodeList elementPermissionEntrylist = (NodeList) xPath.evaluate("//permissionEntry", permissionDoc, XPathConstants.NODESET); 

					for (int j = 0; j < elementPermissionEntrylist.getLength(); j++) {

						Element permissionEntryElement = (Element) elementPermissionEntrylist.item(i);

						searchInRolesFile(permissionEntryElement);
					}
				}
			}
		}
	}

	/**
	 * This function goes through all role Files and then for each file checks whether the name of the file is the same as permissionEntryElements attribute (sid) or not,
	 * if yes then it removes the searchConfigFileName from "searchConfigNotInPermissionList".
	 * @param rolesFile
	 * @param searchConfigFileName
	 * @param permissionEntryElement
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void searchInRolesFile( Element permissionEntryElement) throws ParserConfigurationException, SAXException, IOException {

		for (File file : getRolesFile().listFiles()) {   
			
			if(!file.isFile()) {           
				continue;
			}

			DocumentBuilder roleBuilder = factory.newDocumentBuilder();
			Document roleDoc = roleBuilder.parse(file.getAbsolutePath());

			if (permissionEntryElement.getAttribute("sid").equals(roleDoc.getDocumentElement().getAttribute("name"))) {

				searchConfigNotInPermissionList.remove(getSearchConfigFileName());
			}
		}
	}

	
	/**
	 * This function adds available warnings to the WarningList
	 * @param warningsList
	 */
	
	private void addToWarningsList(List warningsList) {
		if( !searchConfigNotInPermissionList.isEmpty()) {

			warningsList.add("###################### The SearchConfig , not been authorized  ######################");

			for (int i = 0; i < searchConfigNotInPermissionList.size(); i++) {

				warningsList.add(searchConfigNotInPermissionList.get(i));
			}
		}
	}
	
	public String getSearchConfigFileName() {
		return searchConfigFileName;
	}

	public void setSearchConfigFileName(String searchConfigFileName) {
		this.searchConfigFileName = searchConfigFileName;
	}


	public File getSearchConfigsFile() {
		return searchConfigsFile;
	}


	public void setSearchConfigsFile(File searchConfigsFile) {
		this.searchConfigsFile = searchConfigsFile;
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
