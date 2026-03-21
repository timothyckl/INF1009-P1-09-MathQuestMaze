package com.p1_7.game.gameplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * immutable domain object representing a single arithmetic question in the math maze game.
 *
 * encapsulates the question prompt, the correct integer answer, and exactly four unique
 * integer answer options. this class has no rendering or input concerns and is safe to
 * pass freely between gameplay components.
 */
public final class MathQuestion {

    /** the human-readable question text, e.g. {@code "3 + 4 = ?"} */
    private final String prompt;

    /** the single correct integer answer to the question */
    private final int correctAnswer;

    /** the four unique integer answer options presented to the player */
    private final List<Integer> options;

    /**
     * constructs a math question with the given prompt, correct answer, and answer options.
     *
     * <p>the options list must contain exactly four elements, all of which must be unique,
     * and the correct answer must appear among them.
     *
     * @param prompt        the question text shown to the player; must not be null
     * @param correctAnswer the single correct integer answer
     * @param options       exactly four unique integers that include {@code correctAnswer}
     * @throws IllegalArgumentException if {@code options} does not contain exactly 4 elements,
     *                                  if any options are duplicates, or if {@code correctAnswer}
     *                                  is not present in {@code options}
     */
    public MathQuestion(String prompt, int correctAnswer, List<Integer> options) {
        // validate option count before anything else
        if (options == null || options.size() != 4) {
            throw new IllegalArgumentException(
                "options must contain exactly 4 elements, got: "
                    + (options == null ? "null" : options.size()));
        }

        // check for duplicate values using a set
        HashSet<Integer> uniqueOptions = new HashSet<>(options);
        if (uniqueOptions.size() != options.size()) {
            throw new IllegalArgumentException(
                "options must all be unique, but duplicates were found: " + options);
        }

        // ensure the correct answer is one of the provided options
        if (!uniqueOptions.contains(correctAnswer)) {
            throw new IllegalArgumentException(
                "correctAnswer " + correctAnswer + " is not present in options: " + options);
        }

        this.prompt = prompt;
        this.correctAnswer = correctAnswer;
        // store a defensive unmodifiable copy so callers cannot mutate the list
        this.options = Collections.unmodifiableList(new ArrayList<>(options));
    }

    /**
     * returns the question prompt text.
     *
     * @return the human-readable question string
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * returns the correct integer answer to this question.
     *
     * @return the correct answer
     */
    public int getCorrectAnswer() {
        return correctAnswer;
    }

    /**
     * returns an unmodifiable view of the four answer options.
     *
     * @return an unmodifiable list of exactly four unique integer options
     */
    public List<Integer> getOptions() {
        return options;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MathQuestion other = (MathQuestion) obj;
        return correctAnswer == other.correctAnswer
            && Objects.equals(prompt, other.prompt)
            && Objects.equals(options, other.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prompt, correctAnswer, options);
    }

    @Override
    public String toString() {
        return "MathQuestion{"
            + "prompt='" + prompt + "'"
            + ", correctAnswer=" + correctAnswer
            + ", options=" + options
            + "}";
    }
}
