package prototyopage.Controllers;

import DB.SejourDB.Sejour;
import DB.SejourDB.SejourDAO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import prototyopage.Context;
import prototyopage.MainApp;

import java.util.ArrayList;
import java.util.List;

public class RechercheController {
    private MainApp mainApp;

    // user
    @FXML
    private VBox userBox;
    @FXML
    private javafx.scene.text.Text userIsTravelerText;
    @FXML
    private javafx.scene.text.Text userNameText;

    // search part
    @FXML
    private TextField searchBar;

    @FXML
    private VBox boxSejour;

    @FXML
    private ScrollPane scrollPane;

    private SejourDAO sejourDao = new SejourDAO();

    private ArrayList<Sejour> listDisplayedSejour =new ArrayList<>();
    private ArrayList<Sejour> listSejourToDisplay =new ArrayList<>();
    private int startSejour=0;
    private int endSejour=0;

    private String lastSearch="";
    // end search part


    private ArrayList<Thread> threads=new ArrayList<>();

    public synchronized VBox getBoxSejour(){
        return this.boxSejour;
    }

    public synchronized void addToBoxSejour(Button button){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                boxSejour.getChildren().add(button);
            }
        });

    }

    public synchronized ArrayList<Sejour> getListDisplayedSejour(){
        return this.listDisplayedSejour;
    }

    public synchronized void addToListDisplayedSejour(Sejour sejour){
        this.listDisplayedSejour.add(sejour);
//        if (listDisplayedSejour.size()==startSejour){
//            addSejourToBox();
//
//            System.out.println(listDisplayedSejour.size());
//
//        }

    }



    public void setUserBox(){
        if (Context.getUser()!=null){
            userNameText.setText(Context.getUser().getFirstName());
            if (Context.getUser().isHost()){
                userIsTravelerText.setText("Hote");
            }
            else{
                userIsTravelerText.setText("Voyageur");
            }
            userBox.setVisible(true);
        }
        else {
            userBox.setVisible(false);
        }
    }

    public void init() {
        search();
    }

    private void addSejourToBox(ArrayList<Sejour> list){
        Integer lenght = list.size();

        for (int i=0;i<lenght;i=i+25) {
            List<Sejour> sublist;
            if (i+25<lenght) {
                sublist = list.subList(i, i + 25);
            }
            else {
                sublist = list.subList(i, lenght);
            }
            Thread t = new Thread(new SearchRunnable(sublist, this, this.mainApp));
            t.start();
            threads.add(t);
        }
    }

    private void addSejourToBox(){
        Integer lenght = listSejourToDisplay.size();
        if (lenght<endSejour){
            endSejour=lenght;
        }
        for (int i=startSejour;i<endSejour;i=i+25) {

            List<Sejour> sublist;
            if (i+25<lenght) {
                sublist = listSejourToDisplay.subList(i, i + 25);
            }
            else {
                sublist = listSejourToDisplay.subList(i, lenght);
            }
            Thread t = new Thread(new SearchRunnable(sublist, this, this.mainApp));
            t.start();
            threads.add(t);
        }
        startSejour=endSejour;
        endSejour+=100;
    }
    @FXML
    private void search() {

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (searchBar.getLength() > 1) {
                    for (int i=0;i<threads.size();i++){
                        threads.get(i).interrupt();
                    }
                    threads.clear();
                    listDisplayedSejour.clear();
                    boxSejour.getChildren().clear();
                    lastSearch=searchBar.getText();
                    addSejourToBox(sejourDao.searchSejourByField("Name",searchBar.getText()));
                    addSejourToBox(sejourDao.searchSejourByField("Location",searchBar.getText()));
                    addSejourToBox(sejourDao.searchSejourByField("Description",searchBar.getText()));
                    addSejourToBox(sejourDao.searchSejourByField("DateBegin",searchBar.getText()));
                    addSejourToBox(sejourDao.searchSejourByField("DateEnd",searchBar.getText()));
                } else if (lastSearch=="" || lastSearch.length()>1){
                    listDisplayedSejour.clear();
                    boxSejour.getChildren().clear();
                    startSejour=0;
                    endSejour=100;
                    lastSearch=" ";
                    listSejourToDisplay=sejourDao.searchSejourByField("Name","");
                    addSejourToBox();
                }
                return null;


            }
        };
        task.run();
    }

    @FXML
    private void backToHome(){
        this.mainApp.backView();
    }

    public void setMainApp(MainApp mainApp)
    {
        this.mainApp = mainApp;
    }


    public void openSejour() {
        mainApp.showVoyagerSejourDetails();
    }
}

