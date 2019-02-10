class MainClass{
    public static void main(String[] arg){
        Blub b;
        int a;
        boolean c;
        b = new Blub();
        c = b.blah();
        a = b.bleh(4);
    }
}

class Blub {
    boolean blah(){return false;}
    int bleh(int a){return a;}
}
