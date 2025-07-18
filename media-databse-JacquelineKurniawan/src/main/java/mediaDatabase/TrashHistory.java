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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Author: Jacqueline Kurniawan
 */
public class TrashHistory implements Initializable {
    @FXML
    TableView tableView;
    @FXML
    TableColumn tcTime, tcCreator, tcYear, tcTitle;
    @FXML
    AnchorPane anchorPane = new AnchorPane();

    Media media = new Media();

    ObservableList<Media> ol;
    Connection conn;

    int index = -1;

    public TrashHistory() throws SQLException, IOException {
        conn = DBConnection.connectDB();
    }

    @FXML
    public void onItemClickClose(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void itemMainClick(ActionEvent actionEvent) throws IOException {
        Parent mediaDatabaseControl = FXMLLoader.load(getClass().getResource("mainPage-view.fxml"));
        Scene scene = new Scene(mediaDatabaseControl);
        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setTitle("Main Page");
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

    public void itemEditFilm(ActionEvent actionEvent) throws IOException {
        Parent mediaDatabaseControl = FXMLLoader.load(getClass().getResource("addFilm-view.fxml"));
        Scene scene = new Scene(mediaDatabaseControl);
        Stage stage = (Stage) anchorPane.getScene().getWindow();
        stage.setTitle("Edit Film");
        scene.getRoot().setStyle("-fx-font-family: SansSerif");
        stage.setScene(scene);
        stage.show();
    }

    public void getSelected(MouseEvent mouseEvent) throws IOException {
        index = tableView.getSelectionModel().getFocusedIndex();
        if(index <= -1){
            return;
        }
        media = new Media(tcTitle.getCellData(index).toString(), tcCreator.getCellData(index).toString(), Integer.parseInt(tcYear.getCellData(index).toString()));
    }

    public void recoverMedia(ActionEvent actionEvent) throws SQLException {
        index = tableView.getSelectionModel().getFocusedIndex();
        if(index <= -1){
            return;
        }
        PreparedStatement ps = null;

        ps = conn.prepareStatement("SELECT * FROM trash_history WHERE title = ? AND creator = ? AND year = ?");
        ps.setString(1, tcTitle.getCellData(index).toString());
        ps.setString(2, tcCreator.getCellData(index).toString());
        ps.setInt(3, Integer.parseInt(tcYear.getCellData(index).toString()));

        ResultSet rs = ps.executeQuery();

        while(rs.next()){
            int id = rs.getInt("id");
            Blob image = rs.getBlob("image");
            String title = rs.getString("title");
            String creator = rs.getString("creator");
            int year = rs.getInt("year");
            String editor = rs.getString("editor");
            String cinematographer = rs.getString("cinematographer");
            String note = rs.getString("note");

            if(editor == null){
                ps = conn.prepareStatement("INSERT INTO media (id, image, title, creator, year, note) VALUES (?,?,?,?,?,?)");
                ps.setInt(1, id);
                ps.setBlob(2, image);
                ps.setString(3, title);
                ps.setString(4, creator);
                ps.setInt(5, year);
                ps.setString(6, note);
                ps.execute();
            }
            else{
                ps = conn.prepareStatement("INSERT INTO media (id, image, title, creator, year, editor, cinematographer, note) " +
                        "VALUES (?,?,?,?,?,?,?,?)");
                ps.setInt(1, id);
                ps.setBlob(2, image);
                ps.setString(3, title);
                ps.setString(4, creator);
                ps.setInt(5, year);
                ps.setString(6, editor);
                ps.setString(7, cinematographer);
                ps.setString(8, note);
                ps.execute();
            }
            deleteFromTable();
        }
    }

    public void deleteMedia(ActionEvent actionEvent) throws SQLException, IOException {
        index = tableView.getSelectionModel().getFocusedIndex();
        if(index <= -1){
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
        alert.getDialogPane().setStyle("-fx-font-family: SansSerif");
        alert.setTitle("Deleting Media");
        alert.setHeaderText("Are you sure you want to delete this media?");
        alert.setContentText("The selected media will be permanently deleted. You will not be able to recover it.");

        ButtonType result = alert.showAndWait().orElse(ButtonType.NO);
        if(ButtonType.NO.equals(result)){
            return;
        }

        PreparedStatement ps = conn.prepareStatement("SELECT id FROM trash_history WHERE title = ? AND creator = ? AND year = ?");
        ps.setString(1, tcTitle.getCellData(index).toString());
        ps.setString(2, tcCreator.getCellData(index).toString());
        ps.setInt(3, Integer.parseInt(tcYear.getCellData(index).toString()));

        ResultSet rs = ps.executeQuery();

        int id = 0;
        while(rs.next()){
            id = rs.getInt("id");
        }

        ps = conn.prepareStatement("DELETE FROM media_info WHERE media_id = ?");
        ps.setInt(1, id);
        ps.executeUpdate();

        deleteFromTable();
    }

    public void deleteFromTable() throws SQLException {
        index = tableView.getSelectionModel().getFocusedIndex();

        PreparedStatement ps = conn.prepareStatement("DELETE FROM trash_history WHERE title = ? AND creator = ? AND year = ?");
        ps.setString(1, tcTitle.getCellData(index).toString());
        ps.setString(2, tcCreator.getCellData(index).toString());
        ps.setInt(3, Integer.parseInt(tcYear.getCellData(index).toString()));

        ps.execute();
        updateTable();

    }

    public void updateTable(){
        ArrayList<Media> media = new ArrayList<>();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("SELECT title, creator, year, time FROM trash_history");

            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                media.add(new Media(rs.getString("title"), rs.getString("creator"), rs.getInt("year"), rs.getTimestamp("time")));
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        ol = FXCollections.observableArrayList(media);

        tcTitle.setCellValueFactory(new PropertyValueFactory<Media, String>("title"));
        tcCreator.setCellValueFactory(new PropertyValueFactory<Media, String>("creator"));
        tcYear.setCellValueFactory(new PropertyValueFactory<Media, String>("yearOfRelease"));
        tcTime.setCellValueFactory(new PropertyValueFactory<Media, Timestamp>("time"));

        tableView.setItems(ol);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        updateTable();
    }
}
