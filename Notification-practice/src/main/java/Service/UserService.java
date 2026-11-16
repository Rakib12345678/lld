package Service;

import model.User;

import java.util.HashMap;
public class UserService {

    private final HashMap<String, User> users;

    private static final UserService INSTANCE = new UserService();

    private UserService() {
        users = new HashMap<>();
    }

    public static UserService getInstance() {
        return INSTANCE;
    }

    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    public User getUser(String id) {
        if (!users.containsKey(id)) {
            throw new RuntimeException("User not found: " + id);
        }

        return users.get(id);
    }
}