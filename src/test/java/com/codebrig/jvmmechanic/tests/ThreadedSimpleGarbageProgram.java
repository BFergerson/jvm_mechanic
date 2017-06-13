package com.codebrig.jvmmechanic.tests;

class ThreadedSimpleGarbageProgram {

    public static void main(String[] arg) throws Exception {

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                SimpleGarbageProgram program = new SimpleGarbageProgram();
                try {
                    program.myTest();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

}