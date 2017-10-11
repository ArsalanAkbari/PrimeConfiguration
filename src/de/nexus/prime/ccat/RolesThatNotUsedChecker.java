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
 * This Class checks whether the role files were used in permissions files or not, if not it adds it in to the warnings list.
 * @author Arsalan Akbarioroumieh
 *
 */
public class RolesThatNotUsedChecker {

	private File rolesFile;
	private File permissionsFile;
	private List rolesNotUsedList = new ArrayList<>();
	private String roleFileName;

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
	/**
	 * The Constructor of the Class  takes file-Path and warningsList as input and calls "searchInRolesFile" function , and checks 
	 * whether the warnings (The role that not used) are available then adds them in to the warningsList.
	 * @param filePath The Path of available Config
	 * @param warningsList  List of all Warnings
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	
	public RolesThatNotUsedChecker(String filePath , List warningsList) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

		FilePath filePathInstance = new FilePath(filePath);

		setRolesFile( filePathInstance.getRoleFiles());
		setPermissionsFile ( new File( filePathInstance.getPermissionFiles() +"\\processes"));

		searchInRolesFile();

		addToWarningsList(warningsList);
	}
	
	/**
	 * This function goes through all role Files and then for each file finds the name of role file , then it adds the name in to the rolesNotUsedList ,and 
	 * calls the  "searchInCoretemplatesFile" function
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */

	private void searchInRolesFile() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

		for (File file : getRolesFile().listFiles()) {

			if(!file.isFile()) {           
				continue;
			}

			DocumentBuilder roleBuilder = factory.newDocumentBuilder();
			Document roleDoc = roleBuilder.parse(file.getAbsolutePath());

			setRoleFileName (roleDoc.getDocumentElement().getAttribute("name"));

			rolesNotUsedList.add(getRoleFileName());

			searchInPermissionsFile(getPermissionsFile(), getRoleFileName());
		}
	}

	/**
	 * This function goes through all permission Files and then for each file finds the Element "permissionEntry" with attribute "sid"
	 * ,then it checks whether the attribute"sid" is the same as roleFileName or not , if yes then it removes it from rolesNotUsedList.
	 * @param permissionsFile All permission XML files.
	 * @param roleFileName The name of role XML file from previous function.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private void searchInPermissionsFile(File permissionsFile, String roleFileName) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xPath = xPathFactory.newXPath();

		for (File file : permissionsFile.listFiles()) {

			if (file.isDirectory()) {
				searchInPermissionsFile(file, roleFileName);
			}

			if (file.isFile() && file.getName().endsWith(".xml")) {

				DocumentBuilder dirBuilder = factory.newDocumentBuilder();
				Document dirDoc = dirBuilder.parse(file);

				NodeList nodeList = (NodeList) xPath.evaluate("//permissionEntry [@sid]", dirDoc, XPathConstants.NODESET);

				for (int i = 0; i < nodeList.getLength(); i++) {

					Element elementChild = (Element) nodeList.item(i);

					if (elementChild.getAttribute("sid").equals(roleFileName)) {

						rolesNotUsedList.remove(roleFileName);
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
		if(!rolesNotUsedList.isEmpty()) {

			warningsList.add("###################### Roles that not authorized ######################" + "\n"); 

			for (int i = 0; i < rolesNotUsedList.size(); i++) {

				warningsList.add(rolesNotUsedList.get(i));
			}
		}
	}
	
	public String getRoleFileName() {
		return roleFileName;
	}

	public void setRoleFileName(String roleFileName) {
		this.roleFileName = roleFileName;
	}

	public File getRolesFile() {
		return rolesFile;
	}

	public void setRolesFile(File rolesFile) {
		this.rolesFile = rolesFile;
	}

	public File getPermissionsFile() {
		return permissionsFile;
	}

	public void setPermissionsFile(File permissionsFile) {
		this.permissionsFile = permissionsFile;
	}

}
