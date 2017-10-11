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
 * This Class checks whether the process/Mappings file were used in Permission/Process Files files or not, 
 * If yes then it checks in the same Permission/Process file  , is that any "Role" for permissionEntry as attribute or not. if not ,
 * then it adds the Process file name in to the warning list.
 * @author Arsalan Akbarioroumieh
 *
 */
public class UserTasksAutorizationChecker {

	private File processFiles;
	private File permissionsFile;
	private File rolesFile;
	private List userTaskNotAuthorizedList = new ArrayList();
	private String processFileName;
	private String  taskId;

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	/**
	 *The Constructor of the Class  takes file-Path and warningsList as input and calls searchInProcessFile function, 
	 * then checks whether the warnings are available or not,if yes then adds them in to the errorsList
	 * @param filePath The Path of available Config
	 * @param errorsList List of all errors
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public  UserTasksAutorizationChecker(String filePath ,List errorsList) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException{

		FilePath filePathInstance = new FilePath(filePath);

		setProcessFiles ( filePathInstance.getPrecessFiles());
		setPermissionsFile (new File( filePathInstance.getPermissionFiles() +"\\processes"));
		setRolesFile (filePathInstance.getRoleFiles());
		
		searchInProcessFiles();
		addToWarningsList(errorsList); 
	}


	

	/**
	 * This function goes through all process Files and then for each file finds the processFileName ,and finds all mappedTask Elements that has attribute @type and has 'USER_TASK'
	 * then for each elemnt finds the attribute "taskId" ,and adds the "taskId" in to the "userTaskNotAuthorizedList" and calls the  "searchInPermissionsFile" function.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private void searchInProcessFiles() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		XPath xPath = XPathFactory.newInstance().newXPath();

		for (File file : getProcessFiles().listFiles()) {  

			if(!file.isFile()) {           
				continue;
			}

			DocumentBuilder processBuilder = factory.newDocumentBuilder();
			Document processDoc = processBuilder.parse(file.getAbsolutePath());

			setProcessFileName( processDoc.getDocumentElement().getAttribute("definitionKey"));

			NodeList elementList = (NodeList) xPath.evaluate("//mappedTask[contains(@type , 'USER_TASK')]", processDoc, XPathConstants.NODESET);   //Finding all "mappedTask" Elements , that they have Attribute "Type ='user_task'" 

			for (int a = 0; a < elementList.getLength(); a++) {

				Element element = (Element) elementList.item(a);

				setTaskId(element.getAttribute("taskId"));

				userTaskNotAuthorizedList.add(taskId);

				searchInPermissionsFile(getPermissionsFile(), getTaskId());   //Searching in Permission File
			}
		}
	}

	/**
	 * This Function goes through all permission/process files ,and for each file it finds all "objectPermissions" Elements and the attribute "id" and
	 * checks whether the attribut "id" is the same as "taskId" ,if yes , then it finds all the children of the Element and calls "searchInRolesFile" function.
	 * @param permissionsFile
	 * @param rolesFile
	 * @param taskId
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */

	private void searchInPermissionsFile(File permissionsFile , String taskId) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		
		XPath xPath = XPathFactory.newInstance().newXPath();

		for (File file : permissionsFile.listFiles()) {

			if (file.isDirectory()) {

				searchInPermissionsFile(file, taskId);
			}

			if (file.isFile() && file.getName().endsWith(".xml")) {

				DocumentBuilder permissionBuilder = factory.newDocumentBuilder();
				Document permissionDoc = permissionBuilder.parse(file.getAbsolutePath());

				NodeList permissionList = (NodeList) xPath.evaluate("//objectPermissions[@id]", permissionDoc, XPathConstants.NODESET);

				for (int i = 0; i < permissionList.getLength(); i++) {

					if (((Element) permissionList.item(i)).getAttribute("id").equals(taskId)) {
						
						NodeList childList = permissionList.item(i).getChildNodes();

						for (int k = 0; k < childList.getLength(); k++) {

							Element elementPermission = (Element) permissionList.item(i);
							org.w3c.dom.Node child =  childList.item(k);

							if (child instanceof Element) {
								Element elementChild = (Element) child;

								searchInRolesFile(elementChild ,taskId );
							}
						}
					}
				}
			}
		}
	}

	/**
	 * This Function goes through all role files , it checks is there any roleFileName(name) that is the same as elementChild(sid) or not , if Yes then 
	 * it removes the taskId from userTaskNotAuthorizedList.
	 * @param rolesFile
	 * @param elementChild
	 * @param taskId
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */

	private void searchInRolesFile(Element elementChild, String taskId) throws ParserConfigurationException, SAXException, IOException {

		for (File file : getRolesFile().listFiles()) {

			if(!file.isFile()) {           
				continue;
			}

			DocumentBuilder roleBuilder = factory.newDocumentBuilder();
			Document roleDoc = roleBuilder.parse(file.getAbsolutePath());

			if (roleDoc.getDocumentElement().getAttribute("name").equals(elementChild.getAttribute("sid"))) {

				userTaskNotAuthorizedList.remove(taskId);
			}
		}
	}

	
	/**
	 * This function adds available warnings to the WarningList
	 * @param warningsList
	 */
	
	private void addToWarningsList(List errorsList) {
		if (!userTaskNotAuthorizedList.isEmpty()) {

			errorsList.add("######################(ERROR) userTask_Nicht_Authorized (ERROR)######################" + "\n");

			for (int i = 0; i < userTaskNotAuthorizedList.size(); i++) {

				errorsList.add(userTaskNotAuthorizedList.get(i));
			}
		}
	}


	public String getProcessFileName() {
		return processFileName;
	}


	public void setProcessFileName(String processFileName) {
		this.processFileName = processFileName;
	}


	public String getTaskId() {
		return taskId;
	}


	public void setTaskId(String taskId) {
		this.taskId = taskId;
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
