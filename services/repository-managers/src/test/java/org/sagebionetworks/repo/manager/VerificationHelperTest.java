package org.sagebionetworks.repo.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;

import org.junit.Test;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
public class VerificationHelperTest {

	@Test
	public void testIsVerified() {
		// method under test
		assertFalse(VerificationHelper.isVerified(null));
	
		VerificationSubmission verificationSubmission = new VerificationSubmission();
		
		// method under test
		assertFalse(VerificationHelper.isVerified(verificationSubmission));
		
		VerificationState verificationState = new VerificationState();
		verificationState.setState(VerificationStateEnum.SUBMITTED);
		verificationSubmission.setStateHistory(Collections.singletonList(verificationState));
		
		// method under test
		assertFalse(VerificationHelper.isVerified(verificationSubmission));

		verificationState.setState(VerificationStateEnum.APPROVED);
		
		// method under test
		assertTrue(VerificationHelper.isVerified(verificationSubmission));
	}

	@Test
	public void testGetApprovalDate() {
		// method under test
		assertNull(VerificationHelper.getApprovalDate(null));
	
		VerificationSubmission verificationSubmission = new VerificationSubmission();
		
		// method under test
		assertNull(VerificationHelper.getApprovalDate(verificationSubmission));
		
		VerificationState verificationState = new VerificationState();
		verificationState.setState(VerificationStateEnum.SUBMITTED);
		verificationSubmission.setStateHistory(Collections.singletonList(verificationState));
		
		// method under test
		assertNull(VerificationHelper.getApprovalDate(verificationSubmission));

		Date now = new Date();
		verificationState.setCreatedOn(now);
		
		// method under test
		assertNull(VerificationHelper.getApprovalDate(verificationSubmission));
		
		
		verificationState.setState(VerificationStateEnum.APPROVED);

		// method under test
		assertEquals(now, VerificationHelper.getApprovalDate(verificationSubmission));
	}

}
