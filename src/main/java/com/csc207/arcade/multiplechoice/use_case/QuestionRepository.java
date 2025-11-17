package com.csc207.arcade.multiplechoice.use_case;

import com.csc207.arcade.multiplechoice.entities.QuizQuestion;
import java.util.List;

/**
 * Interface defining the contract for data access to quiz questions.
 */
public interface QuestionRepository {
    /**
     * Loads data from the data source.
     */
    void loadData();

    /**
     * Gets questions from the data source.
     *
     * @param count Number of questions to retrieve
     * @return List of QuizQuestion objects in the order they appear in the data source
     */
    List<QuizQuestion> getQuestions(int count);
}
