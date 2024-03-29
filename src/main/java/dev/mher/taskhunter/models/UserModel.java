package dev.mher.taskhunter.models;

import dev.mher.taskhunter.utils.DataSourceUtils;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;

/**
 * User: MheR
 * Date: 12/2/19.
 * Time: 7:03 PM.
 * Project: taskhunter.
 * Package: dev.mher.taskhunter.models.
 */
@Component
@Getter
@Setter
public class UserModel {

    private static final Logger logger = LoggerFactory.getLogger(UserModel.class);

    private DataSource dataSource;

    private int userId;
    private String email;
    private CharSequence password;
    private String firstName;
    private String lastName;
    private String createdAt;
    private String updatedAt;
    private String confirmationToken;
    private Timestamp confirmationSentAt;
    private String confirmedAt;
    private boolean isActive;

    public UserModel() {
    }

    @Autowired
    public UserModel(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    public boolean signUp() {
        String queryString = "INSERT INTO users (email, password, confirmation_token, confirmation_sent_at, first_name, last_name) VALUES (?, ?, ?, ?, ?, ?);";

        boolean isOk = false;


        Connection con = null;
        PreparedStatement pst = null;
        try {

            con = dataSource.getConnection();
            pst = con.prepareStatement(queryString);
            pst.setString(1, getEmail());
            pst.setString(2, getPassword().toString());
            pst.setString(3, getConfirmationToken());
            pst.setTimestamp(4, getConfirmationSentAt());
            pst.setString(5, getFirstName());
            pst.setString(6, getLastName());

            pst.execute();
            isOk = true;
        } catch (SQLException e) {
            logger.info(e.getMessage());
            logger.error(e.getMessage(), e);
        } finally {
            DataSourceUtils.closeConnection(con, pst, null);
        }

        return isOk;
    }

    public boolean userConfirm(Timestamp interval) {
        String queryString = "UPDATE users SET is_active=?, confirmation_token=NULL WHERE confirmation_token=? AND confirmation_sent_at > ?;";

        Connection con = null;
        PreparedStatement pst = null;

        boolean isUpdated = false;

        try {
            con = dataSource.getConnection();
            pst = con.prepareStatement(queryString);
            pst.setBoolean(1, isActive());
            pst.setString(2, getConfirmationToken());
            pst.setTimestamp(3, interval);
            isUpdated = pst.executeUpdate() != 0;
        } catch (SQLException e) {
            logger.info(e.getMessage());
            logger.error(e.getMessage(), e);
        } finally {
            DataSourceUtils.closeConnection(con, pst, null);
        }
        return isUpdated;
    }

    public UserModel findByEmail(String email) {

        String queryString = "SELECT user_id AS \"userId\",\n" +
                "       email AS \"email\",\n" +
                "       password AS \"password\",\n" +
                "       created_at AS \"createdAt\",\n" +
                "       first_name AS \"firstName\",\n" +
                "       last_name AS \"lastName\",\n" +
                "       is_active AS \"isActive\"\n" +
                "FROM users\n" +
                "WHERE email=? AND is_active=?\n" +
                "LIMIT 1;";

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();

            pst = con.prepareStatement(queryString);
            pst.setString(1, email);
            pst.setBoolean(2, true);

            rs = pst.executeQuery();
            if (rs != null && rs.next()) {

                UserModel user = new UserModel();
                user.setUserId(rs.getInt("userId"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setFirstName(rs.getString("firstName"));
                user.setLastName(rs.getString("lastName"));
                user.setActive(rs.getBoolean("isActive"));

                return user;
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            logger.error(e.getMessage(), e);
        } finally {
            DataSourceUtils.closeConnection(con, pst, rs);
        }
        return null;
    }
}
