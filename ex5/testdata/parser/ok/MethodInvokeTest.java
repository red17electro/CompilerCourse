class MainClass{
    public static void main(String[] a){
        Blub b;
        int a;
        bolean c;
        b = new Blub();
        c = b.blah();
        a = b.bleh(4);
        b = b.bluh();
    }
}

class Blub {
    boolean blah(){return false;}
    int bleh(int a){return a;}
    Blub bleh(){return new Blub();}
}
