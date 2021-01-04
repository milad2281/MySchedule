package com.milad200281.github;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Callback;

public class AppController {

    private List<TodoItem> todoItems;
    @FXML
    private ListView<TodoItem> todoListView;
    @FXML
    private TextArea itemDetailsTextArea;
    @FXML
    private Label deadlineLabel;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ContextMenu listContextMenu;
    @FXML
    private ToggleButton filterToggleButton;

    private FilteredList<TodoItem> filteredList;
    SortedList<TodoItem> sortedList;

    private Predicate<TodoItem> wantAllItems;
    private Predicate<TodoItem> wantTodaysItems;

    public void initialize() {
        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        MenuItem editMenuItem = new MenuItem("Edit");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });

        editMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                editItem(item);
            }
        });

        listContextMenu.getItems().addAll(editMenuItem, deleteMenuItem);

        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
            @Override
            public void changed(ObservableValue<? extends TodoItem> observable, TodoItem oldValue, TodoItem newValue) {
                if (newValue != null) {
                    TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                    itemDetailsTextArea.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                    deadlineLabel.setText(df.format(item.getDeadline()));
                }
            }
        }
        );

        wantAllItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem todoItem) {
                return true;
            }
        };
        wantTodaysItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem todoItem) {
                return todoItem.getDeadline().equals(LocalDate.now());
            }
        };

        sorting();

        todoListView.getSelectionModel()
                .setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel()
                .selectFirst();

        todoListView.setCellFactory(
                new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> param
            ) {
                ListCell<TodoItem> cell = new ListCell<TodoItem>() {
                    @Override
                    protected void updateItem(TodoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(item.getShortDescription());
                            if (item.getDeadline().isBefore(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.RED);
                            } else if (item.getDeadline().equals(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.BROWN);
                            }
                        }
                    }
                };
                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if (isNowEmpty) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu);
                            }
                        }
                );
                return cell;
            }
        }
        );
    }

    public void sorting() {
        filteredList = new FilteredList<TodoItem>(TodoData.getInstance().getTodoItems(), wantAllItems);
        sortedList = new SortedList<TodoItem>(filteredList, new Comparator<TodoItem>() {
            @Override
            public int compare(TodoItem o1, TodoItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());
            }
        });
        todoListView.setItems(sortedList);
    }

    @FXML

    public void showNewItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());

        dialog.setTitle("Add New Todo Item");
        dialog.setHeaderText("Use this dialog to create a new todo item");

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        final DialogController controller = fxmlLoader.getController();
        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btOk.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    if (controller.validation()) {
                        event.consume();
                    }
                }
        );

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            TodoItem newItem = controller.proccessResults();
            todoListView.getSelectionModel().select(newItem);
            System.out.println("OK pressed");

        } else {
            System.out.println("Cancel pressed");
        }

    }

    @FXML
    public void handleKeyPressedDelete(KeyEvent keyEvent) {
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                deleteItem(selectedItem);
            }
        }
    }

    @FXML
    public void handleKeyPressed(KeyEvent keyEvent) throws IOException {
            if (keyEvent.isControlDown() && (keyEvent.getCode() == KeyCode.X)) {
                System.out.println("Ctrl+X");
                handleExportMSV1();
            }
            if (keyEvent.isControlDown() && (keyEvent.getCode() == KeyCode.I)) {
                System.out.println("Ctrl+I");
                handleImportMSV1();
            }
            if (keyEvent.isControlDown() && (keyEvent.getCode() == KeyCode.M)) {
                System.out.println("Ctrl+M");
                handleMerge();
            }
            if (keyEvent.isControlDown() && (keyEvent.getCode() == KeyCode.N)) {
                System.out.println("Ctrl+N");
                showNewItemDialog();
            }
        }
    

    @FXML
    public void handleClickListView() {
        TodoItem item = todoListView.getSelectionModel().getSelectedItem();
        itemDetailsTextArea.setText(item.getDetails());
        deadlineLabel.setText(item.getDeadline().toString());
    }

    public void deleteItem(TodoItem item) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Todo Item");
        alert.setHeaderText("Delete item: " + item.getShortDescription());
        alert.setContentText("Are you sure? Press OK to confirm, or cancel to back out.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TodoData.getInstance().deleteTodoItem(item);
        }

    }

    public void editItem(TodoItem oldItem) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());

        dialog.setTitle("Edit Todo Item");
        dialog.setHeaderText("Use this dialog to edit a todo item");

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));

        try {

            dialog.getDialogPane().setContent(fxmlLoader.load());

        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        final DialogController controller = fxmlLoader.getController();
        controller.proccessEdit(oldItem);

        final Button btApply = (Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY);
        btApply.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    if (controller.validation()) {
                        event.consume();
                    }
                }
        );

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.APPLY) {
            TodoData.getInstance().deleteTodoItem(oldItem);
            todoListView.getSelectionModel().select(controller.proccessResults());
            System.out.println("Apply pressed");
        } else {
            System.out.println("Cancel pressed");
        }

    }

    @FXML
    public void handleFilterButton() {
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();

        if (filterToggleButton.isSelected()) {
            filteredList.setPredicate(wantTodaysItems);
            if (filteredList.isEmpty()) {
                itemDetailsTextArea.clear();
                deadlineLabel.setText("");
            } else if (filteredList.contains(selectedItem)) {
                todoListView.getSelectionModel().select(selectedItem);
            } else {
                todoListView.getSelectionModel().selectFirst();
            }
        } else {
            filteredList.setPredicate(wantAllItems);
            todoListView.getSelectionModel().select(selectedItem);
        }
    }

    @FXML
    public void handleExportMSV1() throws IOException {
        Path savePath;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export File as MSV1");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MSV1", "*.msv1"));

        File file = chooser.showSaveDialog(mainBorderPane.getScene().getWindow());
        if (file != null) {
            savePath = file.toPath();
            System.out.println(savePath);
            TodoData.getInstance().exportTodoItemsMSV1(savePath);
        } else {
            System.out.println("Chooser was cancelled");
        }

    }

    @FXML
    public void handleExportCSV() throws IOException {
        Path savePath;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export File As CSV");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = chooser.showSaveDialog(mainBorderPane.getScene().getWindow());
        if (file != null) {
            savePath = file.toPath();
            System.out.println(savePath);
            TodoData.getInstance().exportTodoItemsCSV(savePath);
        } else {
            System.out.println("Chooser was cancelled");
        }

    }

    @FXML
    public void handleImportMSV1() throws IOException {
        Path loadPath;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select MSV1 file");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MSV1", "*.msv1"));
        File file = chooser.showOpenDialog(mainBorderPane.getScene().getWindow());
        if (file != null) {
        loadPath = file.toPath();
        System.out.println(loadPath);
        TodoData.getInstance().importTodoItemsMSV1(loadPath);
        sorting();
        }else {
            System.out.println("Chooser was cancelled");
        }

    }

    @FXML
    public void handleMerge() throws IOException {
        Path loadPath;
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(mainBorderPane.getScene().getWindow());
        loadPath = file.toPath();
        System.out.println(loadPath);
        TodoData.getInstance().MergeTodoItemsMSV1(loadPath);

    }

    @FXML
    public void handleAbout() {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("About MySchdule");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("about.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }
        //DialogController controller = fxmlLoader.getController();
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Optional<ButtonType> result = dialog.showAndWait();

    }

    @FXML
    public void handleExit() {
        Platform.exit();
    }
}
