package mediaDatabase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 * Author: Jacqueline Kurniawan
 */
public class DBConnection {
    static Connection conn = null;
    ResultSet rs;

    /**
     * Establishes a connection to the MySQL database.
     * @return The Connection object representing the connection to the database
     * @throws SQLException
     */
    public static Connection connectDB() throws SQLException{
        // Establish connection to the MySQL database
        conn = DriverManager.getConnection("jdbc:mysql://localhost:8889/mediaDatabase", "root","root");
        System.out.println("Connection successful");
        // Return the Connection object
        return conn;
    }

    public DBConnection() {
    }

    /**
     * Adds Media object to the database
     * @param media
     * @throws SQLException
     */
    public void addBook(Media media) throws SQLException {
        // SQL Query to add media object into 'media' table in database
        String sql = "INSERT INTO media(image, title, creator, year, note) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        //converts byte array of media's image to a ByteArrayInputStream
        ByteArrayInputStream bais = new ByteArrayInputStream(media.getPicture());
        // Set the parameters of the SQL query with data from the 'Media' object
        preparedStatement.setBlob(1, bais);
        preparedStatement.setString(2, media.getTitle());
        preparedStatement.setString(3, media.getCreator());
        preparedStatement.setInt(4, media.getYearOfRelease());
        preparedStatement.setString(5, media.getNote());
        //add media to the database
        preparedStatement.execute();
    }

    /**
     * Retrieves detailed information about a media item from the database based on its ID.
     * This method performs a SQL query to select information about the specified media item,
     * including its attributes and associated genres, subgenres, themes, and literary elements.
     *
     * @return A ResultSet containing the information retrieved from the database
     * @throws SQLException if a database access error occurs or the SQL execution fails
     */
    public ResultSet getInfo() throws SQLException {
        // Get the ID of the media item
        int id = MediaDatabaseController.getId();
        // Prepare the SQL statement to retrieve information about the media item
        PreparedStatement ps = conn.prepareStatement("SELECT m.*, GROUP_CONCAT(g.genre) genres, GROUP_CONCAT(sg.subgenre) subGenres, " +
                "GROUP_CONCAT(t.theme) themes, GROUP_CONCAT(e.element) element FROM media m " +
                "LEFT JOIN media_info mi ON mi.media_id = m.id LEFT JOIN genres g ON mi.genre_id = g.genre_id " +
                "LEFT JOIN subgenres sg ON mi.subgenre_id = sg.subgenre_id LEFT JOIN themes t ON mi.theme_id = t.theme_id " +
                "LEFT JOIN literary_elements e ON mi.element_id = e.element_id WHERE id = ? GROUP BY m.id");

        // Set the ID parameter for the SQL query
        ps.setInt(1, id);
        // Execute the query and retrieve the results
        rs = ps.executeQuery();
        // Return the ResultSet containing the retrieved information
        return rs;
    }

    /**
     * @param media
     * @return True if media exists in database
     * @throws SQLException
     */
    public boolean mediaExists(Media media) throws SQLException {
        //Query to check if there's a media in the database with matching title, creator, and year
        String query = "SELECT * FROM media WHERE title = ? AND creator = ? AND year = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, media.getTitle());
        ps.setString(2, media.getCreator());
        ps.setInt(3, media.getYearOfRelease());

        //execute query and get result set
        ResultSet rs = ps.executeQuery();

        //returns true or false if media exists in the database
        return rs.next();
    }


    public int findID(Media media) throws SQLException {
        int id = 0;
        PreparedStatement ps = conn.prepareStatement("SELECT id FROM media WHERE title = ? AND creator = ? AND year = ?");
        ps.setString(1, media.getTitle());
        ps.setString(2, media.getCreator());
        ps.setInt(3, media.getYearOfRelease());

        ResultSet rs = ps.executeQuery();

        while(rs.next()){
            id = rs.getInt("id");
        }
        return id;
    }

    public ArrayList<Media> getBooks() throws SQLException, IOException {
        ArrayList<Media> books = new ArrayList<>();
        String query = "SELECT * FROM media WHERE cinematographer IS NULL AND editor IS NULL";
        PreparedStatement ps = conn.prepareStatement(query);

        rs = ps.executeQuery();

        while(rs.next()){
            books.add(new Media(rs.getString("title"), rs.getString("creator"), rs.getInt("year")));
        }

        return books;
    }

    public ArrayList<Media> getFilms() throws SQLException, IOException {
        ArrayList<Media> films = new ArrayList<>();
        String query = "SELECT * FROM media WHERE cinematographer IS NOT NULL AND editor IS NOT NULL";
        PreparedStatement ps = conn.prepareStatement(query);

        rs = ps.executeQuery();

        while(rs.next()){
            films.add(new Media(rs.getString("title"), rs.getString("creator"), rs.getInt("year")));
        }

        return films;
    }

    public void deleteEmptyRows() throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM media_info WHERE genre_id IS NULL AND subgenre_id IS NULL " +
                "AND theme_id IS NULL AND element_id IS NULL");
        ps.executeUpdate();
    }
}
