package mediaDatabase;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

/**
 * Author: Jacqueline Kurniawan
 */
public class FilmEntry extends MediaEntry{
    @FXML
    TextField tfCinematographer, tfEditor;
    Connection conn;

    //connect to the database
    public FilmEntry() throws SQLException, IOException {
        conn = DBConnection.connectDB();
    }

    /**
     * allows the user to edit the title, director, year of release, notes, information and picture of a film when the user
     * clicks the 'edit' button on the screen. Same purpose as the method it override from but for films instead of books
     * @param actionEvent
     * @throws SQLException
     * @throws IOException
     */
    @Override
    public void editMedia(ActionEvent actionEvent) throws SQLException, IOException {
        PreparedStatement ps = null;
        //checks if any of the required fields (title, creator, year) was deleted or missing, if yes, gives user an alert
        if(tfTitle.getText().isBlank() || tfCreator.getText().isBlank() || tfYear.getText().isBlank()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Empty Field");
            alert.setHeaderText("All required fields need to be filled!");
            alert.setContentText("Make sure all required fields are filled out");
            alert.getDialogPane().setStyle("-fx-font-family: SansSerif");
            alert.showAndWait();
            return;
        }
        //finds the id of the selected media to be edited in the database
        int id = dbConn.findID(media);
        //calls the remove media info method
        removeMediaInfo();
        //calls the add media info method
        addMediaInfo();

        //checks if any changes were made to the title, year, or creator. If yes, database will be updated.
        if(!(media.getTitle().equals(tfTitle.getText())) || !(media.getCreator().equals(tfCreator.getText())) ||
                media.getYearOfRelease() != Integer.parseInt(tfYear.getText())){
            ps = conn.prepareStatement("UPDATE media SET title = ?, year = ?, creator = ? WHERE id = ? ");
            ps.setString(1, tfTitle.getText());
            ps.setInt(2, Integer.parseInt(tfYear.getText()));
            ps.setString(3, tfCreator.getText());
            ps.setInt(4, id);
            ps.executeUpdate();
        }

        //checks if any changes to the notes were made, if yes, the note column in the database will be updated
        if(!(txtNote.getText().equals(media.getNote()))){
            ps = conn.prepareStatement("UPDATE media SET note = ? WHERE id = ?");
            ps.setString(1, txtNote.getText());
            ps.setInt(2, id);
            ps.executeUpdate();
        }

        //updates the image in the database to the current image on the imageView
        ByteArrayInputStream bais = new ByteArrayInputStream(media.getPicture());
        ps = conn.prepareStatement("UPDATE media SET image = ?, editor = ?, cinematographer = ? WHERE id = ?");
        ps.setBlob(1, bais);
        ps.setString(2, tfEditor.getText());
        ps.setString(3, tfCinematographer.getText());
        ps.setInt(4, id);
        ps.executeUpdate();

        //makes all the fields and vBox blank.
        resetBox();
        reset();
        //updates table with the new changes.
        updateTable();
    }

    /**
     * Removes a media entry from the database and archives it into the trash history.
     * @throws SQLException
     * @throws IOException
     */
    @Override
    public void removeMedia() throws SQLException, IOException {
        if(index <= -1){
            // Display warning if no media is selected
            selectMediaWarning();
        }
        else{
            PreparedStatement ps = null;
            int id = dbConn.findID(media);
            // Delete the media entry from the 'media' table
            ps = conn.prepareStatement("DELETE FROM media WHERE title = ? AND creator = ? AND year = ?");
            ps.setString(1, tfTitle.getText());
            ps.setString(2, tfCreator.getText());
            ps.setInt(3, Integer.parseInt(tfYear.getText()));

            ps.execute();

            // Archive the deleted media entry into the 'trash_history' table
            ByteArrayInputStream bais = new ByteArrayInputStream(media.getPicture());
            ps = conn.prepareStatement("INSERT INTO trash_history (id, image, title, creator, year, editor, cinematographer, note, time) " +
                    "VALUES(?,?,?,?,?,?,?,?,?)");
            ps.setInt(1, id);
            ps.setBlob(2, bais);
            ps.setString(3, tfTitle.getText());
            ps.setString(4, tfCreator.getText());
            ps.setInt(5, Integer.parseInt(tfYear.getText()));
            ps.setString(6, tfEditor.getText());
            ps.setString(7, tfCinematographer.getText());
            ps.setString(8, txtNote.getText());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));

            ps.execute();

