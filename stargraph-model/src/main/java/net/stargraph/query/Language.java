package net.stargraph.query;

public enum Language {

    EN("EN", "english"),
    DE("DE", "german"),
    PT("PT", "portuguese")

    ;

    String code;
    String name;

    Language(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
