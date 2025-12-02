package com.example.provapoo3.controller;

import com.example.provapoo3.dao.CursoDAO;
import com.example.provapoo3.dao.DisciplinaDAO;
import com.example.provapoo3.dao.ProfessorDAO;
import com.example.provapoo3.model.Curso;
import com.example.provapoo3.model.Disciplina;
import com.example.provapoo3.model.Professor;
import com.example.provapoo3.utils.AlertUtil;
import com.example.provapoo3.dao.*;
import com.example.provapoo3.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DisciplinaController {

    @FXML private TextField txtId;
    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private ComboBox<Curso> cmbCurso;
    @FXML private ComboBox<Professor> cmbProfessor;
    @FXML private Label lblProfessoresSelecionados;
    @FXML private TableView<Disciplina> tableViewDisciplinas;
    @FXML private TableColumn<Disciplina, Long> colId;
    @FXML private TableColumn<Disciplina, String> colNome;
    @FXML private TableColumn<Disciplina, String> colDescricao;
    @FXML private TableColumn<Disciplina, String> colCurso;
    @FXML private TableColumn<Disciplina, String> colProfessores;

    private DisciplinaDAO disciplinaDAO;
    private CursoDAO cursoDAO;
    private ProfessorDAO professorDAO;

    private ObservableList<Disciplina> listaDisciplinas;
    private ObservableList<Curso> listaCursos;
    private ObservableList<Professor> listaProfessores;
    private Set<Professor> professoresSelecionados;

    @FXML
    public void initialize() {
        disciplinaDAO = new DisciplinaDAO();
        cursoDAO = new CursoDAO();
        professorDAO = new ProfessorDAO();

        listaDisciplinas = FXCollections.observableArrayList();
        listaCursos = FXCollections.observableArrayList();
        listaProfessores = FXCollections.observableArrayList();
        professoresSelecionados = new HashSet<>();

        configurarComboBoxes();
        configurarTableView();
        loadData();
    }

    private void configurarComboBoxes() {
        cmbCurso.setItems(listaCursos);
        cmbCurso.setConverter(new javafx.util.StringConverter<Curso>() {
            @Override public String toString(Curso curso) {
                return curso != null ? curso.getNome() : "";
            }
            @Override public Curso fromString(String string) {
                return null;
            }
        });

        cmbProfessor.setItems(listaProfessores);
        cmbProfessor.setConverter(new javafx.util.StringConverter<Professor>() {
            @Override public String toString(Professor professor) {
                return professor != null ? professor.getNome() : "";
            }
            @Override public Professor fromString(String string) {
                return null;
            }
        });
    }

    private void configurarTableView() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colCurso.setCellValueFactory(cellData -> {
            Curso curso = cellData.getValue().getCurso();
            return new SimpleStringProperty(curso != null ? curso.getNome() : "");
        });
        colProfessores.setCellValueFactory(cellData -> {
            String nomes = cellData.getValue().getProfessores().stream()
                    .map(Professor::getNome)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(nomes);
        });

        tableViewDisciplinas.setItems(listaDisciplinas);
        tableViewDisciplinas.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showDisciplinaDetails(newValue));
    }

    private void loadData() {
        loadCursos();
        loadProfessores();
        loadDisciplinas();
    }

    private void loadCursos() {
        listaCursos.clear();
        List<Curso> cursos = cursoDAO.findAll();
        listaCursos.addAll(cursos);
    }

    private void loadProfessores() {
        listaProfessores.clear();
        List<Professor> professores = professorDAO.findAll();
        listaProfessores.addAll(professores);
    }

    private void loadDisciplinas() {
        listaDisciplinas.clear();
        List<Disciplina> disciplinas = disciplinaDAO.findAllWithProfessoresAndCurso();
        listaDisciplinas.addAll(disciplinas);
    }

    private void showDisciplinaDetails(Disciplina disciplina) {
        if (disciplina != null) {
            Disciplina disciplinaCompleta = disciplinaDAO.findByIdWithProfessores(disciplina.getId());

            txtId.setText(String.valueOf(disciplinaCompleta.getId()));
            txtNome.setText(disciplinaCompleta.getNome());
            txtDescricao.setText(disciplinaCompleta.getDescricao());
            cmbCurso.getSelectionModel().select(disciplinaCompleta.getCurso());

            professoresSelecionados.clear();
            professoresSelecionados.addAll(disciplinaCompleta.getProfessores());
            updateProfessoresSelecionadosLabel();
        } else {
            handleClear();
        }
    }

    private void updateProfessoresSelecionadosLabel() {
        if (professoresSelecionados.isEmpty()) {
            lblProfessoresSelecionados.setText("Nenhum professor selecionado");
        } else {
            String nomes = professoresSelecionados.stream()
                    .map(Professor::getNome)
                    .collect(Collectors.joining(", "));
            lblProfessoresSelecionados.setText("Professores: " + nomes);
        }
    }

    @FXML
    private void handleAddProfessor() {
        Professor selected = cmbProfessor.getSelectionModel().getSelectedItem();
        if (selected != null && !professoresSelecionados.contains(selected)) {
            professoresSelecionados.add(selected);
            updateProfessoresSelecionadosLabel();
        }
    }

    @FXML
    private void handleRemoveProfessor() {
        Professor selected = cmbProfessor.getSelectionModel().getSelectedItem();
        if (selected != null && professoresSelecionados.remove(selected)) {
            updateProfessoresSelecionadosLabel();
        }
    }

    @FXML
    private void handleNew() {
        handleClear();
    }

    @FXML
    private void handleSave() {
        try {
            String nome = txtNome.getText().trim();
            String descricao = txtDescricao.getText().trim();
            Curso curso = cmbCurso.getSelectionModel().getSelectedItem();

            if (nome.isEmpty() || curso == null) {
                AlertUtil.showErrorAlert("Erro", "Nome e curso são obrigatórios!");
                return;
            }

            if (txtId.getText().isEmpty()) {
                // Novo registro
                Disciplina novaDisciplina = new Disciplina(nome, descricao, curso);
                novaDisciplina.setProfessores(new HashSet<>(professoresSelecionados));
                disciplinaDAO.create(novaDisciplina);
                AlertUtil.showInformationAlert("Sucesso", "Disciplina criada com sucesso!");
            } else {
                // Atualização
                Long id = Long.valueOf(txtId.getText());
                Disciplina disciplina = new Disciplina(nome, descricao, curso);
                disciplina.setId(id);
                disciplina.setProfessores(new HashSet<>(professoresSelecionados));
                disciplinaDAO.update(disciplina);
                AlertUtil.showInformationAlert("Sucesso", "Disciplina atualizada com sucesso!");
            }

            loadDisciplinas();
            handleClear();
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Erro", "Erro ao salvar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        try {
            if (txtId.getText().isEmpty()) {
                AlertUtil.showWarningAlert("Aviso", "Selecione uma disciplina para excluir");
                return;
            }

            Long id = Long.valueOf(txtId.getText());
            Disciplina disciplina = disciplinaDAO.findById(id);

            if (disciplina != null && AlertUtil.showConfirmationAlert("Confirmar",
                    "Tem certeza que deseja excluir esta disciplina?")) {

                disciplinaDAO.delete(disciplina);
                AlertUtil.showInformationAlert("Sucesso", "Disciplina excluída com sucesso!");
                loadDisciplinas();
                handleClear();
            }
        } catch (Exception e) {
            AlertUtil.showErrorAlert("Erro", "Erro ao excluir: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        txtId.clear();
        txtNome.clear();
        txtDescricao.clear();
        cmbCurso.getSelectionModel().clearSelection();
        cmbProfessor.getSelectionModel().clearSelection();
        professoresSelecionados.clear();
        updateProfessoresSelecionadosLabel();
        tableViewDisciplinas.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleListAll() {
        loadDisciplinas();
    }
}