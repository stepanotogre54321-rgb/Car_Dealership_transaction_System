package chbcars.data;

import chbcars.model.User;

public class UserRepository extends Repository<User> {

    public UserRepository() {
        super("users.txt", "USR");
    }

    @Override
    protected User parse(String[] fields) {
        return User.fromFields(fields);
    }

    public User findByUsername(String username) {
        for (User u : getAll()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }
}
