class Wuff {
    public static void main(String[] args) {
        int x;
        x = new A().foo(1);
    }
}

class A {
    int foo(int x, int y) {
        return x + y;
    }
}