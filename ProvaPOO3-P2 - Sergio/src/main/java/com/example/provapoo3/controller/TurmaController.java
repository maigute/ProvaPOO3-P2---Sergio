package com.example.provapoo3.controller;

import com.example.provapoo3.utils.AlertUtil;
import com.example.provapoo3.dao.DisciplinaDAO;
import com.example.provapoo3.dao.ProfessorDAO;
import com.example.provapoo3.dao.TurmaDAO;
import com.example.provapoo3.model.Disciplina;
import com.example.provapoo3.model.Professor;
import com.example.provapoo3.model.Turma;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalTime; // Importação necessária
import java.time.format.DateTimeParseException; // Importação necessária para tratamento de erro de formato
import java.util.List;

public class TurmaController {

    @FXML private TextField txtId;
    @FXML private TextField txtSemestre;
    @FXML private TextField txtHorario; // Continua sendo TextField para entrada de String
    @FXML private ComboBox<Disciplina> cmbDisciplina;
    @FXML private ComboBox<Professor> cmbProfessor;
    @FXML private TableView<Turma> tableViewTurmas;
    @FXML private TableColumn<Turma, Long> colId;
    @FXML private TableColumn<Turma, String> colSemestre;
    @FXML private TableColumn<Turma, String> colHorario; // Tipo String para exibição na tabela
    @FXML private TableColumn<Turma, String> colDisciplina;
    @FXML private TableColumn<Turma, String> colProfessor;

    private TurmaDAO turmaDAO;
    private DisciplinaDAO disciplinaDAO;
    private ProfessorDAO professorDAO;

    private ObservableList<Turma> listaTurmas;
    private ObservableList<Disciplina> listaDisciplinas;
    private ObservableList<Professor> listaProfessores;

