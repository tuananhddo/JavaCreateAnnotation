package com;

public class Main {
    public static void main(String[] args) {
        System.out.println("ACS");
        Person person = new PersonBuilder()
                .setAge(25)
                .setName("John")
                .build();
        Person p2 = new PersonBuilderB()
                .build();
        System.out.println(p2);
    }
}
