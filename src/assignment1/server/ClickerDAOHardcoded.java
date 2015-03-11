package assignment1.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClickerDAOHardcoded extends ClickerDAO {
	
	Map<String, Integer[]> userStorage = new HashMap<String, Integer[]>();
	
	
	public ClickerDAOHardcoded(Integer choiceCount) {
		super(choiceCount);
		
		String[] users = {
				"cse1",
				"cse2",
				"cse3",
				"cse4"
		};
		
		for (int i = 0; i < users.length; i++) {
			Integer[] arr = new Integer[choiceCount];
			Arrays.fill(arr,new Integer(0));
			userStorage.put(users[i], arr);
		}
	}

	boolean userExists(String username) {
		return userStorage.containsKey(username);
	}

	boolean incrementChoice(String username, int choice)
			throws IllegalArgumentException {
		if (!userExists(username)) {
			throw new IllegalArgumentException("User doesn't exist");
		}
		if (choice > choiceCount || choice < 1) {
			throw new IllegalArgumentException("Choice must be between 1 and " + choiceCount);
		}
		
		synchronized(this) {
			Integer[] userScores = userStorage.get(username);
			userScores[choice-1]++;
			userStorage.put(username, userScores);
		}
		
		return true;
	}

	Map<String, Integer[]> getAnswers() {
		return userStorage;
	}

}
