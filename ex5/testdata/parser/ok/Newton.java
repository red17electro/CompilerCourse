// Perform Newton-Raphson iteration to compute the square-root of 
// a given number
// ---------------------------------------------------------------------------
// Expect his output (with epsilon =1/100)(ie. sqrt(2) ~= 577/408)
// 2
// 999
// 577
// 408
// 0
// Expect his output (with epsilon =1/10000)(ie. sqrt(2) ~= 665857/470832):
// 2
// 999
// 665857
// 470832
// 0
// ---------------------------------------------------------------------------

class Newton {
	public static void main(String[] a) {
		System.out.println((new NewtonMain()).test_it(2));
	}
}

class Frac {
	int num;
	int den;

	boolean init(int x, int y) {
		boolean ok;
		ok = true;
		num = x;
		den = y;
		return ok;
	}

	int den() {
		return den;
	}

	int num() {
		return num;
	}

	int showFrac() {
		int rc;
		rc = 0;
		System.out.println(num);
		System.out.println(den);
		return rc;
	}

	// String toString () {
	// return ((new Integer(this.num)).toString()+"/"+(new
	// Integer(this.den)).toString());
	// }
}

class Integral {
	boolean eq(int x, int y) {
		return (!(x < y)) && (!(y < x));
	}

	int div(int x, int y) {
		int res;
		res = 0;
		while (!(x < y)) {
			res = res + 1;
			x = x - y;
		}
		return res;
	}

	int rem(int m, int n) {
		return m - n * this.div(m, n);
	}

	int gcd_rec(int x, int y) {
		int res;
		if (x < y) {
			res = this.gcd_rec(y, x);
		} else if (y < 1) {
			res = x;
		} else {
			res = this.gcd_rec(y, this.rem(x, y));
		}
		return res;
	}

	int gcd_iter(int x, int y) {
		int t;
		if (x < y) {
			t = x;
			x = y;
			y = t;
		} else {
		}
		while (0 < y) {
			t = x;
			x = y;
			y = this.rem(t, y);
		}
		return x;
	}

	int gcd(int x, int y) {
		return this.gcd_rec(x, y);
	}

	int lcm(int x, int y) {
		int res;
		res = y * this.div(x, this.gcd(x, y));
		return res;
	}
}

class FracOps { // extends Integral
	Integral iops;

	int init() {
		int rc;
		iops = new Integral();
		rc = 0;
		return rc;
	}

	Frac mkFrac(int x, int y) {
		boolean ok;
		Frac res;
		res = new Frac();
		ok = res.init(x, y);
		return res;
	}

	Frac plus(Frac x, Frac y) {
		int lcm;
		int res_num;
		int res_den;
		Frac res;
		lcm = iops.lcm(x.den(), y.den());
		res_num = x.num() * (iops.div(lcm, x.den())) + y.num()
				* (iops.div(lcm, y.den()));
		res_den = lcm;
		res = this.mkFrac(res_num, res_den);
		return res;
	}

	Frac times(Frac x, Frac y) {
		int a;
		int b;
		int res_num;
		int res_den;
		Frac res;
		a = iops.gcd(x.den(), y.num());
		b = iops.gcd(x.num(), y.den());
		res_num = iops.div(x.num(), b) * iops.div(y.num(), a);
		res_den = iops.div(x.den(), a) * iops.div(y.den(), b);
		res = this.mkFrac(res_num, res_den);
		return res;
	}

	Frac recip(Frac x) {
		Frac res;
		res = this.mkFrac(x.den(), x.num());
		return res;
	}

	Frac divide(Frac x, Frac y) {
		Frac yr;
		Frac res;
		yr = this.recip(y);
		res = this.times(x, yr);
		return res;
	}

	Frac neg(Frac x) { // assumes non-neg fraction!
		Frac res;
		res = this.mkFrac((0 - 1) * x.num(), x.den());
		return res;
	}

	Frac abs_minus(Frac x, Frac y) {
		Frac res;
		if (this.lt(x, y)) {
			res = this.plus(y, this.neg(x)); // y-x;
		} else {
			res = this.plus(x, this.neg(y)); // x-y;
		}
		return res;
	}

	boolean lt(Frac x, Frac y) {
		int lcm;
		int x0;
		int y0;
		boolean res;
		lcm = iops.lcm(x.den(), y.den());
		x0 = x.num() * (iops.div(lcm, x.den()));
		y0 = y.num() * (iops.div(lcm, y.den()));
		res = x0 < y0;
		return res;
	}
}

class NewtonMain { // extends FracOps
	Frac half;
	FracOps fops;

	// compute the sqrt of an integer using Newton-Raphson iteration:
	// approximate sqrt(N) by: x(n+1) = 1/2*(x(n)+N/x(n))
	Frac newton_step(Frac x, int n) {
		return fops
				.times(half, fops.plus(x, fops.divide(fops.mkFrac(n, 1), x)));
	}

	Frac newton(int n) {
		Frac x;
		Frac y;
		Frac epsilon;
		Frac delta;
		int rc;
		half = fops.mkFrac(1, 2);
		epsilon = fops.mkFrac(1, 100);
		x = fops.mkFrac(n, 1);
		delta = x;
		while (fops.lt(epsilon, delta)) {
			y = x;
			x = this.newton_step(x, n);
			delta = fops.abs_minus(x, y);
		}
		return x;
	}

	int test_it(int n) {
		Frac res;
		int rc;
		fops = new FracOps();
		rc = fops.init();
		res = this.newton(n);
		System.out.println(n); // print input number: N
		System.out.println(999); // print marker
		rc = res.showFrac(); // print fraction, approximating swrt(N)
		return rc;
	}
}
