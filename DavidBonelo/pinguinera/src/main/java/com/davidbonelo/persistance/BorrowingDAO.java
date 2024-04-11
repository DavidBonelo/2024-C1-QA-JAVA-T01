package com.davidbonelo.persistance;

import com.davidbonelo.models.Borrowing;
import com.davidbonelo.models.LibraryItem;
import com.davidbonelo.models.User;
import com.davidbonelo.models.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.davidbonelo.persistance.BookDAO.buildBookFromResult;
import static com.davidbonelo.persistance.NovelDAO.buildNovelFromResult;

public class BorrowingDAO {
    Connection connection;

    public BorrowingDAO(Connection connection) {
        this.connection = connection;
    }

    private static Borrowing buildBorrowingFromResult(ResultSet rs) throws SQLException {
        UserRole role = UserRole.valueOf(rs.getString("role"));
        User user = new User(rs.getString("name"), rs.getString("email"), role);
        return new Borrowing(rs.getInt("id"), rs.getDate("return_date").toLocalDate(),
                rs.getDate("returned_date").toLocalDate(), user);
    }

    public Borrowing getBorrowingWithItems(int borrowingId) throws SQLException {
        String sql = "SELECT * FROM Borrowings b WHERE b.id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, borrowingId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                Borrowing borrowing = buildBorrowingFromResult(rs);
                borrowing.setBorrowedItems(getAllItemsForABorrowing(borrowing.getId()));
                return borrowing;
            } else {
                throw new SQLException("Borrowing with id " + borrowingId + " Not found");
            }
        }
    }

    public List<Borrowing> getAllBorrowings() throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        String sql = "SELECT b.*, u.name, u.email, u.role FROM Borrowings b LEFT JOIN Users u ON "
                + "b.user_id = u.id";
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs =
                statement.executeQuery()) {
            while (rs.next()) {
                borrowings.add(buildBorrowingFromResult(rs));
            }
        }
        return borrowings;
    }

    /**
     * Calling this is expensive, consider only fetching the items for one or a few Borrowings
     */
    public List<Borrowing> getAllBorrowingsAndItems() throws SQLException {
        List<Borrowing> borrowings = getAllBorrowings();
        borrowings.forEach(b -> b.setBorrowedItems(getAllItemsForABorrowing(b.getId())));
        return borrowings;
    }

    public List<LibraryItem> getAllItemsForABorrowing(int borrowingId) {
        List<LibraryItem> items = new ArrayList<>();
        // Note to self: Don't try to optimize by joining this 2 queries because then it will not
        // be possible to diferenciate Books and Novels
        // SubQuery a borrowing and join its books
        String sqlB = "SELECT b.* FROM (SELECT * FROM borrowings_books bb WHERE bb.borrowing_id " +
                "=" + " ?) AS bb LEFT JOIN Books b ON bb.book_id = b.id";
        // SubQuery a borrowing and join its novels
        String sqlN = "SELECT n.* FROM (SELECT * FROM borrowings_novels bn WHERE bn.borrowing_id "
                + "= ?) AS bn LEFT JOIN Novels n ON bb.novel_id = n.id";

        try (PreparedStatement statementB = connection.prepareStatement(sqlB); PreparedStatement statementN = connection.prepareStatement(sqlN)) {
            statementB.setInt(1, borrowingId);
            statementN.setInt(1, borrowingId);

            try (ResultSet rsB = statementB.executeQuery(); ResultSet rsN =
                    statementN.executeQuery()) {

                while (rsB.next()) {
                    items.add(buildBookFromResult(rsB));
                }
                while (rsN.next()) {
                    items.add(buildNovelFromResult(rsB));
                }
            }
        } catch (SQLException e) {
            return null;
        }
        return items;
    }

    public void updateBorrowingState(Borrowing borrowing) throws SQLException {
        if (missingId(borrowing)) {
            throw new IllegalArgumentException("Can't update a borrowing without a id");
        }
        String sql = "UPDATE Borrowing b SET status= ? WHERE id= ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, borrowing.getStatus().getValue());
            statement.setInt(2, borrowing.getId()); // WHERE

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new SQLException("Couldn't update borrowing with id " + borrowing.getId());
            }
        }
    }

    public void deleteBorrowing(Borrowing borrowing) throws SQLException {
        if (missingId(borrowing)) {
            throw new IllegalArgumentException("Can't delete a borrowing without a id");
        }
        String sql = "DELETE FROM Borrowing WHERE id= ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, borrowing.getId()); // WHERE

            int deletedRows = statement.executeUpdate();
            if (deletedRows == 0) {
                throw new SQLException("Couldn't delete borrowing with id " + borrowing.getId());
            }
        }
    }

    private boolean missingId(Borrowing borrowing) {
        return borrowing.getId() == 0;
    }
}
