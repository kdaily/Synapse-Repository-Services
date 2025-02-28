package org.sagebionetworks.repo.model.jdo;

import java.util.Arrays;

import org.sagebionetworks.repo.model.dbo.persistence.DBORevision;

public class JDORevisionUtils {
	
	/**
	 * Make a copy of the passed JDORevision
	 * @param toCopy
	 * @return
	 */
	public static DBORevision makeCopyForNewVersion(DBORevision toCopy){
		DBORevision copy = new DBORevision();
		copy.setOwner(toCopy.getOwner());
		// Increment the revision number
		copy.setRevisionNumber(toCopy.getRevisionNumber() + 1);

		if(toCopy.getEntityPropertyAnnotations() != null){
			copy.setEntityPropertyAnnotations(Arrays.copyOf(toCopy.getEntityPropertyAnnotations(), toCopy.getEntityPropertyAnnotations().length));
		}

		copy.setUserAnnotationsJSON(toCopy.getUserAnnotationsJSON());
		// Make a copy of the references byte array
		if(toCopy.getReference() != null){
			// Make a copy of the references.
			copy.setReference(Arrays.copyOf(toCopy.getReference(), toCopy.getReference().length));
		}
		// do not copy over Activity id!
		// copy the file handle
		if(toCopy.getFileHandleId() != null){
			copy.setFileHandleId(toCopy.getFileHandleId());
		}
		return copy;
	}
	

}
