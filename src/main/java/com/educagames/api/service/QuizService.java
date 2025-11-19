package com.educagames.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.educagames.api.model.dto.quiz.QuizAlternativeDTO;
import com.educagames.api.model.dto.quiz.QuizDTO;
import com.educagames.api.model.dto.quiz.QuizQuestionDTO;
import com.educagames.api.model.entity.Module;
import com.educagames.api.model.entity.Quiz;
import com.educagames.api.model.entity.QuizAlternative;
import com.educagames.api.model.entity.QuizQuestion;
import com.educagames.api.repository.QuizAlternativeRepository;
import com.educagames.api.repository.QuizQuestionRepository;
import com.educagames.api.repository.QuizRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAlternativeRepository quizAlternativeRepository;
    private final EntityManager entityManager;

    /**
     * Salva um quiz a partir de um DTO para um módulo.
     *
     * @param module Módulo ao qual o quiz será associado
     * @param quizDto DTO com os dados do quiz a ser criado
     */
    @Transactional
    public void saveQuizFromDto(Module module, QuizDTO quizDto) {
        if (quizDto == null || quizDto.getQuestions() == null || quizDto.getQuestions().isEmpty()) {
            return;
        }

        Quiz quiz = new Quiz();
        quiz.setModule(module);
        int totalPoints = quizDto.getQuestions().stream()
            .mapToInt(q -> Optional.ofNullable(q.getPoints()).orElse(0))
            .sum();
        quiz.setPoints(totalPoints);
        quizRepository.save(quiz);

        for (QuizQuestionDTO qd : quizDto.getQuestions()) {
            QuizQuestion question = createQuestion(quiz, qd);
            quizQuestionRepository.save(question);
            saveQuestionAlternatives(question, qd);
        }
    }

    /**
     * Cria uma questão de quiz a partir de um DTO.
     *
     * @param quiz quiz ao qual a questão será associada
     * @param dto  DTO com os dados da questão
     * @return entidade QuizQuestion criada
     */
    private QuizQuestion createQuestion(Quiz quiz, QuizQuestionDTO dto) {
        QuizQuestion question = new QuizQuestion();
        question.setQuiz(quiz);
        question.setText(Optional.ofNullable(dto.getText()).orElse(""));
        question.setPoints(Optional.ofNullable(dto.getPoints()).orElse(0));
        return question;
    }

    /**
     * Salva as alternativas de uma questão de quiz.
     * <p>
     * Marca como correta a alternativa cujo texto corresponde ao campo correctAnswer do DTO.
     * </p>
     *
     * @param question questão à qual as alternativas serão associadas
     * @param dto      DTO contendo as opções e a resposta correta
     */
    private void saveQuestionAlternatives(QuizQuestion question, QuizQuestionDTO dto) {
        String correct = Optional.ofNullable(dto.getCorrectAnswer()).orElse("");
        List<String> options = Optional.ofNullable(dto.getOptions()).orElseGet(ArrayList::new);
        for (String opt : options) {
            QuizAlternative alt = new QuizAlternative();
            alt.setQuestion(question);
            alt.setText(Optional.ofNullable(opt).orElse(""));
            alt.setCorrect(opt != null && opt.equals(correct));
            quizAlternativeRepository.save(alt);
        }
    }

    /**
     * Substitui o quiz de um módulo.
     * Remove o quiz existente (se houver) e cria um novo com os dados fornecidos.
     *
     * @param module Módulo
     * @param quizDto DTO com os dados do novo quiz
     */
    @Transactional
    public void replaceQuizFromDto(Module module, QuizDTO quizDto) {
        quizRepository.findByModule(module).ifPresent(this::deleteQuiz);
        entityManager.flush();
        entityManager.clear();
        saveQuizFromDto(module, quizDto);
    }

    /**
     * Remove um quiz e todas as suas questões e alternativas associadas.
     *
     * @param quiz quiz a ser removido
     */
    private void deleteQuiz(Quiz quiz) {
        List<QuizQuestion> questions = quizQuestionRepository.findByQuiz(quiz);
        questions.forEach(question -> {
            List<QuizAlternative> alternatives = quizAlternativeRepository.findByQuestion(question);
            alternatives.forEach(quizAlternativeRepository::delete);
            quizQuestionRepository.delete(question);
        });
        quizRepository.delete(quiz);
    }

    /**
     * Verifica se um módulo possui um quiz.
     *
     * @param module Módulo
     * @return true se o módulo possuir um quiz, false caso contrário
     */
    @Transactional(readOnly = true)
    public boolean hasQuiz(Module module) {
        return quizRepository.findByModule(module).isPresent();
    }

    /**
     * Obtém o quiz de um módulo convertido para DTO.
     *
     * @param module Módulo
     * @return DTO do quiz ou null se o módulo não possuir quiz
     */
    @Transactional(readOnly = true)
    public QuizDTO getQuizByModule(Module module) {
        return quizRepository.findByModule(module)
            .map(quiz -> {
                List<QuizQuestion> questions = quizQuestionRepository.findByQuiz(quiz);
                List<QuizQuestionDTO> questionDTOs = questions.stream()
                    .map(q -> {
                        List<QuizAlternative> alternatives = quizAlternativeRepository.findByQuestion(q);
                        String correct = alternatives.stream()
                            .filter(QuizAlternative::isCorrect)
                            .map(QuizAlternative::getText)
                            .findFirst()
                            .orElse(null);
                        List<String> options = alternatives.stream()
                            .map(QuizAlternative::getText)
                            .toList();
                        return QuizQuestionDTO.builder()
                            .id(q.getId())
                            .text(q.getText())
                            .options(options)
                            .correctAnswer(correct)
                            .points(q.getPoints())
                            .build();
                    })
                    .toList();
                return QuizDTO.builder()
                    .id(quiz.getId())
                    .questions(questionDTOs)
                    .build();
            })
            .orElse(null);
    }

    /**
     * Obtém o quiz de um módulo convertido para DTO sem a resposta correta (para alunos).
     * IMPORTANTE: Retorna os IDs das questões e alternativas para que o frontend possa
     * enviar as respostas corretamente. O campo 'correct' das alternativas é sempre null
     * para não revelar a resposta correta ao aluno.
     *
     * @param module Módulo
     * @return DTO do quiz ou null se o módulo não possuir quiz
     */
    @Transactional(readOnly = true)
    public QuizDTO getQuizByModuleForStudent(Module module) {
        return quizRepository.findByModule(module)
            .map(quiz -> {
                List<QuizQuestion> questions = quizQuestionRepository.findByQuiz(quiz);
                List<QuizQuestionDTO> questionDTOs = questions.stream()
                    .map(q -> {
                        List<QuizAlternative> alternatives = quizAlternativeRepository.findByQuestion(q);

                        List<QuizAlternativeDTO> alternativeDTOs = alternatives.stream()
                            .map(alt -> QuizAlternativeDTO.builder()
                                .id(alt.getId())
                                .text(alt.getText())
                                .correct(null)
                                .build())
                            .toList();

                        List<String> options = alternatives.stream()
                            .map(QuizAlternative::getText)
                            .toList();

                        return QuizQuestionDTO.builder()
                            .id(q.getId())
                            .text(q.getText())
                            .alternatives(alternativeDTOs)
                            .options(options)
                            .correctAnswer(null)
                            .points(q.getPoints())
                            .build();
                    })
                    .toList();
                return QuizDTO.builder()
                    .id(quiz.getId())
                    .questions(questionDTOs)
                    .build();
            })
            .orElse(null);
    }
}
