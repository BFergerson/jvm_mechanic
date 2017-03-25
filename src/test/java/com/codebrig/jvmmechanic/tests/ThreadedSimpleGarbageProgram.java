package com.codebrig.jvmmechanic.tests;

class ThreadedSimpleGarbageProgram {

    public static void main(String[] arg) throws Exception {

        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SimpleGarbageProgram program = new SimpleGarbageProgram();
                    try {
                        program.myTest();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

}