package org.sagebionetworks.repo.manager.table;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.sagebionetworks.StackConfigurationSingleton;
import org.sagebionetworks.common.util.progress.ProgressCallback;
import org.sagebionetworks.common.util.progress.ProgressingCallable;
import org.sagebionetworks.repo.manager.AuthorizationManager;
import org.sagebionetworks.repo.manager.ObjectTypeManager;
import org.sagebionetworks.repo.manager.entity.ReplicationMessageManagerAsynch;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.ConflictingUpdateException;
import org.sagebionetworks.repo.model.DataType;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.LimitExceededException;
import org.sagebionetworks.repo.model.NodeDAO;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UnauthorizedException;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.model.dao.table.TableRowTruthDAO;
import org.sagebionetworks.repo.model.dao.table.TableStatusDAO;
import org.sagebionetworks.repo.model.dbo.dao.table.ViewScopeDao;
import org.sagebionetworks.repo.model.entity.IdAndVersion;
import org.sagebionetworks.repo.model.entity.IdAndVersionBuilder;
import org.sagebionetworks.repo.model.jdo.KeyFactory;
import org.sagebionetworks.repo.model.message.ChangeMessage;
import org.sagebionetworks.repo.model.message.ChangeType;
import org.sagebionetworks.repo.model.message.MessageToSend;
import org.sagebionetworks.repo.model.message.TransactionalMessenger;
import org.sagebionetworks.repo.model.table.ColumnChange;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.EntityField;
import org.sagebionetworks.repo.model.table.TableState;
import org.sagebionetworks.repo.model.table.TableStatus;
import org.sagebionetworks.repo.model.table.ViewTypeMask;
import org.sagebionetworks.repo.transactions.NewWriteTransaction;
import org.sagebionetworks.repo.transactions.WriteTransaction;
import org.sagebionetworks.repo.web.NotFoundException;
import org.sagebionetworks.table.cluster.ColumnChangeDetails;
import org.sagebionetworks.table.cluster.ConnectionFactory;
import org.sagebionetworks.table.cluster.TableIndexDAO;
import org.sagebionetworks.table.cluster.utils.TableModelUtils;
import org.sagebionetworks.util.TimeoutUtils;
import org.sagebionetworks.util.ValidateArgument;
import org.sagebionetworks.workers.util.semaphore.WriteReadSemaphoreRunner;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

public class TableManagerSupportImpl implements TableManagerSupport {
	
	public static final long TABLE_PROCESSING_TIMEOUT_MS = 1000*60*10; // 10 mins
	
	public static final int MAX_CONTAINERS_PER_VIEW = 1000*10; // 10K;
	public static final String SCOPE_SIZE_LIMITED_EXCEEDED_FILE_VIEW = "The view's scope exceeds the maximum number of "
			+ MAX_CONTAINERS_PER_VIEW
			+ " projects and/or folders. Note: The sub-folders of each project and folder in the scope count towards the limit.";
	public static final String SCOPE_SIZE_LIMITED_EXCEEDED_PROJECT_VIEW = "The view's scope exceeds the maximum number of "
			+ MAX_CONTAINERS_PER_VIEW + " projects.";
	
	private static final List<EntityField> FILE_VIEW_DEFAULT_COLUMNS= Lists.newArrayList(
			EntityField.id,
			EntityField.name,
			EntityField.createdOn,
			EntityField.createdBy,
			EntityField.etag,
			EntityField.type,
			EntityField.currentVersion,
			EntityField.parentId,
			EntityField.benefactorId,
			EntityField.projectId,
			EntityField.modifiedOn,
			EntityField.modifiedBy,
			EntityField.dataFileHandleId
			);
	
	private static final List<EntityField> BASIC_ENTITY_DEAFULT_COLUMNS = Lists.newArrayList(
			EntityField.id,
			EntityField.name,
			EntityField.createdOn,
			EntityField.createdBy,
			EntityField.etag,
			EntityField.modifiedOn,
			EntityField.modifiedBy
			);

