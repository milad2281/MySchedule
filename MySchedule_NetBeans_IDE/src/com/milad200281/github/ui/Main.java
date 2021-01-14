/**
 * MySchedule Author: Milad Mobini
 * Last Modified: 2021/1 GitHub:
 * https://github.com/milad200281/MySchedule
 * License available at legal folder
 */
package com.milad200281.github.ui;

import com.milad200281.github.commen.Option;
import com.milad200281.github.commen.TodoData;
import java.io.IOException;
import java.util.Optional;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * This the main class the starts the main stage
 *
 * @author Milad Mobini
 * @version 1.1.15
 * @since JDK 1.8u271
 */
public class Main extends Application {

    /**
     * runs at the start of the application
     *
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("application.fxml"));
        //this line will choose the template
        setUserAgentStylesheet(STYLESHEET_MODENA);
        primaryStage.setTitle("MySchedule");
        primaryStage.getIcons().add(new Image("/com/milad200281/github/icons/MyScheduleLogo.png"));
        primaryStage.setScene(new Scene(root, 900, 500));
        primaryStage.show();
        popUpNotification();
    }

    /**
     * initialize the program
     *
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        try {
            Option.getInstance().loadOption();
            TodoData.getInstance().loadTodoItems();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Main method
     *
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * runs when the application is closed
     *
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        try {
            Option.getInstance().saveOption();
            TodoData.getInstance().storeTodoItems();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method handle the pop-up notification at the opening stage of the
     * application
     */
    public void popUpNotification() {
        if (Option.getInstance().isPopUp()) {
            if (TodoData.getInstance().getTodoItems().size() >= 1) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("MySchedule");
                alert.setHeaderText("Welcome Back.");
                String strOne = "There ";
                String strTwo = "There ";
                int numOne = Option.getInstance().getTodayItems();
                int numTwo = Option.getInstance().getTomorrowItems();
                switch (numOne) {
                    case 0:
                        strOne += "is no item due to today.";
                        break;
                    case 1:
                        strOne += "is 1 item due to today.";
                        break;
                    default:
                        strOne += "are " + numOne + " items due to today.";
                        break;
                }
                switch (numTwo) {
                    case 0:
                        strTwo += "is no item due to tomorrow.";
                        break;
                    case 1:
                        strTwo += "is 1 item due to tomorrow.";
                        break;
                    default:
                        strTwo += "are " + numTwo + " item due to tomorrow.";
                        break;
                }
                alert.setContentText(strOne + "\n" + strTwo);
                Optional<ButtonType> result = alert.showAndWait();
            }
        }
    }
}
