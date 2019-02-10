public class System {
	public static Out out = new Out();
	public static In in = new In();
	public static class Out {
		public void println(int i) {
			java.lang.System.out.println(i);
		}
	}
	public static class In {
		public int read() {
			try {
				return java.lang.System.in.read();
			} catch (java.io.IOException e) {
				return -1;
			}
		}
	}
}
