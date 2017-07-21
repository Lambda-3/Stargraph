package net.stargraph.core.ner;

import net.stargraph.model.LabeledEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class LinkedNamedEntity {

    private LabeledEntity entity;
    private double score;
    private int start;
    private int end;
    private String cat;
    private String value;

    public LinkedNamedEntity(String value, String cat, int start, int end) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("NamedEntity `value` can't be null or empty");
        }
        this.value = value;
        this.cat = cat;
        this.start = start;
        this.end = end;
    }

    public String getValue() {
        return value;
    }

    public String getCat() {
        return cat;
    }

    public double getScore() {
        return score;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public LabeledEntity getEntity() {
        return entity;
    }


    public void reverseValue() {
        if (cat.equalsIgnoreCase("I-PER")) { // TODO: make 'I-PER' constant, align with all classifiers
            List<String> nameTokens = Arrays.asList(value.split("\\s+"));
            if (nameTokens.size() > 1) {
                value = nameTokens.get(nameTokens.size() - 1)
                        + ", " + String.join(" ", nameTokens.subList(0, nameTokens.size() - 1));
            }
        }
    }

    public void merge(String value, int newEndPos) {
        this.value = value;
        this.end = newEndPos;
    }

    public void link(LabeledEntity entity, double score) {
        this.entity = Objects.requireNonNull(entity);
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkedNamedEntity that = (LinkedNamedEntity) o;
        return start == that.start &&
                end == that.end &&
                Objects.equals(entity, that.entity) &&
                Objects.equals(score, that.score) &&
                Objects.equals(cat, that.cat) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity, score, start, end, cat, value);
    }

    @Override
    public String toString() {
        if (entity == null) {
            return "Unlinked{" +
                    "value='" + value + '\'' +
                    ", cat='" + cat + '\'' +
                    '}';
        }
        return "Linked{" +
                "entity='" + entity + '\'' +
                ", value='" + value + '\'' +
                ", cat='" + cat + '\'' +
                ", score='" + score + '\'' +
                '}';
    }
}
