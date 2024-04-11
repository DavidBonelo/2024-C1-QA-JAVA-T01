package com.davidbonelo.persistance;

import com.davidbonelo.models.Book;
import com.davidbonelo.models.LibraryItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    private final Connection connection;

    public BookDAO(Connection connection) {
        this.connection = connection;
    }

    protected static Book buildBookFromResult(ResultSet rs) throws SQLException {
        return new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"),
                rs.getInt("copies"), rs.getInt("copies_borrowed"), rs.getString("field"),
                rs.getInt("pages"));
    }

    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM Books";
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs =
                statement.executeQuery()) {
            while (rs.next()) {
                books.add(buildBookFromResult(rs));
            }
        }
        return books;
    }

    public void createBook(Book book) throws SQLException {
        String sql = "INSERT INTO Books ( title, author, copies, copies_borrowed, field, " +
                "pages ) VALUES ( ?, ?, ?, ?, ?, ? )";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setInt(3, book.getCopies());
            statement.setInt(4, book.getCopiesBorrowed());
            statement.setString(5, book.getField());
            statement.setInt(6, book.getPages());
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted == 0) {
                throw new SQLException("Creating book failed, no rows affected.");
            }
        }
    }

    public void updateBook(Book book) throws SQLException {
        if (!containsId(book))
            throw new IllegalArgumentException("Cant update a Book without an id");
        String sql = "UPDATE Books b SET title= ?, author= ?, copies= ?, copies_borrowed= ?, " +
                "field= ?, pages= ? WHERE b.id=" + book.getId();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setInt(3, book.getCopies());
            statement.setInt(4, book.getCopiesBorrowed());
            statement.setString(5, book.getField());
            statement.setInt(6, book.getPages());
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("Update of book with id " + book.getId() + " failed.");
            }
        }
    }

    public void deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM Books b WHERE b.id=" + bookId;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted == 0) {
                throw new SQLException("Book deletion failed, no rows affected");
            }
        }
    }

    private boolean containsId(LibraryItem item) {
        return item.getId() != 0; // just learned that int cant be null and defaults to 0
    }
}
