package io.github.zebin.javabash.frontend;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextBrush {

    private List<ColouredBlock> blocks;

    public TextBrush(String s) {
        this.blocks = new LinkedList<>(List.of(new ColouredBlock(s, null)));
    }

    public TextBrush paint(String word, Object c) {
        blocks = blocks.stream()
                .filter(cW -> !cW.getText().isEmpty())
                .flatMap(cWord -> {
                            List<ColouredBlock> colouredWords = joinWithWord(
                                    Stream.of((cWord.getText() + " ").split(word))
                                            .map(f -> new ColouredBlock(f, cWord.getColor()))
                                    , new ColouredBlock(word, c)
                            );

                            ColouredBlock last = lastOf(colouredWords);
                            last.setText(last.getText().substring(0, last.getText().length() - 1));
                            colouredWords = colouredWords.stream().filter(cW -> !cW.getText().isEmpty())
                                    .toList();

                            if (colouredWords.isEmpty()) {
                                return Stream.of(new ColouredBlock(word, c));
                            }

                            return colouredWords.stream();

                        }
                )

                .collect(Collectors.toList());

        return this;
    }

    private static ColouredBlock lastOf(List<ColouredBlock> colouredWords) {
        return colouredWords.get(colouredWords.size() - 1);
    }

    public TextBrush fill(Object c) {
        blocks.forEach(cBlock -> cBlock.setColor(c));
        return this;
    }

    public TextBrush fillSurrounding(String word, Object c) {
        blocks = blocks.stream()
                .filter(cW -> !cW.getText().isEmpty())
                .flatMap(
                        cWord -> {

                            Stream<ColouredBlock> wordStream = Stream.of((cWord.getText() + " ").split(word))
                                    .map(f -> new ColouredBlock(f, cWord.getColor()));

                            List<ColouredBlock> colouredWords = joinWithWord(
                                    wordStream,
                                    new ColouredBlock(word, cWord.getColor())
                            );

                            final ColouredBlock last = lastOf(colouredWords);
                            last.setText(last.getText().substring(0, last.getText().length() - 1));

                            boolean start = false;
                            for (ColouredBlock cw : colouredWords) {
                                if (start && cw.getText().equals(word)) {
                                    start = false;
                                } else if (start) {
                                    cw.setColor(c);
                                } else if (cw.getText().equals(word)) {
                                    start = true;
                                }
                            }
                            return colouredWords.stream();
                        }
                )
                .filter(cW -> !cW.getText().isEmpty())
                .collect(Collectors.toList());

        return this;
    }

    public static List<ColouredBlock> joinWithWord(Stream<ColouredBlock> bashTMLWordStream, ColouredBlock e) {
        return bashTMLWordStream
                .reduce(
                        new LinkedList<>(),
                        (a, b) -> {
                            final LinkedList<ColouredBlock> bashTMLWords = new LinkedList<>(a);
                            if (!bashTMLWords.isEmpty()) {
                                bashTMLWords.add(e);
                            }
                            bashTMLWords.add(b);
                            return bashTMLWords;
                        },
                        (a, b) -> {

                            final LinkedList<ColouredBlock> objects = new LinkedList<>(a);
                            if (!a.isEmpty() && !b.isEmpty()) {
                                objects.add(e);
                            }
                            objects.addAll(b);
                            return objects;

                        }
                );
    }

    @Override
    public String toString() {
        return blocks.stream()
                .map(cWord -> cWord.getColor() != null ? cWord.getColor() + cWord.getText() + Palette.RESET
                        : cWord.getText())
                .collect(Collectors.joining());
    }
}
