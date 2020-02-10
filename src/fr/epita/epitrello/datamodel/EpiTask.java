package fr.epita.epitrello.datamodel;

public class EpiTask {
	public String listName;
	public String taskName;
	public Integer estimatedTime;
	public Integer priority;
	public String description;
	public String user;
	public boolean isCompleted;
	
	public EpiTask(String listName, String taskName, Integer estimatedTime, Integer priority, String description) {
		setListName(listName);
		setTaskName(taskName);
		setEstimatedTime(estimatedTime);
		setPriority(priority);
		setDescription(description);
		setUser("Unassigned");
		
	}
	
	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Integer getEstimatedTime() {
		return estimatedTime;
	}

	public void setEstimatedTime(Integer estimatedTime) {
		this.estimatedTime = estimatedTime;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}

	public boolean getIsCompleted() {
		return isCompleted;
	}

	public void setIsCompleted(Boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

}
