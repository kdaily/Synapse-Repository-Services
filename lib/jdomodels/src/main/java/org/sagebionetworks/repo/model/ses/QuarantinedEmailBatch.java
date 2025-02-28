package org.sagebionetworks.repo.model.ses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Containter for a batch of {@link QuarantinedEmail}s that all share the same expiration timeout
 * 
 * @author Marco
 *
 */
public class QuarantinedEmailBatch {

	public static final QuarantinedEmailBatch EMPTY_BATCH = new QuarantinedEmailBatch(Collections.emptyList());

	private List<QuarantinedEmail> batch;
	private Long expirationTimeout;

	public QuarantinedEmailBatch() {
		this(new ArrayList<>());
	}

	private QuarantinedEmailBatch(List<QuarantinedEmail> batch) {
		this.batch = batch;
	}

	public List<QuarantinedEmail> getBatch() {
		return batch;
	}

	public Long getExpirationTimeout() {
		return expirationTimeout;
	}

	public QuarantinedEmailBatch withExpirationTimeout(Long expirationTimeout) {
		this.expirationTimeout = expirationTimeout;
		return this;
	}

	public void add(QuarantinedEmail quarantinedEmail) {
		batch.add(quarantinedEmail);
	}
	
	public QuarantinedEmail get(int index) {
		return batch.get(index);
	}

	public boolean isEmpty() {
		return batch.isEmpty();
	}

	public int size() {
		return batch.size();
	}

	@Override
	public int hashCode() {
		return Objects.hash(batch, expirationTimeout);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuarantinedEmailBatch other = (QuarantinedEmailBatch) obj;
		return Objects.equals(batch, other.batch) && Objects.equals(expirationTimeout, other.expirationTimeout);
	}

	@Override
	public String toString() {
		return "QuarantinedEmailBatch [batch=" + batch + ", expirationTimeout=" + expirationTimeout + "]";
	}

}
