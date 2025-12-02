package com.example.provapoo3.controller;

import com.example.provapoo3.dao.CursoDAO;
import com.example.provapoo3.model.Curso;
import com.example.provapoo3.utils.AlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class CursoController {

    @FXML private TextField txtId;
    @FXML private TextField txtNome;
    @FXML private TextField txtCargaHoraria;
    @FXML private TableView<Curso> tableViewCursos;
    @FXML private TableColumn<Curso, Long> colId;
    @FXML private TableColumn<Curso, String> colNome;
    @FXML private TableColumn<Curso, Number> colCargaHoraria;

    private CursoDAO cursoDAO;
    private ObservableList<Curso> listaCursos;

    @FXML
    public void initialize() {
        cursoDAO = new CursoDAO();
        listaCursos = FXCollections.observableArrayList();

        // Configura as colunas da TableView para exibir as propriedades de Curso
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCargaHoraria.setCellValueFactory(new PropertyValueFactory<>("cargaHoraria"));

        tableViewCursos.setItems(listaCursos);

        // Adiciona um listener para preencher os campos de texto quando um item da tabela é selecionado
        tableViewCursos.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showCursoDetails(newValue));

        loadCursos();
    }

    // Exibe os detalhes de um curso nos campos de texto
    private void showCursoDetails(Curso curso) {
        if (curso != null) {
            txtId.setText(String.valueOf(curso.getId()));
            txtNome.setText(curso.getNome());
            txtCargaHoraria.setText(String.valueOf(curso.getCargaHoraria()));
        } else {
            handleClear();
        }
    }

    @FXML
    private void handleNew() {
        handleClear();
    }

    @FXML
    private void handleSave() {
        try {
            String nome = txtNome.getText();
            int cargaHoraria = Integer.parseInt(txtCargaHoraria.getText());

            if (nome.isEmpty()) {
                AlertUtil.showErrorAlert("Erro de Validação", "O campo 'Nome' não pode estar vazio.");
                return;
            }

            Curso novoCurso = new Curso(nome, cargaHoraria);
            cursoDAO.create(novoCurso);
            AlertUtil.showInformationAlert("Sucesso", "Curso salvo com sucesso!");
            loadCursos();
            handleClear();
        } catch (NumberFormatException e) {
            AlertUtil.showErrorAlert("Erro de Entrada", "Carga Horária deve ser um número válido.");
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Erro", "Erro ao salvar curso: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            if (txtId.getText().isEmpty()) {
                AlertUtil.showWarningAlert("Seleção Necessária", "Selecione um curso na tabela para atualizar.");
                return;
            }
            Long id = Long.valueOf(txtId.getText());
            String nome = txtNome.getText();
            int cargaHoraria = Integer.parseInt(txtCargaHoraria.getText());

            if (nome.isEmpty()) {
                AlertUtil.showErrorAlert("Erro de Validação", "O campo 'Nome' não pode estar vazio.");
                return;
            }

            Curso cursoToUpdate = cursoDAO.findById(id);
            if (cursoToUpdate != null) {
                cursoToUpdate.setNome(nome);
                cursoToUpdate.setCargaHoraria(cargaHoraria);
                cursoDAO.update(cursoToUpdate);
                AlertUtil.showInformationAlert("Sucesso", "Curso atualizado com sucesso!");
                loadCursos();
                handleClear();
            } else {
                AlertUtil.showWarningAlert("Não Encontrado", "Curso com ID " + id + " não encontrado.");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showErrorAlert("Erro de Entrada", "ID e Carga Horária devem ser números válidos.");
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Erro", "Erro ao atualizar curso: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        try {
            if (txtId.getText().isEmpty()) {
                AlertUtil.showWarningAlert("Seleção Necessária", "Selecione um curso na tabela para excluir.");
                return;
            }
            Long id = Long.valueOf(txtId.getText());
            Curso cursoToDelete = cursoDAO.findById(id);
            if (cursoToDelete != null) {
                cursoDAO.delete(cursoToDelete);
                AlertUtil.showInformationAlert("Sucesso", "Curso excluído com sucesso!");
                loadCursos();
                handleClear();
            } else {
                AlertUtil.showWarningAlert("Não Encontrado", "Curso com ID " + id + " não encontrado.");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showErrorAlert("Erro de Entrada", "ID deve ser um número válido para exclusão.");
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Erro", "Erro ao excluir curso: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        txtId.clear();
        txtNome.clear();
        txtCargaHoraria.clear();
        tableViewCursos.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleListAll() {
        loadCursos();
    }

    private void loadCursos() {
        listaCursos.clear();
        List<Curso> cursos = cursoDAO.findAll();
        listaCursos.addAll(cursos);
    }
}