            // Update the table display
            updateTable();
            // Reset input fields and selections
            reset();
            resetBox();
        }
    }

    /**
     * Adds a Film to the database
     * @throws SQLException
     * @throws IOException
     */
    @Override
    public void onBtnAddClick() throws SQLException, IOException {
        //Check if author, title, and year TextFields are empty
        if(tfCreator.getText().isBlank() || tfTitle.getText().isBlank() || tfYear.getText().isBlank()){
            //displays alert dialog to tell client to fill in the required fields
            emptyFieldWarning();
        }
        else{
            String editor = tfEditor.getText();
            String cinematographer = tfCinematographer.getText();
            //gets the user input from the TextFields and sets it to the media object
            media.setTitle(tfTitle.getText());
            media.setCreator(tfCreator.getText());
            media.setYearOfRelease(Integer.parseInt(tfYear.getText()));
            media.setNote(txtNote.getText());
            //checks if media/book already exists in the database and displays warning if book exists
            if(dbConn.mediaExists(media)){
                warning();
            } else{
                //adds book to the database
                // SQL Query to add media object into 'media' table in database
                String sql = "INSERT INTO media(image, title, creator, year, editor, cinematographer, note) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement preparedStatement = conn.prepareStatement(sql);

                //converts byte array of media's image to a ByteArrayInputStream
                ByteArrayInputStream bais = new ByteArrayInputStream(media.getPicture());

                // Set the parameters of the SQL query with data from the 'Media' object
                preparedStatement.setBlob(1, bais);
                preparedStatement.setString(2, media.getTitle());
                preparedStatement.setString(3, media.getCreator());
                preparedStatement.setInt(4, media.getYearOfRelease());
                preparedStatement.setString(5, editor);
                preparedStatement.setString(6, cinematographer);
                preparedStatement.setString(7, txtNote.getText());
                preparedStatement.execute();
                //adds the genres, subgenres, literary elements, and themes the user added to the database
                addMediaInfo();
                //removes the genres, subgenres, literary elements, and themes the user removed to the database
                removeMediaInfo();
                //resets or empty the TextFields
                reset();
                resetBox();
            }
            //updates the TableView which displays the books in the database
            updateTable();
        }
    }

    /**
     * Updates the TableView with the latest data from the database.
     * This method retrieves films from the database connection, populates an ObservableList,
     * configures cell value factories for each column, and sets the items of the TableView.
     * @throws SQLException
     * @throws IOException
     */
    @Override
    public void updateTable() throws SQLException, IOException {
        // Retrieve films from the database and create an ObservableList
        ol = FXCollections.observableArrayList(dbConn.getFilms());

        // Configure cell value factories for each column
        tcTitle.setCellValueFactory(new PropertyValueFactory<Media, String>("title"));
        tcAuthor.setCellValueFactory(new PropertyValueFactory<Media, String>("creator"));
        tcYear.setCellValueFactory(new PropertyValueFactory<Media, String>("yearOfRelease"));

        // Set the items of the TableView
        tableView.setItems(ol);
    }

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method sets up media information, updates the TableView with the latest data from the database,
     * and configures filtering and sorting functionalities for the TableView based on user input.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up media information
        setMediaInfo();
        // Update TableView and configure filtering and sorting only if the FXML file matches "addFilm-view.fxml"
        if (url.equals(getClass().getResource("addFilm-view.fxml"))) {
            try {
                // Update TableView with the latest data from the database
                updateTable();

                // Create a filtered list based on the observable list
                FilteredList<Media> filtered = new FilteredList<>(ol, b-> true);

                // Add listener to text field for dynamic filtering
                tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                    filtered.setPredicate(Media -> {
                        //check if the TextField is empty
                        if(newValue.isBlank() || newValue.isEmpty() || newValue.equals(null)){
                            return true;
                        }

                        String keyword = newValue.toLowerCase();

                        //searches for Media whose title or author contains the letters inputted in the TextField
                        if(Media.getTitle().toLowerCase().indexOf(keyword) > -1){
                            return true;
                        }
                        else if(Media.getCreator().toLowerCase().indexOf(keyword) > -1){
                            return true;
                        }
                        else{
                            return false;
                        }
                    });
                });

                // Create a sorted list based on the filtered list
                SortedList<Media> sort = new SortedList<>(filtered);
                // Bind the comparator property of the sorted list to the comparator property of the TableView
                sort.comparatorProperty().bind(tableView.comparatorProperty());
                // Set the sorted list as the items of the TableView
                tableView.setItems(sort);

            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
