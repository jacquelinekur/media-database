package mediaDatabase;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;

import javax.swing.*;

import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Author: Jacqueline Kurniawan
 */
public class MediaEntry implements Initializable {
    @FXML
    ImageView imageView;
    @FXML
    TextField tfTitle, tfCreator, tfYear, tfCinematographer, tfDirector, tfEditor, tfSearch;
    @FXML
    AnchorPane anchorPane = new AnchorPane();
    @FXML
    TableView tableView;
    @FXML
    TableColumn tcAuthor, tcYear, tcTitle;
    @FXML
    TextArea txtNote;
    @FXML
    ComboBox comboTheme, comboElement, comboGenre, comboSubGenre;
    @FXML
    VBox vGenre, vTheme, vSubGenre, vElement;
    @FXML
    ScrollPane scrollGenre;

    ObservableList genreList = FXCollections.observableArrayList();
    ObservableList elementList = FXCollections.observableArrayList();
    ObservableList subgenreList = FXCollections.observableArrayList();
    ObservableList themeList = FXCollections.observableArrayList();

    //keep track of all the media information and their respective id available in the database
    HashMap<String,Integer> genreDict = new HashMap<>();
    HashMap<String,Integer> subgenreDict = new HashMap<>();
    HashMap<String,Integer> themeDict = new HashMap<>();
    HashMap<String,Integer> litDict = new HashMap<>();

    //lists that hold the information that is currently associated to a specified media
    ArrayList<String> genre = new ArrayList<>();
    ArrayList<String> theme = new ArrayList<>();
    ArrayList<String> subgenre = new ArrayList<>();
    ArrayList<String> literaryElements = new ArrayList<>();

    //ArrayList that holds list of media information that the user wants to remove for a specified media
    ArrayList<String> genreRemove = new ArrayList<>();
    ArrayList<String> subgenreRemove= new ArrayList<>();;
    ArrayList<String> themeRemove= new ArrayList<>();;
    ArrayList<String> elementRemove= new ArrayList<>();;

    ArrayList<String> genreAdd = new ArrayList<>();
    ArrayList<String> subgenreAdd = new ArrayList<>();
    ArrayList<String> themeAdd = new ArrayList<>();
    ArrayList<String> elementAdd = new ArrayList<>();

    Connection conn;
    DBConnection dbConn = new DBConnection();
    Media media = new Media();

    ObservableList<Media> ol;
    int index = -1;

    public MediaEntry() throws SQLException, IOException {
        conn = DBConnection.connectDB();
    }

    public void onBtnBackClick(ActionEvent actionEvent) throws IOException{
        Parent mediaDatabaseControl = FXMLLoader.load(getClass().getResource("mainPage-view.fxml"));
        Scene scene = new Scene(mediaDatabaseControl);
        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setTitle("Main Page");
        scene.getRoot().setStyle("-fx-font-family: SansSerif");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * displays information dialog that media already exists in the database
     * @pre:
     */
    public void warning(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Media Already Exists");
        alert.setContentText("There is already a media with the same title, creator, and year of release in your database!");
        alert.getDialogPane().setStyle("-fx-font-family: SansSerif");
        alert.showAndWait();
    }

    public void reset(){
        tfTitle.setText("");
        tfYear.setText("");
        txtNote.setText("");
        imageView.setImage(null);
        tfCreator.setText("");

        if(tfCinematographer != null){
            tfCinematographer.setText("");
        }
        if(tfEditor != null){
            tfEditor.setText("");
        }
    }

    /**
     * Allows client to choose an image from their files and upload or change the current image on the ImageView.
     * @pre: method is called when user clicks 'uplaod/change image' button
     * @post: selected image is displayed on the ImageView
     */
    public void UploadCoverImage() {
        //Creates a FileChooser to allow client to select an image from their files
        FileChooser fc = new FileChooser();
        //filter to restrict client's choice to a png or jpg file
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg");
        fc.getExtensionFilters().add(filter);
        //shows the file dialog and gets the selected file
        File file = fc.showOpenDialog(null);

        try {
            //create a BufferedImage from the selected file that was read
            BufferedImage bufferedImage = ImageIO.read(file);
            //converts BufferedImage to JavaFX Image
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            //sets ImageView to the JavaFX Image
            imageView.setImage(image);
            //calls UploadImage method to set the selected image file to its associated media object
            media.setPicture(UploadImage(file));
        } catch (IOException ex) {
            System.out.println("exception");
        }

    }

    /**
     * Receives a file (file image chosen by the client) and returns it as a byte array
     * @param file
     * @return byte array
     * @throws FileNotFoundException
     */
    public byte[] UploadImage(File file) throws FileNotFoundException {
        // Create FileInputStream object to read the image file
        FileInputStream fis = new FileInputStream(file);
        //Create ByteArrayOutputStream to store the image as bytes
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        try {
            //read contents of FileInputStream to the ByteArrayOutputStream
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum);
            }
        } catch (IOException ex) {
            System.out.println("Exception");
        }

