package fr.epita.epitrello.services;

import fr.epita.epitrello.datamodel.*;
import fr.epita.epitrello.services.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EpitrelloDataService {
	/**
	 *  The ArrayList that will hold all Epitrello lists 
	 */
	static List<EpiList> allLists = new ArrayList<EpiList>();
	
	/** 
	 * The ArrayList that will hold all Epitrello tasks 
	 */
	static List<EpiTask> allTasks = new ArrayList<EpiTask>();

	
	/**
	 * Define Configuration values
	 */
	private static final String DATA_FILE = Configuration.getValueFromKey("data.file");

	/** 
	 * Connection 
	 */
	public EpitrelloDataService() throws SQLException {
		Connection connection = getConnection();
		try {
			connection.prepareStatement(
					"CREATE TABLE USERS(ID INT AUTO_INCREMENT PRIMARY KEY, USERNAME VARCHAR(500))")
			.execute();
		} catch (SQLException e) {
			// e.printStackTrace();
		}
	}

	
	/** 
	 * Creator
	 * The Creator returns a new EpitrelloDataService instance
	 * This is called initially in the main to be later on uses to call other functions
	 */
	public static EpitrelloDataService creator() throws SQLException {
		return new EpitrelloDataService();
	}


	/** 
	 * Get Connection 
	 * The connection is being brought using the URL, User and Password
	 * These information are brought from the H2 console.
	 * The H2 jar file has been imported in the libraries as well so that the driver recognizes the service.
	 */
	public static Connection getConnection() throws SQLException {
		// Url:
		String url = "jdbc:h2:~/test";

		// User:
		String user = "sa";

		// Password:
		String password = "";

		Connection connection = DriverManager.getConnection(url, user, password);
		return connection;
	}


	/** 
	 * Add User 
	 * It takes a username as a required parameter
	 * With the H2 driver working, it is a requirement to store the User in the DB
	 * The connection should be called prior to executing any query, in this case to check or insert a new User
	 * the Prepared Statement is giving us the ability to hold the values returned by the query and to execute any query with values injected
	 * It is mandatory to check if the User already Exists, if the User Exists, "the User already Exists" is returned
	 * Otherwise, return "Success" after the User is being stored in the DB
	 */
	public String addUser(String username) throws SQLException {
		PreparedStatement preparedStatement;
		Connection connection = getConnection();

		preparedStatement = connection
				.prepareStatement("SELECT USERNAME FROM USERS WHERE USERNAME = ?");
		preparedStatement.setString(1, username);
		ResultSet rs = preparedStatement.executeQuery();
		boolean temp = rs.next();
		//if temporary variable is equal to true, then the user has been found
		//checking if the user has been found
		if(temp) {
			writeToFile("the User already exists");
			return "the User already exists";
		}
		else {
			try {
				// the ? is considered as an injection variable
				preparedStatement = connection
						.prepareStatement("INSERT INTO USERS (USERNAME) VALUES (?)");
				//1 is the first ?, which injects the username in the query
				preparedStatement.setString(1, username);
				preparedStatement.executeUpdate();
				
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			writeToFile("Success");
			return "Success";
		}
	}


	/** 
	 * Add List:
	 * It takes a listName as a parameter
	 * Using the allLists list, we need to check first if the name given already exists
	 * If it exists, return that the listName + "already exists"
	 * Otherwise, add it in the list and return "Success"
	 */
	public String addList(String listName) {
		//checking if the allLists class is not empty to proceed
		if(!allLists.isEmpty()) {
			for (int i=0; i<allLists.size(); i++) {
				if(allLists.get(i).listName.equals(listName)) {
					writeToFile(listName + " already exists");
					return(listName + " already exists");
				}
			}
		}

		allLists.add(new EpiList(listName));
		writeToFile("Success");
		return "Success";
	}


	/** 
	 * Add Task: 
	 * It takes a listName, taskName, estimatedTime, priority and description as a parameters
	 * Using the allTasks list, we need to check first if the values given already exist by comparing taskNames
	 * If it exists, return that the taskName + "already exists"
	 * Then, make sure that the name of the list given exists, if it does, then return Success after adding the elements in the list.
	 */
	public String addTask(String listName, String taskName, Integer estimatedTime, Integer priority, String description) {	
		//checking if the allTasks class is not empty to proceed
		if(!allTasks.isEmpty()) {
			for (int i=0; i<allTasks.size(); i++) {
				if(allTasks.get(i).taskName.equals(taskName)) {
					writeToFile(taskName + " does not exist");
					return(taskName + " already exist");
				}
			}
		}
		//checking if the allLists class is not empty to proceed
		if(!allLists.isEmpty()) {
			for (int i=0; i<allLists.size(); i++) {
				if(allLists.get(i).listName.equals(listName)) {
					allTasks.add(new EpiTask(listName, taskName, estimatedTime, priority, description));
					writeToFile("Success");
					return "Success";
				}
			}
		}
		writeToFile("");
		return null;
	}


	/** 
	 * Edit Task 
	 * It takes a taskName, estimatedTime, priority and description as parameters
	 * The task should be found in the allTasks using the taskName to modify the rest of the information
	 * if the task has been found, "Success" statement is return after modifying the task information
	 * Otherwise, return that the task does not exist
	 */
	public String editTask(String taskName, Integer estimatedTime, Integer priority, String description) {	
		if(!allTasks.isEmpty()) {
			for (int i=0; i<allTasks.size(); i++) {
				if(allTasks.get(i).getTaskName().equals(taskName)) {
					allTasks.get(i).setEstimatedTime(estimatedTime);
					allTasks.get(i).setPriority(priority);
					allTasks.get(i).setDescription(description);
					writeToFile("Success");
					return "Success";
				}
			}
			writeToFile(taskName + " does not exist");
			return(taskName + " does not exist");
		}
		writeToFile("");
		return null;
	}


	/** 
	 * Assign Task 
	 * It takes a taskName and a username as parameters
	 * To Assign a task, you need to check if the user already exists in the DB
	 * If the temp variable returns false, then return that the user does not exist
	 * Otherwise, check if the task exists
	 * If the task exists, then return Success after assigning the task to the user
	 * Otherwise, return that the task does not exist
	 */
	public String assignTask(String taskName, String username) throws SQLException {
		PreparedStatement preparedStatement;
		Connection connection = getConnection();

		preparedStatement = connection.prepareStatement("SELECT USERNAME FROM USERS WHERE USERNAME = ?");
		preparedStatement.setString(1, username);
		ResultSet rs = preparedStatement.executeQuery();
		boolean temp = rs.next();

		if (!temp) {
			writeToFile( "The user does not exist");
			return "The user doesn't exist";
		} else {
			if (!allTasks.isEmpty()) {
				for (int i = 0; i < allTasks.size(); i++) {
					if (allTasks.get(i).getTaskName().equals(taskName)) {
						allTasks.get(i).setUser(username);
						writeToFile("Success");
						return "Success";
					}
				}
				writeToFile(taskName + " does not exist");
				return (taskName + " does not exist");
			}
			writeToFile("Success");
			return "Success";
		}
	}


	/** 
	 * Move Task 
	 * It takes the task's name and the list's name as parameters
	 * first, the presence of the task in the allTasks list should be found
	 * if it is not found, then return that the task does not exist and exit the function
	 * if it is found, check for the presence of the list in allLists
	 * if the list is not found, return that the list does not exist
	 * otherwise, modify the list's name in the task 
	 */
	public String moveTask(String task, String list) {
		if (!allTasks.isEmpty()) {
			for (int i = 0; i < allTasks.size(); i++) {
				if (allTasks.get(i).getTaskName().equals(task)) {

					if (!allLists.isEmpty()) {
						for (int j = 0; j < allLists.size(); j++) {
							if (allLists.get(j).listName.equals(list)) {
								allTasks.get(i).setListName(list);
								writeToFile("Success");
								return "Success";
							}
						}
					}
					writeToFile(list + " does not exist");
					return list +" does not exist"; 

				}
			}
			writeToFile(task + " does not exist");
			return task + " does not exist";
		}
		writeToFile("Success");
		return "Success";
	}


	/** 
	 * Print Task 
	 * It takes the taskName as parameter
	 * First, it has to find the task after making sure that allTasks is not empty
	 * If the task is found, append into a string the information that are present with this task and return it (name, description, priority etc...)
	 * Otherwise, return that the task does not exist
	 * 
	 */
	public String printTask(String taskName) {
		String response = "";

		if(!allTasks.isEmpty()) {
			for (int i=0; i<allTasks.size(); i++) {
				if(allTasks.get(i).getTaskName().equals(taskName)) {
					//append into the response all the information related to the task
					response += allTasks.get(i).getTaskName() +"\n"+
							allTasks.get(i).getDescription() +"\n"+
							"Priority: "+ allTasks.get(i).getPriority() +"\n"+
							"Estimated Time: " + allTasks.get(i).getEstimatedTime() +"\n";

					if(!allTasks.get(i).getUser().equals("Unassigned")) {
						response += "Assigned to " +allTasks.get(i).getUser();
					}
					else {
						// this value contains Unassigned
						response += allTasks.get(i).getUser();
					}

					response += "\n";
				}
			}
			writeToFile(response);
			return response;
		}
		writeToFile(taskName + " does not exist");
		return taskName + " does not exist";
	}


	/** 
	 * Delete Task
	 * It takes taskName as parameter
	 * first, we must check whether the task exists in allTasks after checking that it is not empty
	 * if it is found, remove the task from the list
	 * otherwise, return that it does not exist
	 */
	public String deleteTask(String taskName) {
		if(!allTasks.isEmpty()) {
			for (int i=0; i<allTasks.size(); i++) {
				if(allTasks.get(i).getTaskName().equals(taskName)) {
					allTasks.remove(i);
					writeToFile("Success");
					return "Success";
				}
			}			
		}
		writeToFile(taskName + " does not exist");
		return(taskName + " does not exist");
	}


	/** 
	 * Complete Task 
	 * It takes task as parameter
	 * first, we must check whether the task exists in allTasks after checking that it is not empty
	 * if it is found, modify the completion to true
	 * otherwise, return that it does not exist
	 */
	public String completeTask(String task) {
		if (!allTasks.isEmpty()) {
			for (int i = 0; i < allTasks.size(); i++) {
				if (allTasks.get(i).getTaskName().equals(task)) {
					allTasks.get(i).setIsCompleted(true);
					
					writeToFile("Success");
					return ("Success");
				}
			}			
		}
		writeToFile(task + " does not exist");
		return (task + " does not exist");
	}


	/** 
	 * Delete List
	 * It takes listName as parameter
	 * first, we must check whether the list exists in allLists after checking that it is not empty
	 * if it is found, remove the list from the list
	 * otherwise, return that it does not exist
	 * also, all tasks that contain the listName should also be removed from allTasks
	 * if each related to that list is found after making sure that allTasks is not empty, remove it
	 * otherwise, return that neither the list nor the task were found.
	 */
	public static String deleteList(String listName) {
		if(!allLists.isEmpty()) {
			for (int i=0; i<allLists.size(); i++) {
				if(allLists.get(i).getListName().equals(listName)) {
					allLists.remove(i);
					
					if(!allTasks.isEmpty()) {
						for (int j=0; i<allTasks.size(); j++) {
							if(allTasks.get(j).getListName().equals(listName)) {
								allTasks.remove(i);					
							}
						}
						writeToFile("Success");
						return "Success";
					}
				}
			}
			writeToFile(listName + " does not exist");
			return(listName + " does not exist");
		}

		
		writeToFile("neither lists or tasks were found");
		return "neither lists or tasks were found";
	}


	/** 
	 * Print List 
	 * It takes listName as parameter
	 * if the list is not found, return that the list is not found.
	 * once the list name is found after checking that the allLists is not empty
	 * append the list's name into a string
	 * then, check for every task that is contained in the listName after checking that allTasks is not empty
	 * once a task is found, append into the same string all the information related to that task
	 * the procedure is done until no task with that listName is found
	 * the response is being returned
	 */
	public String printList(String listName) {
		String response = "";

		if(!allLists.isEmpty()) {
			for (int i=0; i<allLists.size(); i++) {
				if(allLists.get(i).listName.equals(listName)) {
					response += "List " + allLists.get(i).getListName() + "\n";

					if(!allTasks.isEmpty()) {
						for (int j=0; j<allTasks.size(); j++) {
							if(allTasks.get(j).getListName().equals(listName)) {	
								response += allTasks.get(j).getPriority() + " | ";
								response += allTasks.get(j).getTaskName() + " | ";
								response += allTasks.get(j).getUser() + " | ";
								response += allTasks.get(j).getEstimatedTime()  + "h";
								response += "\n";								
							}
						}
					}
				}
				
			}
			writeToFile(response);
			return response;			
		}
		writeToFile(listName + " does not exist");
		return(listName + " does not exist");		
	}


	/** 
	 * Print All Lists
	 * this function checks whether allLists is empty if allLists is empty
	 * return that the list is empty
	 * if it is not empty, append into a string each listName present in the allLists list
	 * return the response
	 */
	public String printAllLists() {
		String response = "";

		if(!allLists.isEmpty()) {
			for (int i=0; i<allLists.size(); i++) {
				String l = printList(allLists.get(i).getListName());	
				response += l + "\n";
			}
			writeToFile(response);
			return response;
		}
		writeToFile("the list is empty, therefore there's nothing to print");
		return "the list is empty, therefore there's nothing to print";
	}


	/** 
	 * Print User Tasks 
	 * It takes username as parameter
	 * to print this specific user's tasks, we must check whether the user exists in the DB or not
	 * in case temp is false, return that the user does not exit
	 * otherwise, check for every task whether it is assigned to this user after checking that allTasks is not empty
	 * once a task is found, append into a string all the information related to that task
	 * the procedure keeps on going until no tasks are found related to that user
	 */
	public String printUserTasks(String username) throws SQLException {
		PreparedStatement preparedStatement;
		Connection connection = getConnection();

		preparedStatement = connection.prepareStatement("SELECT USERNAME FROM USERS WHERE USERNAME = ?");
		preparedStatement.setString(1, username);
		ResultSet rs = preparedStatement.executeQuery();
		boolean temp = rs.next();

		String response = "";

		if (!temp) {
			writeToFile("The user does not exist");
			return "The user does not exist";
		} 
		else {
			if (!allTasks.isEmpty()) {
				for (int i = 0; i < allTasks.size(); i++) {
					if (allTasks.get(i).user != null && allTasks.get(i).getUser().equals(username)) {
						response += allTasks.get(i).getPriority() + " | ";
						response += allTasks.get(i).getTaskName() + " | ";
						response += allTasks.get(i).getUser() + " | ";
						response += allTasks.get(i).getEstimatedTime() + "h \n";						
					}
				}
			}
		}
		writeToFile(response);
		return response;
	}


	/** 
	 * Print Unassigned Tasks By Priority
	 * this function creates a list called unassignedTaskByPriority
	 * this list will be used to collect every task that is unassigned
	 * to check it, we much check every task in the allTasks list after checking it is not empty
	 * once we add every task that is unassigned, we sort the unassginedTaskbyPriority by using the Collections.sort function
	 * the Collections.sort function will sort a list according to a specific element, in this case the priority
	 * once the list is being sorted, every task is printed by appending each task's information in a string
	 * then, the response is being returned
	 */
	public String printUnassignedTasksByPriority(){
		String response = "";
		List<EpiTask> unassignedTaskByPriority = new ArrayList<EpiTask>();

		if(!allTasks.isEmpty()) {
			for (int i=0; i<allTasks.size(); i++) {
				if(allTasks.get(i).getUser().equals("Unassigned")) {					
					unassignedTaskByPriority.add(allTasks.get(i));	
				}
			}			
		}

		Collections.sort(unassignedTaskByPriority, new Comparator<EpiTask>() {
			public int compare(EpiTask e1, EpiTask e2) {
				return Integer.valueOf(e2.getPriority()).compareTo(e1.getPriority());
			}			   
		});

		for (int j=0; j<unassignedTaskByPriority.size(); j++) {
			response += unassignedTaskByPriority.get(j).getPriority() + " | ";
			response += unassignedTaskByPriority.get(j).getTaskName() + " | ";
			response += unassignedTaskByPriority.get(j).getUser() + " | ";
			response += unassignedTaskByPriority.get(j).getEstimatedTime()  + "h";
			response += "\n";								
		}
		writeToFile(response);
		return response;
	}


	/** 
	 * Print Unfinished Tasks By Priority 
	 * * this function creates a list called unfinishedTasksByPriority
	 * this list will be used to collect every task that is not completed
	 * to check it, we much check every task in the allTasks list after checking it is not empty
	 * once we add every task that is not completed, we sort the unfinishedTasksByPriority by using the Collections.sort function
	 * the Collections.sort function will sort a list according to a specific element, in this case the priority
	 * once the list is being sorted, every task is printed by appending each task's information in a string
	 * then, the response is being returned
	 */
	public String printAllUnfinishedTasksByPriority(){
		String response = "";

		List<EpiTask> unfinishedTasksByPriority = new ArrayList<EpiTask>();

		if(!allTasks.isEmpty()) {
			for (int i=0; i<allTasks.size(); i++) {
				if(allTasks.get(i).getIsCompleted() == false) {					
					unfinishedTasksByPriority.add(allTasks.get(i));	
				}
			}			
		}

		Collections.sort(unfinishedTasksByPriority, new Comparator<EpiTask>() {
			public int compare(EpiTask e1, EpiTask e2) {
				return Integer.valueOf(e1.getPriority()).compareTo(e2.getPriority());
			}			   
		});

		for (int j=0; j<unfinishedTasksByPriority.size(); j++) {
			response += unfinishedTasksByPriority.get(j).getPriority() + " | ";
			response += unfinishedTasksByPriority.get(j).getTaskName() + " | ";
			response += unfinishedTasksByPriority.get(j).getUser() + " | ";
			response += unfinishedTasksByPriority.get(j).getEstimatedTime()  + "h";
			response += "\n";								
		}
		writeToFile(response);
		return response;
	}


	/** 
	 * Print Users By Workload
	 * this function created a HashMap, which is the most adequate data structure to use to access keys
	 * first, we must retrieve the users from the DB
	 * then, we must verify the presence of these users in the allTasks list after checking that it was not empty
	 * once we compare the presence of every user, we add the workload by getting the estimatedTime for each task in a variable
	 * then, we store the information with the username and workload in the hashmap
	 * we call a function called sortByValueDesc(map) to sort the map
	 * once the map is sorted, we append the sorted users in a string
	 * we return the response
	 */
	public String printUsersByWorkload() throws SQLException{
		String response = "";

		HashMap<String, Integer> map = new HashMap<String, Integer>();

		PreparedStatement preparedStatement;
		Connection connection = getConnection();

		preparedStatement = connection.prepareStatement("SELECT * FROM USERS");
		ResultSet rs = preparedStatement.executeQuery();

		while (rs.next()) {
			String username = rs.getString("username");

			if (!allTasks.isEmpty()) {
				Integer workload = 0;
				for (int i = 0; i < allTasks.size(); i++) {
					if (allTasks.get(i).getUser().equals(username)) {
						workload += allTasks.get(i).getEstimatedTime();
					}
					map.put(username, workload);
				}
			}
		}
		// calling the function softByValueDesc from the SortingMaps class
		Map<String,Integer> sortedMap = SortingMaps.sortByValueDesc(map);
		
		for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
			response += entry.getKey() + "\n";
		}
		writeToFile(response);
		return response;
	}


	/** 
	 * Print Users By Performance:
	 * this function created a HashMap, which is the most adequate data structure to use to access keys
	 * first, we must retrieve the users from the DB
	 * then, we must verify the presence of these users in the allTasks list after checking that it was not empty
	 * once we compare the presence of every user, we add the performance by checking the completed tasks and getting the estimated time
	 * in a variable
	 * then, we store the information with the username and performance in the hashmap
	 * we call a function called sortByValueDesc(map) to sort the map
	 * once the map is sorted, we append the sorted users in a string
	 * we return the response
	 */
	public String printUsersByPerformance() throws SQLException {
		String response = "";

		HashMap<String, Integer> map = new HashMap<String, Integer>();

		PreparedStatement preparedStatement;
		Connection connection = getConnection();

		preparedStatement = connection.prepareStatement("SELECT * FROM USERS");
		ResultSet rs = preparedStatement.executeQuery();

		while (rs.next()) {
			String username = rs.getString("username");

			if (!allTasks.isEmpty()) {
				Integer performance = 0;
				for (int i = 0; i < allTasks.size(); i++) {
					if (allTasks.get(i).getUser().equals(username) && allTasks.get(i).getIsCompleted() == true) {
						performance += allTasks.get(i).getEstimatedTime();
					}
					map.put(username, performance);
				}
			}
		}
		
		// calling the function softByValueDesc from the SortingMaps class
		Map<String,Integer> sortedMap = SortingMaps.sortByValueAsc(map);
		
		

		for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
			response += entry.getKey() + "\n";
		}
		writeToFile(response);
		return response;		
	}

	/** 
	 * Write To File 
	 */
	public static void writeToFile(String response){
		File file = new File(DATA_FILE);

		PrintWriter writer;
		try {
			FileOutputStream out = new FileOutputStream(file, true);
			writer = new PrintWriter(out);
			writer.println(response);
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}