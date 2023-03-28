package com.ugav.jgalgo.test;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.TPMKomlos1985King1997Hagerup2009;

public class TPMKomlos1985King1997Hagerup2009Test extends TestUtils {

	@Test
	public void testTPM() {
		final long seed = 0x32cba050c3014810L;
		TPMTestUtils.testTPM(TPMKomlos1985King1997Hagerup2009::new, seed);
	}

	@Test
	public void testVerifyMSTPositive() {
		final long seed = 0x61820733d2eb1adaL;
		TPMTestUtils.verifyMSTPositive(TPMKomlos1985King1997Hagerup2009::new, seed);
	}

	@Test
	public void testVerifyMSTNegative() {
		final long seed = 0x3f6671898b7bc54cL;
		TPMTestUtils.verifyMSTNegative(TPMKomlos1985King1997Hagerup2009::new, seed);
	}

}
