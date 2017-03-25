package com.codebrig.jvmmechanic.tests;

public class SimpleGarbageProgram {

    MyList objList = null;
    int wait = 500; // in milliseconds
    int objSize = 1280; // in KB, default = 1.25 M
    int testSteps = 32; // # of added objects

    public static void main(String[] arg) throws Exception {
        new SimpleGarbageProgram().myTest();
    }

    public void myTest() throws Exception {
        System.out.println("Time  Total  Free  Used  Free  Total  Act.  Dead  Over");
        System.out.println("sec.   Mem.  Mem.  Mem.    %.   Obj.  Obj.  Obj.  Head");
        long dt0 = System.currentTimeMillis() / 1000;
        objList = new MyList();
        while (true) {
            doWork(dt0);
        }
    }

    void doWork(long dt0) throws Exception {
        doGarbageStuff1();
        doGarbageStuff2();
        doGarbageStuff3();
        doGarbageStuff4();
        doGarbageStuff5();

        Runtime rt = Runtime.getRuntime();
        long tm = rt.totalMemory() / 1024;
        long fm = rt.freeMemory() / 1024;
        long ratio = (100 * fm) / tm;
        long dt = System.currentTimeMillis() / 1000 - dt0;
        long to = MyObject.getCount() * objSize;
        long ao = MyList.getCount() * objSize;
        System.out.println(dt
                + "  " + tm + "  " + fm + "  " + (tm - fm) + "  " + ratio + "%"
                + "  " + to + "  " + ao + "  " + (to - ao)
                + "  " + (tm - fm - to));
        mySleep(wait);
        mySleep(wait);
    }

    void doGarbageStuff1() {
        for (int m = 0; m < testSteps; m++) {
            objList.add(new MyObject(objSize));
        }
        for (int m = 0; m < testSteps; m++) {
            objList.removeTail();
        }
    }

    void doGarbageStuff2() {
        for (int m = 0; m < testSteps; m++) {
            objList.add(new MyObject(objSize * 2));
        }
        for (int m = 0; m < testSteps; m++) {
            objList.removeTail();
        }
    }

    void doGarbageStuff3() {
        for (int m = 0; m < testSteps; m++) {
            objList.add(new MyObject(objSize * 3));
        }
        for (int m = 0; m < testSteps; m++) {
            objList.removeTail();
        }
    }

    void doGarbageStuff4() {
        for (int m = 0; m < testSteps; m++) {
            objList.add(new MyObject(objSize * 4));
        }
        for (int m = 0; m < testSteps; m++) {
            objList.removeTail();
        }
    }

    void doGarbageStuff5() {
        for (int m = 0; m < testSteps; m++) {
            objList.add(new MyObject(objSize * 5));
        }
        for (int m = 0; m < testSteps; m++) {
            objList.removeTail();
        }
    }

    void mySleep(int t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            System.out.println("Interrupted...");
        }
    }

    public static class MyObject {
        private static long count = 0;
        private long[] obj = null;
        public MyObject next = null;
        public MyObject prev = null;

        public MyObject(int objSize) {
            count++;
            obj = new long[objSize * 128]; //128*8=1024 bytes
        }

        protected final void finalize() {
            count--;
        }

        static long getCount() {
            return count;
        }
    }

    public static class MyList {
        static long count = 0;
        MyObject head = null;
        MyObject tail = null;

        void add(MyObject o) {
            if (head == null) {
                head = o;
                tail = o;
            } else {
                o.prev = head;
                head.next = o;
                head = o;
            }
            count++;
        }

        void removeTail() {
            if (tail != null) {
                if (tail.next == null) {
                    tail = null;
                    head = null;
                } else {
                    tail = tail.next;
                    tail.prev = null;
                }
                count--;
            }
        }

        static long getCount() {
            return count;
        }
    }

}
