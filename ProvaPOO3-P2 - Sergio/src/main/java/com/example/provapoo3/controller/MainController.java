package com.example.provapoo3.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainController {

    @FXML
    private void initialize() {
        System.out.println("HelloController inicializado. Tela inicial pronta!");
    }

    @FXML
    private void handleCadastroCursos(ActionEvent event) {
        carregarNovaTela("/com/example/provapoo3/view/cadastro-curso-view.fxml", "Cadastro de Cursos");
    }

    @FXML
    private void handleCadastroDisciplinas(ActionEvent event) {
        carregarNovaTela("/com/example/provapoo3/view/cadastro-disciplina-view.fxml", "Cadastro de Disciplinas");
    }

    @FXML
    private void handleCadastroProfessores(ActionEvent event) {
        carregarNovaTela("/com/example/provapoo3/view/cadastro-professor-view.fxml", "Cadastro de Professores");
    }

    @FXML
    private void handleCadastroTurmas(ActionEvent event) {
        carregarNovaTela("/com/example/provapoo3/view/cadastro-turma-view.fxml", "Cadastro de Turmas");
    }

    private void carregarNovaTela(String fxmlPath, String title) {
        try {
            URL fxmlLocation = getClass().getResource(fxmlPath);

            if (fxmlLocation == null) {
                System.err.println("Erro: Não foi possível encontrar o FXML em: " + fxmlPath);
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(fxmlLoader.load());

            Stage newStage = new Stage();
            newStage.setTitle(title);
            newStage.setScene(scene);

            // --- LÓGICA DE AJUSTE AO MONITOR ---
            // 1. Pega as dimensões visuais do monitor principal (desconta a barra de tarefas)
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // 2. Define um tamanho máximo (ex: 90% da tela)
            double maxHeight = screenBounds.getHeight() * 0.95;
            double maxWidth = screenBounds.getWidth() * 0.95;

            // 3. Aplica o tamanho apenas se o conteúdo for maior que a tela
            // (O sizeToScene faz o palco calcular o tamanho necessário)
            newStage.sizeToScene();

            if (newStage.getHeight() > maxHeight) {
                newStage.setHeight(maxHeight);
            }
            if (newStage.getWidth() > maxWidth) {
                newStage.setWidth(maxWidth);
            }

            // 4. Centraliza a janela
            newStage.setX((screenBounds.getWidth() - newStage.getWidth()) / 2);
            newStage.setY((screenBounds.getHeight() - newStage.getHeight()) / 2);

            newStage.show();

        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }
}