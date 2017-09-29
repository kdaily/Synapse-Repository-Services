package org.sagebionetworks.repo.web.service;

import org.sagebionetworks.reflection.model.PaginatedResults;
import org.sagebionetworks.repo.model.*;
import org.sagebionetworks.repo.web.NotFoundException;

public interface MembershipInvitationService {
	
	/**
	 * 
	 * @param userId
	 * @param dto
	 * @param acceptInvitationEndpoint
	 * @param notificationUnsubscribeEndpoint
	 * @return
	 * @throws UnauthorizedException
	 * @throws InvalidModelException
	 * @throws NotFoundException
	 */
	public MembershipInvtnSubmission create(Long userId,
			MembershipInvtnSubmission dto,
			String acceptInvitationEndpoint, 
			String notificationUnsubscribeEndpoint) throws UnauthorizedException, InvalidModelException, NotFoundException;
	
	/**
	 * 
	 * @param inviteeId
	 * @param teamId
	 * @param limit
	 * @param offset
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException 
	 */
	public PaginatedResults<MembershipInvitation> getOpenInvitations(Long userId, String inviteeId, String teamId, long limit, long offset)
			throws DatastoreException, NotFoundException;

	/**
	 * 
	 * @param inviteeId
	 * @param teamId
	 * @param limit
	 * @param offset
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException 
	 */
	public PaginatedResults<MembershipInvtnSubmission> getOpenInvitationSubmissions(Long userId, String inviteeId, String teamId, long limit, long offset)
			throws DatastoreException, NotFoundException;

	/**
	 * 
	 * @param userId
	 * @param dtoId
	 * @return
	 * @throws DatastoreException
	 * @throws UnauthorizedException
	 * @throws NotFoundException
	 */
	public MembershipInvtnSubmission get(Long userId, String dtoId) throws DatastoreException, UnauthorizedException, NotFoundException;

	/**
	 *
	 * @param token
	 * @return
	 * @throws DatastoreException
	 * @throws UnauthorizedException
	 * @throws NotFoundException
	 */
	public MembershipInvtnSubmission get(String misId, MembershipInvtnSignedToken token) throws DatastoreException, UnauthorizedException, NotFoundException;

	/**
	 * 
	 * @param userId
	 * @param dtoId
	 * @throws DatastoreException
	 * @throws UnauthorizedException
	 * @throws NotFoundException 
	 */
	public void delete(Long userId, String dtoId) throws DatastoreException, UnauthorizedException, NotFoundException;

	/**
	 * 
	 * @param principalId
	 * @return
	 */
	public Count getOpenInvitationCount(Long principalId);

	/**
	 *
	 * @param userId
	 * @param membershipInvitationId
	 * @return
	 */
	public InviteeVerificationSignedToken verifyInvitee(Long userId, String membershipInvitationId);
}
