package de.nexus.prime.ccat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

/**
 * The CCAT Program checks all the Config files and finds the Errors and Warnings
 * @author Arsalan Akbarioroumieh
 *
 */
public class Main {

	public static void main(String[] args) {

		List warningsList = new ArrayList<>();
		List errorsList = new ArrayList<>();

		try {

			String unpackedConfigDirectoryPath = getConfigDirectoryPath();	
			callClasses(warningsList, errorsList, unpackedConfigDirectoryPath);
			toString(warningsList, errorsList);

		}
		catch (IOException ex) {
			ex.printStackTrace();
		} 
		catch (Exception e) {
			e.getMessage();
		}
	}


	/**
	 * This function search for unpacked Config Directory Path in Project . First it get the current Applications path , then it gets the parent Directory (Project parent Directory) and 
	 * after that it searches for directory with the name (".._projectconfig") and assigns the path to the unpackedConfigDirectoryPath.
	 * @return
	 */
	private static String getConfigDirectoryPath() {

		final String dir = System.getProperty("user.dir");
		File applicationDirectoryPath =new File(dir);
		File parentDir = applicationDirectoryPath.getParentFile();
		String projectConfigDirectoryPath = "" ;
		String unpackedConfigDirectoryPath = "";

		for (File file : parentDir.listFiles()) {
			if (file.getName().contains("_projectconfig")){
				unpackedConfigDirectoryPath=   file.getPath();
			}
		}
		unpackedConfigDirectoryPath += "\\config\\unpackedConfig";
		return unpackedConfigDirectoryPath;
	}


	/**
	 * This Function call each class and find the warnings and Errors.
	 * @param warningsList List of all Warnings
	 * @param errorsList  List of all Errors
	 * @param unpackedConfigDirectoryPath
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */

	private static void callClasses(List warningsList, List errorsList, String unpackedConfigDirectoryPath) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

		new DatapoolsUsageChecker(unpackedConfigDirectoryPath, warningsList);
		new FormsUsageChecker(unpackedConfigDirectoryPath, warningsList);
		new ButtonsTranslateChecker(unpackedConfigDirectoryPath, warningsList);
		new MailsUsageChecker(unpackedConfigDirectoryPath, warningsList);
		new ProcessAuthorizationChecker(unpackedConfigDirectoryPath, warningsList);
		new ProcessNotInvolevedAsSubprocessChecker(unpackedConfigDirectoryPath, warningsList);
		new ProcessUsageInCoretemplateChecker(unpackedConfigDirectoryPath, warningsList);
		new RolesThatNotUsedChecker(unpackedConfigDirectoryPath, warningsList); 
		new SearchConfigAuthorizationChecker(unpackedConfigDirectoryPath ,warningsList);
		new UserTasksAutorizationChecker(unpackedConfigDirectoryPath, errorsList);
		new UserTasksTranslateChekcer(unpackedConfigDirectoryPath , errorsList);
	}



	/**
	 * This function is the projects Output. It checks the warningsList and errorsList and print the warnings and Errors in console output.
	 * @param warningsList List of all Warnings
	 * @param errorsList List of all Errors
	 */
	private static void toString(List warningsList, List errorsList) {

		if(!warningsList.isEmpty()) {
			System.out.println("!!!!!!!!!!!!!!!!!! WARNINGS !!!!!!!!!!!!!!!!!!" +"\n\n");
			for (int i = 0; i < warningsList.size(); i++) {
				System.out.println(warningsList.get(i));
			}
		}

		if(!errorsList.isEmpty()) {
			System.out.println("\n\n" +"!!!!!!!!!!!!!!!!!! ERRORS !!!!!!!!!!!!!!!!!!" +"\n\n");
			for (int i = 0; i < errorsList.size(); i++) {
				System.out.println(errorsList.get(i));
			}
			//System.exit(1);
		}

		if(warningsList.isEmpty() && errorsList.isEmpty()) {
			System.out.println("No Erros and no Warnings");
		}
		//System.exit(0);
	}
}