        // convert ByteArrayOutputStream into a byte array and return the array
        byte[] bytes = bos.toByteArray();

        return bytes;
    }

    @FXML
    public void onItemClickClose(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void itemEditFilm(ActionEvent actionEvent) throws IOException {
        Parent mediaDatabaseControl = FXMLLoader.load(getClass().getResource("addFilm-view.fxml"));
        Scene scene = new Scene(mediaDatabaseControl);
        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setTitle("Edit Film");
        scene.getRoot().setStyle("-fx-font-family: SansSerif");
        stage.setScene(scene);
        stage.show();
    }

    public void itemEditBook(ActionEvent actionEvent) throws IOException {
        Parent mediaDatabaseControl = FXMLLoader.load(getClass().getResource("addBook-view.fxml"));
        Scene scene = new Scene(mediaDatabaseControl);
        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setTitle("Edit Book");
        scene.getRoot().setStyle("-fx-font-family: SansSerif");
        stage.setScene(scene);
        stage.show();
    }

    public void removeImage(ActionEvent actionEvent) throws IOException {
        imageView.setImage(null);
        media.setPicture(Files.readAllBytes(Paths.get("src/main/resources/mediaDatabase/240_F_64672736_U5kpdGs9keUll8CRQ3p3YaEv2M6qkVY5.jpg")));
    }

    /** @pre: method is called when client clicks 'add' button
     * @post: book information is added to the 'media' table in database and book's title, year of release, and author is shown on the tableView*/
    public void onBtnAddClick() throws SQLException, IOException {
        //Check if author, title, and year TextFields are empty
        if(tfCreator.getText().isBlank() || tfTitle.getText().isBlank() || tfYear.getText().isBlank()){
            //displays alert dialog to tell client to fill in the required fields
            emptyFieldWarning();
        } else{
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
                dbConn.addBook(media);
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
            genreAdd = new ArrayList<>();
            subgenreAdd = new ArrayList<>();
            themeAdd = new ArrayList<>();
            elementAdd = new ArrayList<>();
            genreRemove = new ArrayList<>();
            subgenreRemove = new ArrayList<>();
            themeRemove = new ArrayList<>();
            elementRemove = new ArrayList<>();
        }
    }

    public void selectMediaWarning(){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("No media selected!");
        alert.setHeaderText("Please select a media to remove or edit");
        alert.setContentText("Click on a media displayed on the table to remove or edit");
        alert.getDialogPane().setStyle("-fx-font-family: SansSerif");
        alert.showAndWait();
    }

    public void removeMedia() throws SQLException, IOException {
        if(index <= -1){
            selectMediaWarning();
            return;
        }

        PreparedStatement ps = null;
        int id = dbConn.findID(media);
        ps = conn.prepareStatement("DELETE FROM media WHERE title = ? AND creator = ? AND year = ?");
        ps.setString(1, tfTitle.getText());
        ps.setString(2, tfCreator.getText());
        ps.setInt(3, Integer.parseInt(tfYear.getText()));

        ps.execute();

        ByteArrayInputStream bais = new ByteArrayInputStream(media.getPicture());
        ps = conn.prepareStatement("INSERT INTO trash_history (id, image, title, creator, year, note, time) " +
                "VALUES(?,?,?,?,?,?,?)");
        ps.setInt(1, id);
        ps.setBlob(2, bais);
        ps.setString(3, tfTitle.getText());
        ps.setString(4, tfCreator.getText());
        ps.setInt(5, Integer.parseInt(tfYear.getText()));
        ps.setString(6, txtNote.getText());
        ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

        ps.execute();

        updateTable();
        reset();
        resetBox();

    }

    public void resetBox(){
        ArrayList<Node> nodes = new ArrayList<>();

        for(Node node: vGenre.getChildren()){
            nodes.add(node);
        }
        vGenre.getChildren().removeAll(nodes);

        nodes = new ArrayList<>();

        for(Node node: vSubGenre.getChildren()){
            nodes.add(node);
        }
        vSubGenre.getChildren().removeAll(nodes);

        nodes = new ArrayList<>();

        for(Node node: vElement.getChildren()){
            nodes.add(node);
        }
        vElement.getChildren().removeAll(nodes);

        nodes = new ArrayList<>();

        for(Node node: vTheme.getChildren()){
            nodes.add(node);
        }
        vTheme.getChildren().removeAll(nodes);

    }

    public void getSelected() throws SQLException, IOException {
        genreRemove = new ArrayList<>();
        subgenreRemove = new ArrayList<>();
        elementRemove = new ArrayList<>();
        themeRemove = new ArrayList<>();

        genreAdd = new ArrayList<>();
        subgenreAdd = new ArrayList<>();
        elementAdd = new ArrayList<>();
        themeAdd = new ArrayList<>();

        resetBox();
        index = tableView.getSelectionModel().getFocusedIndex();
        if(index <= -1){
            return;
        }
        tfTitle.setText(tcTitle.getCellData(index).toString());
        tfCreator.setText(tcAuthor.getCellData(index).toString());
        tfYear.setText(tcYear.getCellData(index).toString());

        media = new Media(tfTitle.getText(), tfCreator.getText(), Integer.parseInt(tfYear.getText()));

        String query = "SELECT m.*, GROUP_CONCAT(g.genre) genres, GROUP_CONCAT(sg.subgenre) subGenres, " +
                " GROUP_CONCAT(t.theme) themes, GROUP_CONCAT(e.element) element FROM media m LEFT JOIN media_info mi ON mi.media_id = m.id " +
                "LEFT JOIN genres g ON mi.genre_id = g.genre_id LEFT JOIN subgenres sg ON mi.subgenre_id = sg.subgenre_id " +
                "LEFT JOIN themes t ON mi.theme_id = t.theme_id LEFT JOIN literary_elements e ON mi.element_id = e.element_id " +
                "WHERE m.title = ? AND m.creator = ? AND m.year = ? GROUP BY m.id;";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, tfTitle.getText());
        ps.setString(2, tfCreator.getText());
        ps.setInt(3, Integer.parseInt(tfYear.getText()));

        ResultSet rs = ps.executeQuery();

        while(rs.next()){
            Blob blob = rs.getBlob("image");
            String genres = rs.getString("genres");
            String subGenres = rs.getString("subGenres");
            String themes = rs.getString("themes");
            String element = rs.getString("element");
            String editor = rs.getString("editor");
            String cinematographer = rs.getString("cinematographer");
            String notes = rs.getString("note");

            byte[] byteImage = blob.getBytes(1, (int)blob.length());
            ByteArrayInputStream bais = new ByteArrayInputStream(byteImage);
            BufferedImage bufferedImage = ImageIO.read(bais);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            imageView.setImage(image);
            media.setPicture(byteImage);
            media.setNote(notes);
            txtNote.setText(notes);

            if(cinematographer != null){
                tfCinematographer.setText(cinematographer);
                tfEditor.setText(editor);
            }

            genre = new ArrayList<String>();
            subgenre = new ArrayList<>();
            literaryElements = new ArrayList<>();
            theme = new ArrayList<>();

            genreList.clear();
            subgenreList.clear();
            themeList.clear();
            elementList.clear();
            setMediaInfo();

            if(genres != null){
                String[] results = genres.split(",", 0);
                Collections.addAll(genre, results);

                for(int i = 0; i < genre.size(); i++){
                    Text txt = new Text(genre.get(i));
                    txt.setFont(new Font("SansSerif Regular",14));
                    txt.setOnMouseClicked(e -> removeGenre(e,txt));
                    txt.setWrappingWidth(270);
                    vGenre.getChildren().add(txt);
                    genreList.remove(genre.get(i));
                }
            }
            if(subGenres != null){
                String[] results = subGenres.split(",", 0);
                Collections.addAll(subgenre, results);

                for(int i = 0; i < subgenre.size(); i++){
                    Text txt = new Text(subgenre.get(i));
                    txt.setFont(new Font("SansSerif Regular",14));
                    txt.setOnMouseClicked(e -> removeSubGenre(e,txt));
                    txt.setWrappingWidth(270);
                    vSubGenre.getChildren().add(txt);
                    subgenreList.remove(subgenre.get(i));
                }
            }
            if(themes != null){
                String[] results = themes.split(",", 0);
                Collections.addAll(theme, results);

                for(int i = 0; i < theme.size(); i++){
                    Text txt = new Text(theme.get(i));
                    txt.setFont(new Font("SansSerif Regular",14));
                    txt.setOnMouseClicked(e -> removeTheme(e,txt));
                    txt.setWrappingWidth(270);
                    vTheme.getChildren().add(txt);
                    themeList.remove(theme.get(i));
                }
            }
            if(element != null){
                String[] results = element.split(",", 0);
                Collections.addAll(literaryElements, results);

                for(int i = 0; i < literaryElements.size(); i++){
                    Text txt = new Text(literaryElements.get(i));
                    txt.setFont(new Font("SansSerif Regular",14));
                    txt.setOnMouseClicked(e -> removeElement(e,txt));
                    txt.setWrappingWidth(270);
                    vElement.getChildren().add(txt);
                    elementList.remove(literaryElements.get(i));
                }
            }

            comboGenre.setItems(genreList);
            comboSubGenre.setItems(subgenreList);
            comboTheme.setItems(themeList);
            comboElement.setItems(elementList);

        }
    }

    public void updateTable() throws SQLException, IOException {
        ol = FXCollections.observableArrayList(dbConn.getBooks());

        tcTitle.setCellValueFactory(new PropertyValueFactory<Media, String>("title"));
        tcAuthor.setCellValueFactory(new PropertyValueFactory<Media, String>("creator"));
        tcYear.setCellValueFactory(new PropertyValueFactory<Media, String>("yearOfRelease"));

        tableView.setItems(ol);
    }

    public void setMediaInfo(){
        genreDict = new HashMap<>();
        subgenreDict = new HashMap<>();
        themeDict = new HashMap<>();
        litDict = new HashMap<>();

        try{
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = conn.prepareStatement("SELECT genre_id, genre FROM genres");
            rs = ps.executeQuery();

            while(rs.next()){
                genreList.add(rs.getString("genre"));
                genreDict.put(rs.getString("genre"), rs.getInt("genre_id"));
            }

            ps = conn.prepareStatement("SELECT subgenre_id, subgenre FROM subgenres");
            rs = ps.executeQuery();

            while(rs.next()){
                subgenreList.add(rs.getString("subgenre"));
                subgenreDict.put( rs.getString("subgenre"), rs.getInt("subgenre_id"));
            }
            ps = conn.prepareStatement("SELECT theme_id, theme FROM themes");
            rs = ps.executeQuery();

            while(rs.next()){
                themeList.add(rs.getString("theme"));
                themeDict.put(rs.getString("theme"), rs.getInt("theme_id"));
            }

            ps = conn.prepareStatement("SELECT element_id, element FROM literary_elements");
            rs = ps.executeQuery();

            while(rs.next()){
                elementList.add(rs.getString("element"));
                litDict.put(rs.getString("element"), rs.getInt("element_id"));
            }

            comboElement.setItems(elementList);
            comboGenre.setItems(genreList);
            comboSubGenre.setItems(subgenreList);
            comboTheme.setItems(themeList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setMediaInfo();
        if (url.equals(getClass().getResource("addBook-view.fxml"))) {
            try {
                updateTable();

                //filtered list
                FilteredList<Media> filtered = new FilteredList<>(ol, b-> true);

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

                SortedList<Media> sort = new SortedList<>(filtered);

                sort.comparatorProperty().bind(tableView.comparatorProperty());
                tableView.setItems(sort);

            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void addElement(ActionEvent actionEvent) {
        if(comboElement.getValue() != null){
            //literaryElements.add(comboElement.getValue().toString());
            elementAdd.add(comboElement.getValue().toString());
            Text txt = new Text(comboElement.getValue().toString());
            txt.setFont(new Font("SansSerif Regular",14));
            txt.setWrappingWidth(270);
            txt.setOnMouseClicked(e -> removeElement(e,txt));
            vElement.getChildren().add(txt);
            elementList.remove(comboElement.getValue().toString());
            comboElement.setItems(elementList);
            comboElement.setValue("");
        }
    }

    public void addSubGenre(ActionEvent actionEvent) {
        if(comboSubGenre.getValue() != null){
            //subgenre.add(comboSubGenre.getValue().toString());
            subgenreAdd.add(comboSubGenre.getValue().toString());
            Text txt = new Text(comboSubGenre.getValue().toString());
            txt.setFont(new Font("SansSerif Regular",14));
            txt.setWrappingWidth(270);
            txt.setOnMouseClicked(e -> removeSubGenre(e,txt));
            vSubGenre.getChildren().add(txt);
            subgenreList.remove(comboSubGenre.getValue().toString());
            comboSubGenre.setItems(subgenreList);
            comboSubGenre.setValue("");
        }
    }

    public void addTheme(ActionEvent actionEvent) {
        if(comboTheme.getValue() != null){
            //theme.add(comboTheme.getValue().toString());
            themeAdd.add(comboTheme.getValue().toString());
            Text txt = new Text(comboTheme.getValue().toString());
            txt.setFont(new Font("SansSerif Regular",14));
            txt.setWrappingWidth(270);
            txt.setOnMouseClicked(e -> removeTheme(e,txt));
            vTheme.getChildren().add(txt);
            themeList.remove(comboTheme.getValue().toString());
            comboTheme.setItems(themeList);
            comboTheme.setValue("");
        }
    }

    public void addGenre(ActionEvent actionEvent) {
        if(comboGenre.getValue() != null){
            //genre.add(comboGenre.getValue().toString());
            genreAdd.add(comboGenre.getValue().toString());
            Text txt = new Text(comboGenre.getValue().toString());
            txt.setFont(new Font("SansSerif Regular",14));
            txt.setWrappingWidth(270);
            txt.setOnMouseClicked(e -> removeGenre(e,txt));
            vGenre.getChildren().add(txt);
            genreList.remove(comboGenre.getValue().toString());
            comboGenre.setItems(genreList);
            comboGenre.setValue("");
        }
    }

    public String textToString(Text text){
        int first = text.toString().indexOf("\"")+1;
        int last = text.toString().lastIndexOf("\"");
        return text.toString().substring(first, last);
    }

    public void removeGenre(MouseEvent event, Text text){
        genreRemove.add(textToString(text));
        vGenre.getChildren().remove(text);
        genreList.add(textToString(text));
        comboGenre.setItems(genreList);
    }

    public void removeSubGenre(MouseEvent event, Text text){
        subgenreRemove.add(textToString(text));
        vSubGenre.getChildren().remove(text);
        subgenreList.add(textToString(text));
        comboSubGenre.setItems(subgenreList);
    }

    public void removeElement(MouseEvent event, Text text){
        elementRemove.add(textToString(text));
        vElement.getChildren().remove(text);
        elementList.add(textToString(text));
        comboElement.setItems(elementList);
    }

    public void removeTheme(MouseEvent event, Text text){
        themeRemove.add(textToString(text));
        vTheme.getChildren().remove(text);
        themeList.add(textToString(text));
        comboTheme.setItems(themeList);
    }

    /**
     * Removes associations between media and various categories (genre, subgenre, theme, element) in the database.
     * @pre: method is called when a user is editing or adding a media
     * @post: all media information the user deleted is no longer associated to the selected media in the database
     */
    public void removeMediaInfo() throws SQLException {
        //loops through all the genres the user removed for a media and removes its association with the media in the database
        for (int i = 0; i < genreRemove.size(); i++) {
            PreparedStatement ps2 = conn.prepareStatement("UPDATE media_info SET genre_id = null WHERE genre_id = ? AND media_id = ?");
            //gets id of each genre from HashMap to set into the database
            ps2.setInt(1, genreDict.get(genreRemove.get(i)));
            ps2.setInt(2, dbConn.findID(media));
            ps2.executeUpdate();
        }
        //loops through all the subgenres the user removed for a media and removes its association with the media in the database
        for (int i = 0; i < subgenreRemove.size(); i++) {
            PreparedStatement ps2 = conn.prepareStatement("UPDATE media_info SET subgenre_id = null WHERE subgenre_id = ? AND media_id = ?");
            //gets id of each subgenre from HashMap to set into the database
            ps2.setInt(1, subgenreDict.get(subgenreRemove.get(i)));
            ps2.setInt(2, dbConn.findID(media));
            ps2.executeUpdate();
        }
        //loops through all the themes the user removed for a media and removes its association with the media in the database
        for (int i = 0; i < themeRemove.size(); i++) {
            PreparedStatement ps2 = conn.prepareStatement("UPDATE media_info SET theme_id = null WHERE theme_id = ? AND media_id = ?");
            //gets id of each theme from HashMap to set into the database
            ps2.setInt(1, themeDict.get(themeRemove.get(i)));
            ps2.setInt(2, dbConn.findID(media));
            ps2.executeUpdate();
        }
        //loops through all the literary elements the user removed for a media and removes its association with the media in the database
        for (int i = 0; i < elementRemove.size(); i++) {
            PreparedStatement ps2 = conn.prepareStatement("UPDATE media_info SET element_id = null WHERE element_id = ? AND media_id = ?");
            //gets id of each literary element from HashMap to set into the database
            ps2.setInt(1, litDict.get(elementRemove.get(i)));
            ps2.setInt(2, dbConn.findID(media));
            ps2.executeUpdate();
        }
        //deletes any records with no genre, subgenre, theme, and element from media_info table
        dbConn.deleteEmptyRows();
    }


    /**
     * Adds associations between media information and various categories (genre, subgenre, theme, element) in the database.
     * Inserts records into the 'media_info' table with specified IDs for the given media if the selected media isn't
     */
    public void addMediaInfo() throws SQLException {
        PreparedStatement ps2 = null;
        int id = dbConn.findID(media);
        //adds genre and associates it with the selected media
        ps2 = conn.prepareStatement("INSERT INTO media_info (media_id, genre_id) VALUES (?,?)");
        int infoID;
        for (int i = 0; i < genreAdd.size(); i++) {
            //checks if the entered genre currently exists in the database, if not, it will add it to the genres table in the database
            if(genreDict.get(genreAdd.get(i)) == null){
                PreparedStatement ps3 = conn.prepareStatement("INSERT INTO  genres (genre) VALUES (?)");
                ps3.setString(1, genreAdd.get(i));
                ps3.execute();
                genreList.clear();
                setMediaInfo();
            }
            //gets id of the added genre
            infoID = genreDict.get(genreAdd.get(i));
            //sets the id to be added to hte media_info table
            ps2.setInt(1, id);
            ps2.setInt(2, infoID);
            ps2.execute();
        }
        //adds subgenre and associates it with the selected media
        ps2 = conn.prepareStatement("INSERT INTO media_info (media_id, subgenre_id) VALUES (?,?)");
        for (int i = 0; i < subgenreAdd.size(); i++) {
            //checks if the entered subgenre currently exists in the database, if not, it will add it to the subgenres table in the database
            if(subgenreDict.get(subgenreAdd.get(i)) == null){
                PreparedStatement ps3 = conn.prepareStatement("INSERT INTO  subgenres (subgenre) VALUES (?)");
                ps3.setString(1, subgenreAdd.get(i));
                ps3.execute();
                subgenreList.clear();
                setMediaInfo();
            }

            infoID = subgenreDict.get(subgenreAdd.get(i));

            ps2.setInt(1, id);
            ps2.setInt(2, infoID);

            ps2.execute();
        }

        ps2 = conn.prepareStatement("INSERT INTO media_info (media_id, theme_id) VALUES (?,?)");
        for (int i = 0; i < themeAdd.size(); i++) {
            if(themeDict.get(themeAdd.get(i)) == null){
                PreparedStatement ps3 = conn.prepareStatement("INSERT INTO  themes (theme) VALUES (?)");
                ps3.setString(1, themeAdd.get(i));
                ps3.execute();
                themeList.clear();
                setMediaInfo();
            }

            infoID = themeDict.get(themeAdd.get(i));

            ps2.setInt(1, id);
            ps2.setInt(2, infoID);

            ps2.execute();
        }

        ps2 = conn.prepareStatement("INSERT INTO media_info (media_id, element_id) VALUES (?,?)");
        for (int i = 0; i < elementAdd.size(); i++) {
            if(litDict.get(elementAdd.get(i)) == null){
                PreparedStatement ps3 = conn.prepareStatement("INSERT INTO  literary_elements (element) VALUES (?)");
                ps3.setString(1, elementAdd.get(i));
                ps3.execute();
                elementList.clear();
                setMediaInfo();
            }

            infoID = litDict.get(elementAdd.get(i));

            ps2.setInt(1, id);
            ps2.setInt(2, infoID);

            ps2.execute();
        }
    }

    public void emptyFieldWarning(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Empty Field");
        alert.setHeaderText("All required fields need to be filled!");
        alert.setContentText("Either title, author, and/or year of release field has not been filled out");
        alert.getDialogPane().setStyle("-fx-font-family: SansSerif");
        alert.showAndWait();
    }

    public void editMedia(ActionEvent actionEvent) throws SQLException, IOException {
        PreparedStatement ps = null;
        if(tfTitle.getText().isBlank() || tfCreator.getText().isBlank() || tfYear.getText().isBlank()){
            emptyFieldWarning();
            return;
        }
        //gets id of media in the database
        int id = dbConn.findID(media);
        //removes any genres, subgenres, themes, elements associated to the media in the database that the user deleted
        removeMediaInfo();
        //adds genres, subgenres, themes, elements associated to the media in the database that the user added
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
        ps = conn.prepareStatement("UPDATE media SET image = ? WHERE id = ?");
        ps.setBlob(1, bais);
        ps.setInt(2, id);
        ps.executeUpdate();
        //makes all the fields and vBox blank.
        resetBox();
        reset();
        //updates table with the new changes.
        updateTable();
    }

    public void itemTrashClick(ActionEvent actionEvent) throws IOException {
        Parent mediaDatabaseControl = FXMLLoader.load(getClass().getResource("trashHistory-view.fxml"));
        Scene scene = new Scene(mediaDatabaseControl);
        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setTitle("Deleted Media");
        scene.getRoot().setStyle("-fx-font-family: SansSerif");
        stage.setScene(scene);
        stage.show();
    }
}