    @FXML
    public void initialize() {
        turmaDAO = new TurmaDAO();
        disciplinaDAO = new DisciplinaDAO();
        professorDAO = new ProfessorDAO();

        listaTurmas = FXCollections.observableArrayList();
        listaDisciplinas = FXCollections.observableArrayList();
        listaProfessores = FXCollections.observableArrayList();

        // Configura ComboBoxes
        cmbDisciplina.setItems(listaDisciplinas);
        cmbDisciplina.setConverter(new javafx.util.StringConverter<Disciplina>() {
            @Override
            public String toString(Disciplina disciplina) {
                return disciplina != null ? disciplina.getNome() : "";
            }
            @Override
            public Disciplina fromString(String string) {
                return null; // Não usado para este propósito
            }
        });

        cmbProfessor.setItems(listaProfessores);
        cmbProfessor.setConverter(new javafx.util.StringConverter<Professor>() {
            @Override
            public String toString(Professor professor) {
                return professor != null ? professor.getNome() : "";
            }
            @Override
            public Professor fromString(String string) {
                return null; // Não usado para este propósito
            }
        });

        // Configura as colunas da TableView
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSemestre.setCellValueFactory(new PropertyValueFactory<>("semestre"));
        // MUDANÇA: Converter LocalTime para String para exibição na coluna
        colHorario.setCellValueFactory(cellData -> {
            if (cellData.getValue().getHorario() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getHorario().toString());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colDisciplina.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDisciplina() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDisciplina().getNome());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colProfessor.setCellValueFactory(cellData -> {
            if (cellData.getValue().getProfessor() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProfessor().getNome());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        tableViewTurmas.setItems(listaTurmas);

        // Adiciona um listener para quando uma turma for selecionada na tabela
        tableViewTurmas.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTurmaDetails(newValue));

        loadData(); // Carrega disciplinas, professores e turmas ao iniciar a tela
    }

    private void showTurmaDetails(Turma turma) {
        if (turma != null) {
            txtId.setText(String.valueOf(turma.getId()));
            txtSemestre.setText(turma.getSemestre());
            // MUDANÇA: Converter LocalTime para String para exibir no TextField
            txtHorario.setText(turma.getHorario() != null ? turma.getHorario().toString() : "");
            cmbDisciplina.getSelectionModel().select(turma.getDisciplina());
            cmbProfessor.getSelectionModel().select(turma.getProfessor());
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
            String semestre = txtSemestre.getText();
            String horarioString = txtHorario.getText(); // Pega a String do TextField

            // MUDANÇA: Converter String do TextField para LocalTime
            LocalTime horario = null;
            if (!horarioString.isEmpty()) {
                horario = LocalTime.parse(horarioString); // Pode lançar DateTimeParseException
            }

            Disciplina disciplina = cmbDisciplina.getSelectionModel().getSelectedItem();
            Professor professor = cmbProfessor.getSelectionModel().getSelectedItem();

            if (semestre.isEmpty() || horarioString.isEmpty() || disciplina == null || professor == null) {
                AlertUtil.showErrorAlert("Erro de Validação", "Todos os campos e seleções são obrigatórios.");
                return;
            }

            Turma novaTurma = new Turma(semestre, disciplina, professor, horario);
            turmaDAO.create(novaTurma);
            AlertUtil.showInformationAlert("Sucesso", "Turma salva com sucesso!");
            loadTurmas();
            handleClear();
        } catch (DateTimeParseException e) { // Captura erro de formato de data/hora
            AlertUtil.showErrorAlert("Erro de Entrada", "Formato de horário inválido. Use HH:MM ou HH:MM:SS.");
            e.printStackTrace();
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Erro", "Erro ao salvar turma: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            if (txtId.getText().isEmpty()) {
                AlertUtil.showWarningAlert("Seleção Necessária", "Selecione uma turma na tabela para atualizar.");
                return;
            }
            Long id = Long.valueOf(txtId.getText());
            String semestre = txtSemestre.getText();
            String horarioString = txtHorario.getText();

            LocalTime horario = null;
            if (!horarioString.isEmpty()) {
                horario = LocalTime.parse(horarioString);
            }

            Disciplina disciplina = cmbDisciplina.getSelectionModel().getSelectedItem();
            Professor professor = cmbProfessor.getSelectionModel().getSelectedItem();

            if (semestre.isEmpty() || horarioString.isEmpty() || disciplina == null || professor == null) {
                AlertUtil.showErrorAlert("Erro de Validação", "Todos os campos e seleções são obrigatórios.");
                return;
            }

            Turma turmaToUpdate = turmaDAO.findById(id);
            if (turmaToUpdate != null) {
                turmaToUpdate.setSemestre(semestre);
                turmaToUpdate.setHorario(horario);
                turmaToUpdate.setDisciplina(disciplina);
                turmaToUpdate.setProfessor(professor);
                turmaDAO.update(turmaToUpdate);
                AlertUtil.showInformationAlert("Sucesso", "Turma atualizada com sucesso!");
                loadTurmas();
                handleClear();
            } else {
                AlertUtil.showWarningAlert("Não Encontrado", "Turma com ID " + id + " não encontrada.");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showErrorAlert("Erro de Entrada", "ID deve ser um número válido.");
        } catch (DateTimeParseException e) {
            AlertUtil.showErrorAlert("Erro de Entrada", "Formato de horário inválido. Use HH:MM ou HH:MM:SS.");
            e.printStackTrace();
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Erro", "Erro ao atualizar turma: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        try {
            if (txtId.getText().isEmpty()) {
                AlertUtil.showWarningAlert("Seleção Necessária", "Selecione uma turma na tabela para excluir.");
                return;
            }
            Long id = Long.valueOf(txtId.getText());
            Turma turmaToDelete = turmaDAO.findById(id);
            if (turmaToDelete != null) {
                turmaDAO.delete(turmaToDelete);
                AlertUtil.showInformationAlert("Sucesso", "Turma excluída com sucesso!");
                loadTurmas();
                handleClear();
            } else {
                AlertUtil.showWarningAlert("Não Encontrado", "Turma com ID " + id + " não encontrada.");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showErrorAlert("Erro de Entrada", "ID deve ser um número válido para exclusão.");
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Erro", "Erro ao excluir turma: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        txtId.clear();
        txtSemestre.clear();
        txtHorario.clear();
        cmbDisciplina.getSelectionModel().clearSelection();
        cmbProfessor.getSelectionModel().clearSelection();
        tableViewTurmas.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleListAll() {
        loadTurmas();
    }

    private void loadData() {
        loadDisciplinas();
        loadProfessores();
        loadTurmas();
    }

    private void loadDisciplinas() {
        listaDisciplinas.clear();
        List<Disciplina> disciplinas = disciplinaDAO.findAll();
        listaDisciplinas.addAll(disciplinas);
    }

    private void loadProfessores() {
        listaProfessores.clear();
        List<Professor> professores = professorDAO.findAll();
        listaProfessores.addAll(professores);
    }

    private void loadTurmas() {
        listaTurmas.clear();
        List<Turma> turmas = turmaDAO.findAll();
        listaTurmas.addAll(turmas);
    }
}