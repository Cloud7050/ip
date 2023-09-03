package com.cloud.chatbot.token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.cloud.chatbot.exceptions.MissingInputException;



/**
 * Manages all the Tokens representing an instance of user input.
 *
 * Flag sets are stored separate from the list of Tokens. The first token is understood to be the
 * flag, whose flag text is used as the key. The remaining tokens are the sub input, stored in a
 * nested TokenManager.
 */
public class TokenManager {
    private List<Token> tokens = new ArrayList<>();
    private HashMap<String, TokenManager> flagSets = new HashMap<>();

    public TokenManager(String input) {
        // Corner case: Passing "" returns [""] instead of []
        // https://stackoverflow.com/q/4964484/11536796
        String[] words = input.split(" ");

        boolean encounteredContent = false;
        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            // Deal with corner case by ignoring any leading ""
            if (!word.equals("")) {
                encounteredContent = true;
            }

            if (encounteredContent) {
                Token token = new Token(word);
                this.tokens.add(token);
            }

        }

        // Go through tokens to extract flag sets
        int i = 0;
        while (i < tokens.size()) {
            Token token = this.tokens.get(i);

            if (token.isFlag()) {
                int endIndex = this.findFlagSetEnd(i);
                List<Token> flagSet = this.removeTokens(i, endIndex);

                // This removal degrades the flag set into a sub input
                Token flag = flagSet.remove(0);
                this.flagSets.put(
                    flag.getFlag(),
                    new TokenManager(flagSet)
                );
                continue;
            }

            i++;
        }
    }

    public TokenManager(List<Token> _tokens) {
        // Assumption: The passed list will not be mutated externally
        this.tokens = _tokens;
    }

    private static String tokensToString(List<Token> tokens) {
        return tokens
            .stream()
            .map(Token::get)
            .collect(Collectors.joining(" "));
    }

    private int findFlagSetEnd(int startIndex) {
        int lastIndex = this.tokens.size() - 1;
        int movingIndex = startIndex + 1;

        while (movingIndex <= lastIndex) {
            Token token = this.tokens.get(movingIndex);
            if (token.isFlag()) break;

            movingIndex++;
        }

        // Start index inclusive, end index exclusive
        return movingIndex;
    }

    /**
     * Removes and returns all tokens between the specified indices.
     *
     * @param startIndex The index to start from (inclusive).
     * @param endIndex The index to end before (exclusive).
     */
    private List<Token> removeTokens(int startIndex, int endIndex) {
        int removeCount = endIndex - startIndex;
        List<Token> removed = new ArrayList<>();
        while (removeCount > 0) {
            Token token = this.tokens.remove(startIndex);
            removed.add(token);

            removeCount--;
        }
        return removed;
    }

    @Override
    public String toString() {
        return TokenManager.tokensToString(this.tokens);
    }

    public List<Token> getTokens() {
        return this.tokens;
    }

    /**
     * Returns the first token, understood to be the command.
     *
     * @throws MissingInputException If the user input is too short.
     */
    public String getCommand() throws MissingInputException {
        if (this.tokens.size() <= 0) {
            throw new MissingInputException();
        }

        return this.tokens.get(0).get().toLowerCase();
    }

    /**
     * Returns all rejoined tokens except the command.
     *
     * @throws MissingInputException If the user input is too short.
     */
    public String getDescription() throws MissingInputException {
        if (this.tokens.size() <= 1) {
            throw new MissingInputException("Please enter a description for your TODO.");
        }

        List<Token> descriptionTokens = new ArrayList<>(this.tokens);
        descriptionTokens.remove(0);
        return TokenManager.tokensToString(descriptionTokens);
    }

    /**
     * Returns the TokenManager for the specified flag, if it exists.
     *
     * @return null if no such flag exists.
     */
    public TokenManager findFlag(String flag) {
        return this.flagSets.get(flag);
    }
}
