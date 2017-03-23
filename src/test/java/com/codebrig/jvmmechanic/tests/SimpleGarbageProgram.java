package com.codebrig.jvmmechanic.tests;

class SimpleGarbageProgram {

    static MyList objList = null;
    static int wait = 100; // in milliseconds
    static int objSize = 1280; // in KB, default = 1.25 M
    static int initSteps = 320; // # of initial objects
    static int testSteps = 32; // # of added objects

    public static void main(String[] arg) throws Exception {
        if (arg.length > 0) objSize = Integer.parseInt(arg[0]);
        if (arg.length > 1) initSteps = Integer.parseInt(arg[1]);
        if (arg.length > 2) testSteps = Integer.parseInt(arg[2]);

        objList = new MyList();
        myTest();
    }

    public static void myTest() throws Exception {
        System.out.println("Time  Total  Free  Used  Free  Total  Act.  Dead  Over");
        System.out.println("sec.   Mem.  Mem.  Mem.    %.   Obj.  Obj.  Obj.  Head");
        long dt0 = System.currentTimeMillis() / 1000;

        while (true) {
            doWork(dt0);
        }
    }

    static void doWork(long dt0) throws Exception {
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

    static void doGarbageStuff1() {
        for (int m = 0; m < testSteps; m++) {
            objList.add(new MyObject(objSize));
        }
        for (int m = 0; m < testSteps; m++) {
            objList.removeTail();
        }
    }

    static void doGarbageStuff2() {
        for (int m = 0; m < testSteps; m++) {
            objList.add(new MyObject(objSize * 2));
        }
        for (int m = 0; m < testSteps; m++) {
            objList.removeTail();
        }
    }

    static void doGarbageStuff3() {
        for (int m = 0; m < testSteps; m++) {
            objList.add(new MyObject(objSize * 3));
        }
        for (int m = 0; m < testSteps; m++) {
            objList.removeTail();
        }
    }

    static void doGarbageStuff4() {
        for (int m = 0; m < testSteps; m++) {
            objList.add(new MyObject(objSize * 4));
        }
        for (int m = 0; m < testSteps; m++) {
            objList.removeTail();
        }
    }

    static void doGarbageStuff5() {
        for (int m = 0; m < testSteps; m++) {
            objList.add(new MyObject(objSize * 5));
        }
        for (int m = 0; m < testSteps; m++) {
            objList.removeTail();
        }
    }

    static void mySleep(int t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            System.out.println("Interrupted...");
        }
    }

    static class MyObject {
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

    static class MyList {
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
