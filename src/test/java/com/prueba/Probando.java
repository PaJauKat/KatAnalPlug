package com.prueba;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Probando {
    public static void main(String[] args) {
        List<Integer> katarina = new ArrayList<>();
        System.out.println(katarina.stream().min(Comparator.comparingInt(Integer::intValue)).get());
    }
}
