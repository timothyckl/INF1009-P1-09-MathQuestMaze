package com.p1_7.game.gameplay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * stateless factory that produces a room assignment from a math question.
 *
 * maps each of the four answer options from a question to a room index (0–3) in order.
 * no additional shuffling is applied here because QuestionGenerator already shuffles
 * the options list before returning it.
 */
public class RoomAssigner {

    /**
     * assigns the four answer options from the given question to room indices 0–3.
     *
     * the i-th option in the question's options list is assigned to room index i.
     * no extra shuffling is applied: the question generator is responsible for
     * randomising option order before this method is called.
     *
     * @param question the math question whose options will be assigned to rooms;
     *                 must not be null and must contain exactly four options
     * @return an immutable room assignment mapping each index 0–3 to one answer value
     * @throws IllegalArgumentException if question is null
     */
    public RoomAssignment assign(MathQuestion question) {
        if (question == null) {
            throw new IllegalArgumentException("question must not be null");
        }

        List<Integer> options = question.getOptions();
        if (options.size() != 4) {
            throw new IllegalArgumentException(
                "question must have exactly 4 options, got: " + options.size());
        }

        Map<Integer, Integer> roomToAnswer = new HashMap<>();

        // zip options with indices 0–3 directly; options are already shuffled by the generator
        for (int roomIndex = 0; roomIndex < options.size(); roomIndex++) {
            roomToAnswer.put(roomIndex, options.get(roomIndex));
        }

        return new RoomAssignment(roomToAnswer);
    }
}