	@Autowired
	TableStatusDAO tableStatusDAO;
	@Autowired
	TimeoutUtils timeoutUtils;
	@Autowired
	TransactionalMessenger transactionalMessenger;
	@Autowired
	ConnectionFactory tableConnectionFactory;
	@Autowired
	ColumnModelManager columnModelManager;
	@Autowired
	NodeDAO nodeDao;
	@Autowired
	TableRowTruthDAO tableTruthDao;
	@Autowired
	ViewScopeDao viewScopeDao;
	@Autowired
	WriteReadSemaphoreRunner writeReadSemaphoreRunner;
	@Autowired
	AuthorizationManager authorizationManager;
	@Autowired
	ReplicationMessageManagerAsynch replicationMessageManagerAsynch;
	@Autowired
	ObjectTypeManager objectTypeManager;
	
	/*
	 * Cache of default ColumnModels for views.  Once created, these columns will not change
	 * and will be the same across the cluster.
	 */
	Map<EntityField, ColumnModel> defaultColumnCache = Collections.synchronizedMap(new PassiveExpiringMap<>(1, TimeUnit.HOURS));
	
	/*
	 * (non-Javadoc)
	 * @see org.sagebionetworks.repo.manager.table.TableRowManager#getTableStatusOrCreateIfNotExists(java.lang.String)
	 */
	@NewWriteTransaction
	@Override
	public TableStatus getTableStatusOrCreateIfNotExists(IdAndVersion idAndVersion) throws NotFoundException {
		try {
			TableStatus status = tableStatusDAO.getTableStatus(idAndVersion);
			if(!TableState.AVAILABLE.equals(status.getState())){
				// Processing or Failed.
				// Is progress being made?
				if(timeoutUtils.hasExpired(TABLE_PROCESSING_TIMEOUT_MS, status.getChangedOn().getTime())){
					// progress has not been made so trigger another update
					return setTableToProcessingAndTriggerUpdate(idAndVersion);
				}else{
					// progress has been made so just return the status
					return status;
				}
			}
			// Status is Available, is the index synchronized with the truth?
			if(isIndexSynchronizedWithTruth(idAndVersion)){
				// Available and synchronized.
				return status;
			}else{
				// Available but not synchronized, so change the state to processing.
				return setTableToProcessingAndTriggerUpdate(idAndVersion);
			}
			
		} catch (NotFoundException e) {
			// make sure the table exists
			if (!isTableAvailable(idAndVersion)) {
				throw new NotFoundException("Table " + idAndVersion + " not found");
			}
			return setTableToProcessingAndTriggerUpdate(idAndVersion);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sagebionetworks.repo.manager.table.TableManagerSupport#setTableToProcessingAndTriggerUpdate(java.lang.String)
	 */
	@WriteTransaction
	@Override
	public TableStatus setTableToProcessingAndTriggerUpdate(IdAndVersion idAndVersion) {
		ValidateArgument.required(idAndVersion, "idAndVersion");
		// lookup the table type.
		ObjectType tableType = getTableType(idAndVersion);
		// we get here, if the index for this table is not (yet?) being build. We need to kick off the
		// building of the index and report the table as unavailable
		String token = tableStatusDAO.resetTableStatusToProcessing(idAndVersion);
		// notify all listeners.
		transactionalMessenger.sendMessageAfterCommit( new MessageToSend().withObjectId(idAndVersion.getId().toString())
				.withObjectVersion(idAndVersion.getVersion().orElse(null))
				.withObjectType(tableType).withChangeType(ChangeType.UPDATE));
		// status should exist now
		return tableStatusDAO.getTableStatus(idAndVersion);
	}

	@NewWriteTransaction
	@Override
	public void attemptToSetTableStatusToAvailable(IdAndVersion idAndVersion,
			String resetToken, String tableChangeEtag) throws ConflictingUpdateException,
			NotFoundException {
		tableStatusDAO.attemptToSetTableStatusToAvailable(idAndVersion, resetToken, tableChangeEtag);
	}

	@NewWriteTransaction
	@Override
	public void attemptToSetTableStatusToFailed(IdAndVersion idAndVersion,
			String resetToken, Exception error)
			throws ConflictingUpdateException, NotFoundException {
		String errorMessage = error.getMessage();
		StringWriter writer = new StringWriter();
		error.printStackTrace(new PrintWriter(writer));
		String errorDetails = writer.toString();
		tableStatusDAO.attemptToSetTableStatusToFailed(idAndVersion, resetToken, errorMessage, errorDetails);
	}

	@NewWriteTransaction
	@Override
	public void attemptToUpdateTableProgress(IdAndVersion idAndVersion, String resetToken,
			String progressMessage, Long currentProgress, Long totalProgress)
			throws ConflictingUpdateException, NotFoundException {
		tableStatusDAO.attemptToUpdateTableProgress(idAndVersion, resetToken, progressMessage, currentProgress, totalProgress);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sagebionetworks.repo.manager.table.TableStatusManager#startTableProcessing(java.lang.String)
	 */
	@NewWriteTransaction
	@Override
	public String startTableProcessing(IdAndVersion idAndVersion) {
		return tableStatusDAO.resetTableStatusToProcessing(idAndVersion);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sagebionetworks.repo.manager.table.TableStatusManager#isIndexSynchronizedWithTruth(java.lang.String)
	 */
	@Override
	public boolean isIndexSynchronizedWithTruth(IdAndVersion idAndVersion) {
		// MD5 of the table's schema
		String truthSchemaMD5Hex = getSchemaMD5Hex(idAndVersion);
		// get the truth version
		long truthLastVersion = getTableVersion(idAndVersion);
		// compare the truth with the index.
		return this.tableConnectionFactory.getConnection(idAndVersion).doesIndexStateMatch(idAndVersion, truthLastVersion, truthSchemaMD5Hex);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sagebionetworks.repo.manager.table.TableStatusManager#isIndexWorkRequired(java.lang.String)
	 */
	@Override
	public boolean isIndexWorkRequired(IdAndVersion idAndVersion) {
		// Does the table exist and not in the trash?
		if(!isTableAvailable(idAndVersion)){
			return false;
		}
		// work is needed if the index is out-of-sych.
		if(!isIndexSynchronizedWithTruth(idAndVersion)){
			return true;
		}
		// work is needed if the current state is processing.
		TableStatus status = tableStatusDAO.getTableStatus(idAndVersion);
		return TableState.PROCESSING.equals(status.getState());
	}

	/*
	 * (non-Javadoc)
	 * @see org.sagebionetworks.repo.manager.table.TableStatusManager#setTableDeleted(java.lang.String)
	 */
	@Override
	public void setTableDeleted(IdAndVersion idAndVersion, ObjectType tableType) {
		transactionalMessenger.sendDeleteMessageAfterCommit(idAndVersion.getId().toString(), tableType);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sagebionetworks.repo.manager.table.TableManagerSupport#getSchemaMD5Hex(java.lang.String)
	 */
	@Override
	public String getSchemaMD5Hex(IdAndVersion idAndVersion) {
		List<String> columnIds = columnModelManager.getColumnIdsForTable(idAndVersion);
		return TableModelUtils.createSchemaMD5Hex(columnIds);
	}
	
	@Override
	public Optional<Long> getLastTableChangeNumber(IdAndVersion idAndVersion) {
		if(idAndVersion.getVersion().isPresent()) {
			return this.tableTruthDao.getLastTableChangeNumber(idAndVersion.getId(), idAndVersion.getVersion().get());
		}else {
			return this.tableTruthDao.getLastTableChangeNumber(idAndVersion.getId());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sagebionetworks.repo.manager.table.TableManagerSupport#isTableAvailable(java.lang.String)
	 */
	@Override
	public boolean isTableAvailable(IdAndVersion idAndVersion) {
		return nodeDao.isNodeAvailable(idAndVersion.getId());
	}
	
	@Override
	public boolean doesTableExist(IdAndVersion idAndVersion) {
		return nodeDao.doesNodeExist(idAndVersion.getId());
	}
	/*
	 * (non-Javadoc)
	 * @see org.sagebionetworks.repo.manager.table.TableManagerSupport#getTableType(java.lang.String)
	 */
	@Override
	public ObjectType getTableType(IdAndVersion idAndVersion) {
		EntityType type = getTableEntityType(idAndVersion);
		return getObjectTypeForEntityType(type);
	}
	
	/**
	 * Convert an EntityType to an Object.
	 * @param type
	 * @return
	 */
	public static ObjectType getObjectTypeForEntityType(EntityType type) {
		switch (type) {
		case table:
			return ObjectType.TABLE;
		case entityview:
			return ObjectType.ENTITY_VIEW;
		default:
			throw new IllegalArgumentException("unknown table type: " + type);
		}

	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sagebionetworks.repo.manager.table.TableManagerSupport#calculateFileViewCRC32(java.lang.String)
	 */
	@Override
	public Long calculateViewCRC32(IdAndVersion idAndVersion) {
		// Start with all container IDs that define the view's scope
		Long viewTypeMask = getViewTypeMask(idAndVersion);
		Set<Long> viewContainers = getAllContainerIdsForViewScope(idAndVersion, viewTypeMask);
		// Trigger the reconciliation of this view's scope.
		triggerScopeReconciliation(viewTypeMask, viewContainers);
		TableIndexDAO indexDao = this.tableConnectionFactory.getConnection(idAndVersion);
		return indexDao.calculateCRC32ofEntityReplicationScope(viewTypeMask, viewContainers);
	}

	@Override
	public void triggerScopeReconciliation(Long viewTypeMask, Set<Long> viewContainers) {
		// Trigger the reconciliation of this view
		List<Long> containersToReconcile = new LinkedList<Long>();
		if(ViewTypeMask.Project.getMask() == viewTypeMask){
			// project views reconcile with root.
			Long rootId = KeyFactory.stringToKey(StackConfigurationSingleton.singleton().getRootFolderEntityId());
			containersToReconcile.add(rootId);
		}else{
			// all other views reconcile one the view's scope.
			containersToReconcile.addAll(viewContainers);
		}
		this.replicationMessageManagerAsynch.pushContainerIdsToReconciliationQueue(containersToReconcile);
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.sagebionetworks.repo.manager.table.TableViewTruthManager#getAllContainerIdsForViewScope(java.lang.String)
	 */
	@Override
	public Set<Long> getAllContainerIdsForViewScope(IdAndVersion idAndVersion, Long viewTypeMask) {
		ValidateArgument.required(idAndVersion, "idAndVersion");
		// Lookup the scope for this view.
		Set<Long> scope = viewScopeDao.getViewScope(idAndVersion.getId());
		return getAllContainerIdsForScope(scope, viewTypeMask);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sagebionetworks.repo.manager.table.TableManagerSupport#getAllContainerIdsForScope(java.util.Set)
	 */
	@Override
	public Set<Long> getAllContainerIdsForScope(Set<Long> scope, Long viewTypeMask) {
		ValidateArgument.required(scope, "scope");
		ValidateArgument.required(viewTypeMask, "viewTypeMask");
		// Validate the given scope is under the limit.
		if(scope.size() > MAX_CONTAINERS_PER_VIEW){
			throw new IllegalArgumentException(createViewOverLimitMessage(viewTypeMask));
		}
		
		if(ViewTypeMask.Project.getMask() == viewTypeMask){
			return scope;
		}
		// Expand the scope to include all sub-folders
		try {
			return nodeDao.getAllContainerIds(scope, MAX_CONTAINERS_PER_VIEW);
		} catch (LimitExceededException e) {
			// Convert the generic exception to a specific exception.
			throw new IllegalArgumentException(createViewOverLimitMessage(viewTypeMask));
		}
	}
	
	/**
	 * Throw an IllegalArgumentException that indicates the view is over the limit.
	 * 
	 * @param viewType
	 */
	public String createViewOverLimitMessage(Long viewTypeMask) throws IllegalArgumentException{
		ValidateArgument.required(viewTypeMask, "viewTypeMask");
		if(ViewTypeMask.Project.getMask() == viewTypeMask) {
			return SCOPE_SIZE_LIMITED_EXCEEDED_PROJECT_VIEW;
		}else {
			return SCOPE_SIZE_LIMITED_EXCEEDED_FILE_VIEW;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sagebionetworks.repo.manager.table.TableManagerSupport#getTableVersion(java.lang.String)
	 */
	@Override
	public long getTableVersion(IdAndVersion idAndVersion) {
		// Determine the type of able
		ObjectType type = getTableType(idAndVersion);
		switch (type) {
		case TABLE:
			// For TableEntity the version of the last change set is used.
			Optional<Long> value = getLastTableChangeNumber(idAndVersion);
			return value.orElse(-1L);
		case ENTITY_VIEW:
			// For FileViews the CRC of all files in the view is used.
			return calculateViewCRC32(idAndVersion);
		default:
			throw new IllegalArgumentException("unknown table type: " + type);
		}
	}
	
	@Override
	public <R> R tryRunWithTableExclusiveLock(ProgressCallback callback,
			IdAndVersion tableId, int timeoutSec, ProgressingCallable<R> callable)
			throws Exception {
		String key = TableModelUtils.getTableSemaphoreKey(tableId);
		// The semaphore runner does all of the lock work.
		return writeReadSemaphoreRunner.tryRunWithWriteLock(callback, key, timeoutSec, callable);
	}

	@Override
	public <R> R tryRunWithTableNonexclusiveLock(
			ProgressCallback callback, IdAndVersion tableId, int lockTimeoutSec,
			ProgressingCallable<R> callable) throws Exception
			{
		String key = TableModelUtils.getTableSemaphoreKey(tableId);
		// The semaphore runner does all of the lock work.
		return writeReadSemaphoreRunner.tryRunWithReadLock(callback, key, lockTimeoutSec, callable);
	}
	
	@Override
	public EntityType validateTableReadAccess(UserInfo userInfo, IdAndVersion idAndVersion)
			throws UnauthorizedException, DatastoreException, NotFoundException {
		// They must have read permission to access table content.
		authorizationManager.canAccess(userInfo, idAndVersion.getId().toString(), ObjectType.ENTITY, ACCESS_TYPE.READ).checkAuthorizationOrElseThrow();

		// Lookup the entity type for this table.
		EntityType entityTpe = getTableEntityType(idAndVersion);
		ObjectType type = getObjectTypeForEntityType(entityTpe);
		// User must have the download permission to read from a TableEntity.
		if (ObjectType.TABLE.equals(type)) {
			// If the table's DataType is not OPEN then the caller must have the download permission (see PLFM-5240).
			if (!DataType.OPEN_DATA.equals(objectTypeManager.getObjectsDataType(idAndVersion.getId().toString(), ObjectType.ENTITY))) {
				// And they must have download permission to access table content.
				authorizationManager.canAccess(userInfo, idAndVersion.getId().toString(), ObjectType.ENTITY, ACCESS_TYPE.DOWNLOAD).checkAuthorizationOrElseThrow();
			}

		}
		return entityTpe;
	}
	
	@Override
	public void validateTableWriteAccess(UserInfo userInfo, IdAndVersion idAndVersion)
			throws UnauthorizedException, DatastoreException, NotFoundException {
		// They must have update permission to change table content
		authorizationManager.canAccess(userInfo, idAndVersion.getId().toString(), ObjectType.ENTITY, ACCESS_TYPE.UPDATE)
				.checkAuthorizationOrElseThrow();
		// And they must have upload permission to change table content.
		authorizationManager.canAccess(userInfo, idAndVersion.getId().toString(), ObjectType.ENTITY, ACCESS_TYPE.UPLOAD)
				.checkAuthorizationOrElseThrow();
	}
	
	@Override
	public ColumnModel getColumnModel(EntityField field){
		ValidateArgument.required(field, "field");
		// check the cache.
		ColumnModel model = defaultColumnCache.get(field);
		if(model == null){
			// not in the cache so create the column.
			// this call is idempotent so we won't end up creating multiple ColumnModels with same configuration
			model = columnModelManager.createColumnModel(field.getColumnModel());
			defaultColumnCache.put(field, model);
		}
		return model;
	}
	
	@Override
	public List<ColumnModel> getColumnModels(EntityField... fields) {
		List<ColumnModel> results = new LinkedList<ColumnModel>();
		for(EntityField field: fields){
			results.add(getColumnModel(field));
		}
		return results;
	}


	@Override
	public Set<Long> getAccessibleBenefactors(UserInfo user,
			Set<Long> benefactorIds) {
		return authorizationManager.getAccessibleBenefactors(user, benefactorIds);
	}

	@Override
	public EntityType getTableEntityType(IdAndVersion idAndVersion) {
		return nodeDao.getNodeTypeById(idAndVersion.getId().toString());
	}
	
	@Override
	public Long getViewTypeMask(IdAndVersion idAndVersion){
		return viewScopeDao.getViewTypeMask(idAndVersion.getId());
	}

	@Override
	public List<ColumnModel> getDefaultTableViewColumns(Long viewTypeMaks) {
		ValidateArgument.required(viewTypeMaks, "viewTypeMaks");
		if((viewTypeMaks & ViewTypeMask.File.getMask())> 0) {
			// mask includes files so return file columns.
			return getColumnModels(FILE_VIEW_DEFAULT_COLUMNS);
		}else {
			// mask does not include files so return basic entity columns.
			return getColumnModels(BASIC_ENTITY_DEAFULT_COLUMNS);
		}
	}
	
	/**
	 * Get the ColumnModels for the given entity fields.
	 * @param fields
	 * @return
	 */
	public List<ColumnModel> getColumnModels(List<EntityField> fields){
		List<ColumnModel> results = new LinkedList<ColumnModel>();
		for(EntityField field: fields){
			results.add(getColumnModel(field));
		}
		return results;
	}

	@Override
	public Set<Long> getEntityPath(IdAndVersion idAndVersion) {
		List<EntityHeader> headers = nodeDao.getEntityPath(idAndVersion.getId().toString());
		Set<Long> results = new HashSet<Long>(headers.size());
		for(EntityHeader header: headers){
			results.add(KeyFactory.stringToKey(header.getId()));
		}
		return results;
	}

	@WriteTransaction
	@Override
	public void rebuildTable(UserInfo userInfo, IdAndVersion idAndVersion) {
		if (!userInfo.isAdmin())
			throw new UnauthorizedException("Only an administrator may access this service.");
		// purge
		TableIndexDAO indexDao = tableConnectionFactory.getConnection(idAndVersion);
		if (indexDao != null) {
			indexDao.deleteTable(idAndVersion);
			indexDao.deleteSecondaryTables(idAndVersion);
		}
		String resetToken = tableStatusDAO.resetTableStatusToProcessing(idAndVersion);
		ChangeMessage message = new ChangeMessage();
		message.setChangeType(ChangeType.UPDATE);
		message.setObjectType(getTableType(idAndVersion));
		message.setObjectId(idAndVersion.getId().toString());
		transactionalMessenger.sendMessageAfterCommit(message);
	}

	@Override
	public void validateScopeSize(Set<Long> scopeIds, Long viewTypeMask) {
		if(scopeIds != null){
			// Validation is built into getAllContainerIdsForScope() call
			getAllContainerIdsForScope(scopeIds, viewTypeMask);
		}
	}

	@Override
	public String touchTable(UserInfo user, String tableId) {
		ValidateArgument.required(user, "user");
		ValidateArgument.required(tableId, "tableId");
		return nodeDao.touch(user.getId(), tableId);
	}

	@Override
	public List<ColumnModel> getTableSchema(IdAndVersion idAndVersion) {
		return columnModelManager.getColumnModelsForObject(idAndVersion);
	}

}
