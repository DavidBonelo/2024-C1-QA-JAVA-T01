package com.davidbonelo.persistance;

import com.davidbonelo.models.User;
import com.davidbonelo.models.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT * FROM Users";
        ArrayList<User> users = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs =
                statement.executeQuery()) {

            while (rs.next()) {
                UserRole role = UserRole.valueOf(rs.getString("role"));
                User user = new User(rs.getInt("id"), rs.getString("name"), rs.getString("email")
                        , role);
                users.add(user);
            }
            return users;
        }
    }

    public void createUser(User user, String password) throws SQLException {
        String sql = "INSERT INTO Users (name, email, password, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, password);
            statement.setString(4, user.getRole().getValue());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted == 0) {
                throw new SQLException("Creating user failed, no rows affected");
            }
        }
    }

    public void updateUser(User user) throws SQLException {
        if (!containsId(user)) {
            throw new IllegalArgumentException("Cant update a User without an id");
        }
        String sql = "UPDATE Users SET name= ?, email= ?, role= ? WHERE id=" + user.getId();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getRole().getValue());

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new SQLException("Couldn't update user with id: " + user.getId());
            }
        }
    }
    public void deleteUser(User userId) throws SQLException {
        String sql = "DELETE FROM Users WHERE id=" + userId;
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            int deletedRows = statement.executeUpdate();
            if (deletedRows == 0) {
                throw new SQLException("User with id " + userId + " not found, can't delete");
            }
        }
    }

    private boolean containsId(User user) {
        return user.getId() != 0;
    }

}
