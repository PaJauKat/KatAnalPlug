package com.example.nex;

import lombok.Getter;

public enum State {
    APAGADO(false),
    ENTRAR(false),
    FASE_1(true),
    FUMUS(true),
    FASE_2(true),
    UMBRA(true),
    FASE_3(true),
    CRUOR(true),
    FASE_4(true),
    GLACIES(true),
    FASE_5(true);

    @Getter
    private boolean enPelea;

    State(boolean peleando) {
        this.enPelea=peleando;
    }
}
