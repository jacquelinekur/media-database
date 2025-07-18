package mediaDatabase;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableIntegerArray;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.transform.Result;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

import java.awt.*;
import java.awt.event.*;

/**
 * Author: Jacqueline Kurniawan
 */
public class MediaDatabaseController implements Initializable {
    @FXML
    ComboBox<String> comboSort, comboYear, comboGenre, comboSubGenre, comboTheme;

    @FXML
    TextField tfSearch;

    ObservableList<String> sortList = FXCollections.observableArrayList("Creator", "Title");
    ObservableList yearList, genreList, subgenreList, themeList;

    Media media;
    DBConnection dbConn = new DBConnection();

    @FXML
    VBox vBox = new VBox();

    @FXML
    AnchorPane anchorPane = new AnchorPane();
    Connection conn;

    ArrayList<Media> results;

    private static int id;

    public MediaDatabaseController() throws SQLException {
        conn = DBConnection.connectDB();
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

    public void OnBtnAddFilmClick(ActionEvent actionEvent) throws IOException {
        Parent mediaDatabaseControl = FXMLLoader.load(getClass().getResource("addFilm-view.fxml"));
        Scene scene = new Scene(mediaDatabaseControl);
        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setTitle("Edit Film");
        scene.getRoot().setStyle("-fx-font-family: SansSerif");
        stage.setScene(scene);
        stage.show();
    }

    public void onBtnAddBookClick(ActionEvent actionEvent) throws IOException{
        Parent mediaDatabaseControl = FXMLLoader.load(getClass().getResource("addBook-view.fxml"));
        Scene scene = new Scene(mediaDatabaseControl);
        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setTitle("Edit Book");
        scene.getRoot().setStyle("-fx-font-family: SansSerif");
        stage.setScene(scene);
        stage.show();
    }

    public void onBtnSearchClick(ActionEvent actionEvent) throws SQLException, IOException {
        results = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;

        if(comboGenre.getValue() != null && comboGenre.getValue().equals("None")){
            comboGenre.setValue(null);
        }
        if(comboSubGenre.getValue() != null && comboSubGenre.getValue().equals("None")){
            comboSubGenre.setValue(null);
        }
        if(comboTheme.getValue() != null && comboTheme.getValue().equals("None")){
            comboTheme.setValue(null);
        }
        if(comboYear.getValue() != null && comboYear.getValue().isBlank()){
            comboYear.setValue(null);
        }

        if(comboYear.getValue() != null && comboGenre.getValue() != null && comboSubGenre.getValue() != null
                && comboTheme.getValue() != null){
            preparedStatement = conn.prepareStatement("SELECT m.* FROM media_info mi JOIN media m ON mi.media_id = m.id " +
                    "JOIN genres g ON mi.genre_id = g.genre_id JOIN subgenres sg ON mi.subgenre_id = sg.subgenre_id " +
                    "JOIN themes t ON mi.theme_id = t.theme_id WHERE genre = ? AND subgenre = ? AND theme = ? AND m.year = ? AND (title LIKE '%" +
                    tfSearch.getText() + "%' OR creator LIKE '%" +tfSearch.getText() + "%') GROUP BY m.id");
            preparedStatement.setString(1, comboGenre.getValue());
            preparedStatement.setString(2, comboSubGenre.getValue());
            preparedStatement.setString(3, comboTheme.getValue());
            preparedStatement.setString(4, comboYear.getValue());

        }
        else if(comboYear.getValue() == null && comboGenre.getValue() == null && comboSubGenre.getValue() == null
                && comboTheme.getValue() == null){
            preparedStatement = conn.prepareStatement("SELECT * FROM media WHERE title LIKE '%"
                    + tfSearch.getText() + "%' OR creator LIKE '%" + tfSearch.getText() + "%'");
        }
        else if(comboTheme.getValue() != null){
            if(comboYear.getValue() != null){
                if(comboGenre.getValue() != null){
                    preparedStatement = conn.prepareStatement("SELECT m.* FROM media_info mi JOIN media m ON mi.media_id = m.id " +
                            "JOIN genres g ON mi.genre_id = g.genre_id " +
                            "JOIN themes t ON mi.theme_id = t.theme_id WHERE genre = ? AND theme = ? AND m.year = ? AND (title LIKE '%" +
                            tfSearch.getText() + "%' OR creator LIKE '%" +tfSearch.getText() + "%') GROUP BY m.id");
                    preparedStatement.setString(1, comboGenre.getValue());
                    preparedStatement.setString(2, comboTheme.getValue());
                    preparedStatement.setString(3, comboYear.getValue());
                }
                else if(comboSubGenre.getValue() != null){
                   // rs = dbConn.searchMediaTSY(tfSearch.getText(), comboTheme.getValue(), comboSubGenre.getValue(), Integer.parseInt(comboYear.getValue()));
                    preparedStatement = conn.prepareStatement("SELECT m.* FROM media_info mi JOIN media m ON mi.media_id = m.id " +
                            "JOIN subgenres sg ON mi.subgenre_id = sg.subgenre_id " +
                            "JOIN themes t ON mi.theme_id = t.theme_id WHERE theme = ? AND subgenre = ? AND m.year = ? AND (title LIKE '%" +
                            tfSearch.getText() + "%' OR creator LIKE '%" +tfSearch.getText() + "%') GROUP BY m.id");
                    preparedStatement.setString(1, comboTheme.getValue());
                    preparedStatement.setString(2, comboSubGenre.getValue());
                    preparedStatement.setString(3, comboYear.getValue());
                }
                else{
                   // rs = dbConn.searchMediaTY(tfSearch.getText(), comboTheme.getValue(), Integer.parseInt(comboYear.getValue()));
                    preparedStatement = conn.prepareStatement("SELECT m.* FROM media_info mi JOIN media m ON mi.media_id = m.id " +
                            "JOIN themes t ON mi.theme_id = t.theme_id WHERE theme = ? AND m.year = ? AND (title LIKE '%" +
                            tfSearch.getText() + "%' OR creator LIKE '%" +tfSearch.getText() + "%') GROUP BY m.id");
                    preparedStatement.setString(1, comboTheme.getValue());
                    preparedStatement.setString(2, comboYear.getValue());
                }
            }
            else if(comboGenre.getValue() != null){
                if(comboSubGenre.getValue() != null){
                   // rs = dbConn.searchMediaGST(tfSearch.getText(), comboGenre.getValue(), comboSubGenre.getValue(), comboTheme.getValue());

                    preparedStatement = conn.prepareStatement("SELECT m.* FROM media_info mi JOIN media m ON mi.media_id = m.id " +
                            "JOIN genres g ON mi.genre_id = g.genre_id JOIN subgenres sg ON mi.subgenre_id = sg.subgenre_id " +
                            "JOIN themes t ON mi.theme_id = t.theme_id WHERE genre = ? AND subgenre = ? AND theme = ? AND (title LIKE '%" +
                            tfSearch.getText() + "%' OR creator LIKE '%" +tfSearch.getText() + "%') GROUP BY m.id");
                    preparedStatement.setString(1, comboGenre.getValue());
                    preparedStatement.setString(2, comboSubGenre.getValue());
                    preparedStatement.setString(3, comboTheme.getValue());
                }
                else{
                    //rs = dbConn.searchMediaGT(tfSearch.getText(), comboGenre.getValue(),  comboTheme.getValue());
                    preparedStatement = conn.prepareStatement("SELECT m.* FROM media_info mi JOIN media m ON mi.media_id = m.id " +
                            "JOIN genres g ON mi.genre_id = g.genre_id " +
                            "JOIN themes t ON mi.theme_id = t.theme_id WHERE genre = ? AND theme = ? AND (title LIKE '%" +
                            tfSearch.getText() + "%' OR creator LIKE '%" +tfSearch.getText() + "%') GROUP BY m.id");
                    preparedStatement.setString(1, comboGenre.getValue());
                    preparedStatement.setString(2, comboTheme.getValue());
                }
            }
            else if(comboSubGenre.getValue() != null){
                //rs = dbConn.searchMediaST(tfSearch.getText(), comboSubGenre.getValue(),  comboTheme.getValue());
                preparedStatement = conn.prepareStatement("SELECT m.* FROM media_info mi JOIN media m ON mi.media_id = m.id " +
                        "JOIN subgenres sg ON mi.subgenre_id = sg.subgenre_id " +
                        "JOIN themes t ON mi.theme_id = t.theme_id WHERE subgenre = ? AND theme = ? AND (title LIKE '%" +
                        tfSearch.getText() + "%' OR creator LIKE '%" +tfSearch.getText() + "%') GROUP BY m.id");
                preparedStatement.setString(1, comboSubGenre.getValue());
                preparedStatement.setString(2, comboTheme.getValue());
            }
            else{
                //rs = dbConn.searchThemes(tfSearch.getText(), comboTheme.getValue());
                preparedStatement = conn.prepareStatement("SELECT m.* FROM media_info mi JOIN media m ON mi.media_id = m.id " +
                        "JOIN themes t ON mi.theme_id = t.theme_id WHERE theme = ? AND (title LIKE '%" +
                        tfSearch.getText() + "%' OR creator LIKE '%" +tfSearch.getText() + "%') GROUP BY m.id");
                preparedStatement.setString(1, comboTheme.getValue());
            }
        }
        else if(comboGenre.getValue() != null){
            if(comboSubGenre.getValue() != null){
                if(comboYear.getValue() != null){
                    //rs = dbConn.searchMediaGSY(tfSearch.getText(), comboGenre.getValue(), comboSubGenre.getValue(), Integer.parseInt(comboYear.getValue()));
                    preparedStatement = conn.prepareStatement("SELECT m.* FROM media_info mi JOIN media m ON mi.media_id = m.id " +
                            "JOIN genres g ON mi.genre_id = g.genre_id JOIN subgenres sg ON mi.subgenre_id = sg.subgenre_id " +
                            "WHERE genre = ? AND subgenre = ? AND m.year = ? AND (title LIKE '%" +
                            tfSearch.getText() + "%' OR creator LIKE '%" +tfSearch.getText() + "%') GROUP BY m.id");
                    preparedStatement.setString(1, comboGenre.getValue());
                    preparedStatement.setString(2, comboSubGenre.getValue());
                    preparedStatement.setString(3, comboYear.getValue());
                }
                else{
                   // rs = dbConn.searchMediaGS(tfSearch.getText(), comboGenre.getValue(), comboSubGenre.getValue());
                    preparedStatement = conn.prepareStatement("SELECT m.* FROM media_info mi JOIN media m ON mi.media_id = m.id " +
                            "JOIN genres g ON mi.genre_id = g.genre_id JOIN subgenres sg ON mi.subgenre_id = sg.subgenre_id " +
                            "WHERE genre = ? AND subgenre = ? AND (title LIKE '%" +
                            tfSearch.getText() + "%' OR creator LIKE '%" +tfSearch.getText() + "%') GROUP BY m.id");
                    preparedStatement.setString(1, comboGenre.getValue());
                    preparedStatement.setString(2, comboSubGenre.getValue());
                }
            }
            else if(comboYear.getValue() != null){
                preparedStatement = conn.prepareStatement("SELECT m.* FROM media_info mi JOIN media m ON mi.media_id = m.id " +
                        "JOIN genres g ON mi.genre_id = g.genre_id " +
                        "WHERE genre = ? AND m.year = ? AND (title LIKE '%" +
                        tfSearch.getText() + "%' OR creator LIKE '%" +tfSearch.getText() + "%') GROUP BY m.id");
                preparedStatement.setString(1, comboGenre.getValue());
                preparedStatement.setString(2, comboYear.getValue());
            }
            else{
                preparedStatement = conn.prepareStatement("SELECT m.* FROM media_info mi JOIN media m ON mi.media_id = m.id " +
                        "JOIN genres g ON mi.genre_id = g.genre_id " +
                        "WHERE genre = ? AND (title LIKE '%" +
                        tfSearch.getText() + "%' OR creator LIKE '%" +tfSearch.getText() + "%') GROUP BY m.id");
                preparedStatement.setString(1, comboGenre.getValue());
            }
        }
        else if(comboSubGenre.getValue() != null){
            if(comboYear.getValue() != null){
                preparedStatement = conn.prepareStatement("SELECT m.* FROM media_info mi JOIN media m ON mi.media_id = m.id " +
                        "JOIN subgenres sg ON mi.subgenre_id = sg.subgenre_id " +
                        "WHERE subgenre = ? AND m.year = ? AND (title LIKE '%" +
                        tfSearch.getText() + "%' OR creator LIKE '%" +tfSearch.getText() + "%') GROUP BY m.id");
                preparedStatement.setString(1, comboSubGenre.getValue());
                preparedStatement.setString(2, comboYear.getValue());
            }
            else{
                preparedStatement = conn.prepareStatement("SELECT m.* FROM media_info mi JOIN media m ON mi.media_id = m.id " +
                        "JOIN subgenres sg ON mi.subgenre_id = sg.subgenre_id " +
                        "WHERE subgenre = ? AND (title LIKE '%" + tfSearch.getText() + "%' OR creator LIKE '%" +tfSearch.getText()
                        + "%') GROUP BY m.id");
                preparedStatement.setString(1, comboSubGenre.getValue());
            }
        }
        else{
            preparedStatement = conn.prepareStatement("SELECT * FROM media WHERE year = ? AND (title LIKE '%" +
                    tfSearch.getText() + "%' OR creator LIKE '%" +tfSearch.getText() + "%')");
            preparedStatement.setString(1, comboYear.getValue());
        }

        rs = preparedStatement.executeQuery();

        while(rs.next()){
            Blob image = rs.getBlob("image");
            String titleSearch = rs.getString("title");
            String creator = rs.getString("creator");
            int year = rs.getInt("year");
            String editor = rs.getString("editor");
            String cinematographer = rs.getString("cinematographer");

            if(editor == null || cinematographer == null){
                media = new Media(titleSearch, creator, year);
                byte[] byteImage = image.getBytes(1, (int)image.length());
                media.setPicture(byteImage);

                results.add(media);
            }
            else{
                media = new Film(titleSearch, creator, year, editor, cinematographer);
                //Media film = new Film(titleSearch, creator, year, editor, cinematographer);
                byte[] byteImage = image.getBytes(1, (int)image.length());
                media.setPicture(byteImage);

                results.add(media);
            }
        }

//        if(comboSort.getValue() != null){
//            if(comboSort.getValue().equals("Title")){
//               results = mediaList.sortTitle(results);
//            }
//            else{
//                results = mediaList.sortCreator(results);
//            }
//        }

        displayResults(results);
    }

    public void onSortClick(ActionEvent actionEvent) throws IOException {
        MediaList mediaList = new MediaList();
        if(comboSort.getValue().equals("Title")){
            results = mediaList.sortTitle(results);
        }
        else{
            results = mediaList.sortCreator(results);
        }
        displayResults(results);
    }

    public void filterYear(ActionEvent actionEvent) throws IOException {
        for(int i = 0; i < results.size(); i++){
            if(results.get(i).getYearOfRelease() != Integer.parseInt(comboYear.getValue())){
                results.remove(i);
                i--;
            }
        }
        displayResults(results);
    }

    public void filterGenre(ActionEvent actionEvent) throws IOException {

    }

    public void filterSubGenre(ActionEvent actionEvent) {
    }

    public void filterTheme(ActionEvent actionEvent) {

    }

    public static int getId() {
        return id;
    }

    public static void setId(int id) {
        MediaDatabaseController.id = id;
    }

    /** Displays the list of media in the main page
     * @pre: method is called when main page is shown or when client searches for media
     * @post: displays the results of the media on the main page as buttons
     * @param results */
    public void displayResults(ArrayList<Media> results) throws IOException{
        //clears existing results on the page
        clearResults();
        //checks if there are any results, if not, display message that there are no results
        if(results.size() > 0){
            for(int i = 0; i < results.size(); i++){
                //creates new button with string which contains text of the main media information that will be displayed
                String btnText = results.get(i).getTitle() + "\n" + results.get(i).getCreator() + "\n" + results.get(i).getYearOfRelease();
                Button btn = new Button(btnText);
                btn.setPrefSize(1050,110);
                //gets the byte array from the media object
                ByteArrayInputStream bais = new ByteArrayInputStream(results.get(i).getPicture());
                //converts byte array to BufferedImage and to JavaFX image
                BufferedImage bufferedImage = ImageIO.read(bais);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                //displays the image onto a created ImageView
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(110);
                imageView.setFitWidth(80);
                //sets the button's graphic to the created ImageView
                btn.setGraphic(imageView);
                btn.setAlignment(Pos.CENTER_LEFT);
                //adds the result button to the vBox (display container)
                vBox.getChildren().add(btn);

                //sets button an onAction so the user can click and be directed to a new page
                btn.setOnAction(e -> {
                    try{ displayMediaInformation(e, btnText);}
                    catch(IOException | SQLException ex){
                        ex.printStackTrace();
                    } });
            }
        }
        else{
            //adds a label to the vBox to display there are no results
            vBox.getChildren().add(new Label("No results found"));
        }
    }

    private void displayMediaInformation(ActionEvent actionEvent, String text) throws IOException, SQLException {
        String btnText = text;
        String title = "";
        String creator = "";

        int index = 0;
        for(int i = 0; i < btnText.length(); i++){
            if(text.substring(i, i+1).equals("\n")){
                if(index > 0){
                    creator = btnText.substring(index+1, i);
                }
                else{
                    title = btnText.substring(index, i);
                }
                index = i;
            }
        }
        int year = Integer.parseInt(btnText.substring(index+1));

        media.setTitle(title);
        media.setCreator(creator);
        media.setYearOfRelease(year);

        setId(dbConn.findID(media));

        Parent mediaDatabaseControl = FXMLLoader.load(getClass().getResource("mediaInformation-view.fxml"));
        Scene scene = new Scene(mediaDatabaseControl);
        Stage stage = (Stage)((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setTitle("Media Information");
        scene.getRoot().setStyle("-fx-font-family: SansSerif");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Clears the current media displayed on the main page
     * @pre: there are currently results shown on the page
     * @post: currently displayed results will be cleared
     */
    public void clearResults(){
        //create ArrayList that will hold the child nodes that'll be removed
        ArrayList<Node> nodes = new ArrayList<>();

        //loops through the nodes in the vBox
        for(Node node: vBox.getChildren()){
            nodes.add(node);
        }
        //remove all the nodes in the vBox
        vBox.getChildren().removeAll(nodes);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        comboSort.setItems(sortList);
        genreList = FXCollections.observableArrayList();
        yearList = FXCollections.observableArrayList();
        subgenreList = FXCollections.observableArrayList();
        themeList = FXCollections.observableArrayList();

        try {
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = conn.prepareStatement("SELECT genre FROM genres");
            rs = ps.executeQuery();

            while(rs.next()){
                genreList.add(rs.getString("genre"));
            }
            genreList.add("None");

            ps = conn.prepareStatement("SELECT subgenre FROM subgenres");
            rs = ps.executeQuery();

            while(rs.next()){
                subgenreList.add(rs.getString("subgenre"));
            }
            subgenreList.add("None");

            ps = conn.prepareStatement("SELECT theme FROM themes");
            rs = ps.executeQuery();

            while(rs.next()){
                themeList.add(rs.getString("theme"));
            }

            themeList.add("None");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        for(int i = 1800; i <= 2025; i+=5){
            yearList.addAll(i);
        }
        comboYear.setItems(yearList);
        comboGenre.setItems(genreList);
        comboSubGenre.setItems(subgenreList);
        comboTheme.setItems(themeList);


        //initialises 'results' ArrayList to store media
        results = new ArrayList<>();
        try {
            //get all media/data from the 'media' table in the SQL Database
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM media");
            ResultSet rs = preparedStatement.executeQuery();

            while(rs.next()){
                //gets information of media from ResultSet
                Blob image = rs.getBlob("image");
                String titleSearch = rs.getString("title");
                String creator = rs.getString("creator");
                int year = rs.getInt("year");

                //Creates Media object from information retrieved from the database
                media = new Media(titleSearch, creator, year);
                //gets blob image as a byte array and sets it to its associated media object
                byte[] byteImage = image.getBytes(1, (int)image.length());
                media.setPicture(byteImage);

                //adds object to the results ArrayList
                results.add(media);

            }
            displayResults(results);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onItemClickClose(ActionEvent actionEvent) {
        Platform.exit();
    }


}