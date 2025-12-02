package com.example.provapoo3.controller;

import com.example.provapoo3.dao.DisciplinaDAO;
import com.example.provapoo3.dao.ProfessorDAO;
import com.example.provapoo3.model.Disciplina;
import com.example.provapoo3.model.Professor;
import com.example.provapoo3.utils.AlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProfessorController {

    @FXML private TextField txtId;
    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private TextField txtFormacao;
    @FXML private ComboBox<Disciplina> cmbDisciplina;
    @FXML private Label lblDisciplinasAssociadas;
    @FXML private TableView<Professor> tableViewProfessores;
    @FXML private TableColumn<Professor, Long> colId;
    @FXML private TableColumn<Professor, String> colNome;
    @FXML private TableColumn<Professor, String> colEmail;
    @FXML private TableColumn<Professor, String> colFormacao;
    @FXML private TableColumn<Professor, String> colDisciplinas;

    private ProfessorDAO professorDAO;
    private DisciplinaDAO disciplinaDAO;

    private ObservableList<Professor> listaProfessores;
    private ObservableList<Disciplina> listaDisciplinas;
    private Set<Disciplina> disciplinasAssociadas;

    @FXML
    public void initialize() {
        professorDAO = new ProfessorDAO();
        disciplinaDAO = new DisciplinaDAO();

        listaProfessores = FXCollections.observableArrayList();
        listaDisciplinas = FXCollections.observableArrayList();
        disciplinasAssociadas = new HashSet<>();

        cmbDisciplina.setItems(listaDisciplinas);
        cmbDisciplina.setConverter(new javafx.util.StringConverter<Disciplina>() {
            @Override
            public String toString(Disciplina disciplina) {
                return disciplina != null ? disciplina.getNome() : "";
            }
            @Override
            public Disciplina fromString(String string) {
                return null;
            }
        });

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colFormacao.setCellValueFactory(new PropertyValueFactory<>("formacao"));
        colDisciplinas.setCellValueFactory(cellData -> {
            String nomes = cellData.getValue().getDisciplinas().stream()
                    .map(Disciplina::getNome)
                    .collect(Collectors.joining(", "));
            return new javafx.beans.property.SimpleStringProperty(nomes);
        });

        tableViewProfessores.setItems(listaProfessores);

        tableViewProfessores.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> showProfessorDetails(newVal));

        loadData();
    }

    private void showProfessorDetails(Professor professor) {
        if (professor != null) {
            txtId.setText(String.valueOf(professor.getId()));
            txtNome.setText(professor.getNome());
            txtEmail.setText(professor.getEmail());
            txtFormacao.setText(professor.getFormacao());

            disciplinasAssociadas.clear();
            disciplinasAssociadas.addAll(professor.getDisciplinas());
            updateDisciplinasAssociadasLabel();
        } else {
            handleClear();
        }
    }

    @FXML
    private void handleAddDisciplina() {
        Disciplina selectedDisciplina = cmbDisciplina.getSelectionModel().getSelectedItem();
        if (selectedDisciplina != null) {
            if (!disciplinasAssociadas.contains(selectedDisciplina)) {
                disciplinasAssociadas.add(selectedDisciplina);
                updateDisciplinasAssociadasLabel();
            } else {
                AlertUtil.showWarningAlert("Duplicidade", "Disciplina já associada a este professor.");
            }
        } else {
            AlertUtil.showWarningAlert("Seleção Necessária", "Selecione uma disciplina para adicionar.");
        }
    }

    @FXML
    private void handleRemoveDisciplina() {
        Disciplina selectedDisciplina = cmbDisciplina.getSelectionModel().getSelectedItem();
        if (selectedDisciplina != null) {
            if (disciplinasAssociadas.remove(selectedDisciplina)) {
                updateDisciplinasAssociadasLabel();
            } else {
                AlertUtil.showWarningAlert("Não Encontrado", "Disciplina não está na lista de associadas.");
            }
        } else {
            AlertUtil.showWarningAlert("Seleção Necessária", "Selecione uma disciplina para remover.");
        }
    }

    private void updateDisciplinasAssociadasLabel() {
        String nomes = disciplinasAssociadas.stream()
                .map(Disciplina::getNome)
                .collect(Collectors.joining(", "));
        lblDisciplinasAssociadas.setText("Disciplinas Associadas: " + nomes);
    }

    @FXML
    private void handleNew() {
        handleClear();
    }

    @FXML
    private void handleSave() {
        try {
            String nome = txtNome.getText();
            String email = txtEmail.getText();
            String formacao = txtFormacao.getText();

            if (nome.isEmpty() || email.isEmpty() || formacao.isEmpty()) {
                AlertUtil.showErrorAlert("Erro de Validação", "Todos os campos são obrigatórios.");
                return;
            }

            Professor novoProfessor = new Professor(nome, email, formacao);
            novoProfessor.setDisciplinas(disciplinasAssociadas);
            professorDAO.create(novoProfessor);
            AlertUtil.showInformationAlert("Sucesso", "Professor salvo com sucesso!");
            loadProfessores();
            handleClear();
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Erro", "Erro ao salvar professor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            Long id = Long.valueOf(txtId.getText());
            String nome = txtNome.getText();
            String email = txtEmail.getText();
            String formacao = txtFormacao.getText();

            if (nome.isEmpty() || email.isEmpty() || formacao.isEmpty()) {
                AlertUtil.showErrorAlert("Erro de Validação", "Todos os campos são obrigatórios.");
                return;
            }

            Professor professorToUpdate = professorDAO.findById(id);
            if (professorToUpdate != null) {
                professorToUpdate.setNome(nome);
                professorToUpdate.setEmail(email);
                professorToUpdate.setFormacao(formacao);
                professorToUpdate.setDisciplinas(disciplinasAssociadas);
                professorDAO.update(professorToUpdate);
                AlertUtil.showInformationAlert("Sucesso", "Professor atualizado com sucesso!");
                loadProfessores();
                handleClear();
            } else {
                AlertUtil.showWarningAlert("Não Encontrado", "Professor com ID " + id + " não encontrado.");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showErrorAlert("Erro de Entrada", "ID deve ser um número válido.");
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Erro", "Erro ao atualizar professor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        try {
            Long id = Long.valueOf(txtId.getText());
            Professor professorToDelete = professorDAO.findById(id);
            if (professorToDelete != null) {
                professorDAO.delete(professorToDelete);
                AlertUtil.showInformationAlert("Sucesso", "Professor excluído com sucesso!");
                loadProfessores();
                handleClear();
            } else {
                AlertUtil.showWarningAlert("Não Encontrado", "Professor com ID " + id + " não encontrado.");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showErrorAlert("Erro de Entrada", "ID deve ser um número válido para exclusão.");
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Erro", "Erro ao excluir professor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        txtId.clear();
        txtNome.clear();
        txtEmail.clear();
        txtFormacao.clear();
        cmbDisciplina.getSelectionModel().clearSelection();
        disciplinasAssociadas.clear();
        updateDisciplinasAssociadasLabel();
        tableViewProfessores.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleListAll() {
        loadProfessores();
    }

    private void loadData() {
        loadDisciplinas();
        loadProfessores();
    }

    private void loadDisciplinas() {
        listaDisciplinas.clear();
        List<Disciplina> disciplinas = disciplinaDAO.findAll();
        listaDisciplinas.addAll(disciplinas);
    }

    private void loadProfessores() {
        listaProfessores.clear();
        List<Professor> professores = professorDAO.findAllWithDisciplinas(); // <-- Atualizado aqui
        listaProfessores.addAll(professores);
    }
}
