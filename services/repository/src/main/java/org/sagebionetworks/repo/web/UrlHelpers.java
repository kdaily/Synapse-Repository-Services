package org.sagebionetworks.repo.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.PrefixConst;
import org.sagebionetworks.repo.model.ServiceConstants;
import org.sagebionetworks.repo.model.ServiceConstants.AttachmentType;

/**
 * UrlHelpers is responsible for the formatting of all URLs exposed by the
 * service.
 *
 * The various controllers should not be formatting URLs. They should instead
 * call methods in this helper. Its important to keep URL formulation logic in
 * one place to ensure consistency across the space of URLs that this service
 * supports.
 *
 * @author deflaux
 */
public class UrlHelpers {

	private static final Logger log = Logger.getLogger(UrlHelpers.class.getName());
	
	public static final String ACCESS 				= "/access";
	public static final String AUTH_PATH			= "/auth/v1";
	public static final String FILE_PATH			= "/file/v1";
	public static final String REPO_PATH			= "/repo/v1";
	public static final String DOCKER_PATH			= "/docker/v1";
	public static final String DOCKER_REGISTRY_PATH	= "/dockerRegistryListener/v1";

	/**
	 * Used for batch requests
	 */
	public static final String ALL					= "/all";
	
	public static final String BATCH				= "/batch";
	
	public static final String PERMISSIONS 			= "/permissions";

	public static final String ACCESS_TYPE_PARAM	= "accessType";
	
	public static final String BUNDLE				= "/bundle";

	public static final String BUNDLE_V2			= "/bundle2";
	
	public static final String GENERATED_BY			= "/generatedBy";
	
	public static final String GENERATED			= "/generated";

	public static final String CREATE				= "/create";

	/**
	 * All administration URLs must start with this URL or calls will be
	 * blocked when Synapse enters READ_ONLY or DOWN modes for maintenance.
	 */
	public static final String ADMIN = "/admin";

	/**
	 * URL prefix for all objects that are referenced by their ID.
	 * 
	 */
	public static final String ID_PATH_VARIABLE = "id";
	public static final String ID = "/{"+ID_PATH_VARIABLE+"}";
	
	public static final String IDS_PATH_VARIABLE = "ids";
	
	/**
	 * URL prefix for all objects that are referenced by their ID.
	 * 
	 */
	public static final String PROFILE_ID = "/{profileId}";
	
	public static final String PARENT_TYPE 		= "/{parentType}";
	public static final String PARENT_ID 		= "/{parentId}";
	public static final String VERSION_NUMBER 	= "/{versionNumber}";
	public static final String PARENT_TYPE_ID 	= PARENT_TYPE+PARENT_ID;
	
//	public static final String TOKEN_ID = "{tokenId}/{filename}.{mimeType}";
	
	public static final String TYPE = "/type";
	public static final String TYPE_HEADER = "/header";
	
	/**
	 * The URL prefix for all object's Access Control List (ACL).
	 */
	public static final String ACL = "/acl";
	
	public static final String BENEFACTOR = "/benefactor";
	
	/**
	 * The request parameter to enforce ACL inheritance of child nodes.
	 */
	public static final String RECURSIVE = "recursive";
	
	/**
	 * URL suffix for entity annotations
	 * 
	 */
	public static final String ANNOTATIONS = "/annotations";

	/**
	 * URL suffix for entity annotations
	 *
	 */
	public static final String ANNOTATIONS_V2 = "/annotations2";

	/**
	 * URL suffix for locationable entity S3Token
	 * 
	 */
	public static final String S3TOKEN = "/s3Token";

	/**
	 * Used to get the path of a entity.
	 */
	public static final String PATH = "/path";
	
	/**
	 * URL suffix for entity schemas
	 */
	public static final String SCHEMA = "/schema";
	
	public static final String REGISTRY = "/registry";
	/**
	 * The Effective schema is the flattened schema of an entity.
	 */
	public static final String EFFECTIVE_SCHEMA = "/effectiveSchema";
	
	public static final String REST_RESOURCES = "/REST/resources";
	
	public static final String VERSION = "/version";

	public static final String REFERENCED_BY	= "/referencedby";
	
	public static final String ATTACHMENT_S3_TOKEN = "/s3AttachmentToken";
	
	public static final String ATTACHMENT_URL = "/attachmentUrl";
	
	public static final String MIGRATION_OBJECT_ID_PARAM = "id";
	
	
	/**
	 * parameter used by migration services to describe the type of migration 
	 * to be performed
	 */
	public static final String MIGRATION_TYPE_PARAM = "migrationType";
	
	/**
	 * All of the base URLs for Synapse objects
	 */
	public static final String ENTITY	 = PrefixConst.ENTITY;
	public static final String USER_PROFILE  = PrefixConst.USER_PROFILE;
	public static final String HEALTHCHECK = PrefixConst.HEALTHCHECK;
	public static final String VERSIONINFO = PrefixConst.VERSIONINFO;
	public static final String ACTIVITY    = PrefixConst.ACTIVITY;
	public static final String FAVORITE    = PrefixConst.FAVORITE;
	public static final String NOTIFICATION_SETTINGS  = PrefixConst.NOTIFICATION_SETTINGS;
	
	
	public static final String PRINCIPAL = "/principal";
	public static final String PRINCIPAL_AVAILABLE = PRINCIPAL+"/available";
	public static final String ACCOUNT = "/account";
	public static final String EMAIL_VALIDATION = "/emailValidation";
	public static final String ACCOUNT_EMAIL_VALIDATION = ACCOUNT+EMAIL_VALIDATION;
	public static final String ACCOUNT_ID_EMAIL_VALIDATION = ACCOUNT+ID+EMAIL_VALIDATION;
	public static final String EMAIL = "/email";
	public static final String NOTIFICATION_EMAIL = "/notificationEmail";
	public static final String PRINCIPAL_ALIAS = PRINCIPAL + "/alias";
	/**
	 * All of the base URLs for Synapse object batch requests
	 */
	public static final String ENTITY_TYPE = ENTITY+TYPE;
	public static final String ENTITY_TYPE_HEADER = ENTITY+TYPE_HEADER;
	
	// Asynchronous jobs
	public static final String ASYNC_START_REQUEST = "/async/start";
	public static final String ASYNC_GET_REQUEST = "/async/get/{asyncToken}";
	public static final String ASYNCHRONOUS_JOB = "/asynchronous/job";
	public static final String ASYNCHRONOUS_JOB_ID = ASYNCHRONOUS_JOB + "/{jobId}";
	public static final String ASYNCHRONOUS_JOB_CANCEL = ASYNCHRONOUS_JOB_ID + "/cancel";
	public static final String ADMIN_ASYNCHRONOUS_JOB = ADMIN + ASYNCHRONOUS_JOB;
	public static final String ADMIN_ASYNCHRONOUS_JOB_ID = ADMIN + ASYNCHRONOUS_JOB_ID;
	
	public static final String ADMIN_ID_GEN_EXPORT = ADMIN + "/id/generator/export";

	/**
	 * All of the base URLs for Synapse objects with ID.
	 */
	public static final String ENTITY_ID	= ENTITY+ID;
	public static final String USER_PROFILE_ID		= USER_PROFILE+PROFILE_ID;
	public static final String USER_PROFILE_IMAGE = USER_PROFILE_ID+"/image";
	public static final String USER_PROFILE_IMAGE_PREVIEW = USER_PROFILE_IMAGE+"/preview";

	public static final String ENTITY_MD5 = ENTITY + "/md5" + "/{md5}";

	public static final String ENTITY_ALIAS = ENTITY + "/alias" + "/{alias}";
	
	public static final String ENTITY_CHILDREN = ENTITY+"/children";
	public static final String ENTITY_CHILD = ENTITY+"/child";
	
	public static final String ENTITY_BUNDLE = ENTITY+BUNDLE;
	public static final String ENTITY_ID_BUNDLE = ENTITY_ID+BUNDLE;
	public static final String ENTITY_BUNDLE_V2 = ENTITY+BUNDLE_V2;
	public static final String ENTITY_ID_BUNDLE_V2 = ENTITY_ID+BUNDLE_V2;
	public static final String ENTITY_BUNDLE_V2_CREATE = ENTITY+BUNDLE_V2+CREATE;
	public static final String ENTITY_ID_ACL = ENTITY_ID+ACL;
	public static final String ENTITY_ID_ID_BENEFACTOR = ENTITY_ID+BENEFACTOR;

	public static final String FILE= "/file";
	public static final String FILE_PREVIEW = "/filepreview";
	public static final String FILE_HANDLES = "/filehandles";
	public static final String ENTITY_FILE = ENTITY_ID+FILE;
	public static final String ENTITY_FILE_PREVIEW = ENTITY_ID+FILE_PREVIEW;
	public static final String ENTITY_FILE_HANDLES = ENTITY_ID+FILE_HANDLES;
	public static final String S3_FILE_COPY = FILE + "/s3FileCopy";
	public static final String S3_FILE_COPY_ASYNC_START = S3_FILE_COPY + ASYNC_START_REQUEST;
	public static final String S3_FILE_COPY_ASYNC_GET = S3_FILE_COPY + ASYNC_GET_REQUEST;
	public static final String COPY = "/copy";
	public static final String FILE_HANDLES_COPY = FILE_HANDLES + COPY;
	public static final String ENTITY_DATA_TYPE = ENTITY_ID+"/datatype";
	
	public static final String BULK_FILE_DOWNLOAD = FILE + "/bulk";
	public static final String BULK_FILE_DOWNLOAD_ASYNC_START = BULK_FILE_DOWNLOAD + ASYNC_START_REQUEST;
	public static final String BULK_FILE_DOWNLOAD_ASYNC_GET = BULK_FILE_DOWNLOAD + ASYNC_GET_REQUEST;
	public static final String FILE_DOWNLOAD = FILE+ID;
	
	public static final String DOWNLOAD_LIST = "/download/list";
	public static final String DOWNLOAD_LIST_ADD = DOWNLOAD_LIST+"/add";
	public static final String DOWNLOAD_LIST_REMOVE = DOWNLOAD_LIST+"/remove";
	public static final String DOWNLOAD_LIST_ADD_START_ASYNCH = DOWNLOAD_LIST_ADD+ASYNC_START_REQUEST;
	public static final String DOWNLOAD_LIST_ADD_GET_ASYNCH = DOWNLOAD_LIST_ADD+ASYNC_GET_REQUEST;
	
	public static final String DOWNLOAD_ORDER = "/download/order";
	public static final String DOWNLOAD_ORDER_ID = DOWNLOAD_ORDER+"/{orderId}";
	public static final String DOWNLOAD_ORDER_HISTORY = DOWNLOAD_ORDER+"/history";
	
	// multipart upload v2
	public static final String FILE_MULTIPART = FILE+"/multipart";
	public static final String FILE_MULTIPART_UPLOAD_ID = FILE_MULTIPART+"/{uploadId}";
	public static final String FILE_MULTIPART_UPLOAD_ID_PRESIGNED = FILE_MULTIPART_UPLOAD_ID+"/presigned/url/batch";
	public static final String FILE_MULTIPART_UPLOAD_ID_ADD_PART = FILE_MULTIPART_UPLOAD_ID+"/add/{partNumber}";
	public static final String FILE_MULTIPART_UPLOAD_ID_COMPLETE = FILE_MULTIPART_UPLOAD_ID+"/complete";
	
	// version
	public static final String ENTITY_VERSION_FILE = ENTITY_ID+VERSION+VERSION_NUMBER+FILE;
	public static final String ENTITY_VERSION_FILE_PREVIEW = ENTITY_ID+VERSION+VERSION_NUMBER+FILE_PREVIEW;
	public static final String ENTITY_VERSION_FILE_HANDLES = ENTITY_ID+VERSION+VERSION_NUMBER+FILE_HANDLES;
	
	public static final String ENTITY_CONVERT_LOCATIONABLE = ENTITY+"/convertLocationable";
	public static final String ENTITY_CONVERT_LOCATIONABLE_START = ENTITY_CONVERT_LOCATIONABLE+"/start";
	public static final String ENTITY_CONVERT_LOCATIONABLE_GET = ENTITY_CONVERT_LOCATIONABLE+"/{jobId}";
	
	/**
	 * Activity URLs
	 */
	public static final String ACTIVITY_ID = ACTIVITY+ID;
	public static final String ACTIVITY_GENERATED = ACTIVITY_ID+GENERATED;
	/*  
	 * Favorite URLs
	 */
	public static final String FAVORITE_ID = FAVORITE+ID;

	/**
	 * Used to get an entity attachment token
	 */
	public static final String ENTITY_S3_ATTACHMENT_TOKEN = ENTITY_ID+ATTACHMENT_S3_TOKEN;
	/**
	 * The url used to get an attachment URL.
	 */
	public static final String ENTITY_ATTACHMENT_URL = ENTITY_ID+ATTACHMENT_URL;

	// project settings
	public static final String STORAGE_LOCATION = "/storageLocation";
	public static final String STORAGE_LOCATION_BY_ID = "/storageLocation" + ID;
	public static final String PROJECT_SETTINGS = "/projectSettings";
	public static final String PROJECT_SETTINGS_BY_ID = "/projectSettings" + ID;
	public static final String PROJECT_SETTINGS_BY_PROJECT_ID_AND_TYPE = "/projectSettings/{projectId}/type/{type}";

	// Synapse storage report
	public static final String STORAGE_REPORT = "/storageReport";
	public static final String STORAGE_REPORT_ASYNC_START = STORAGE_REPORT + ASYNC_START_REQUEST;
	public static final String STORAGE_REPORT_ASYNC_GET = STORAGE_REPORT + ASYNC_GET_REQUEST;

	/**
	 * The base URL for Synapse objects's type (a.k.a. EntityHeader)
	 */
	public static final String ENTITY_ID_TYPE = ENTITY_ID+TYPE;

	/**
	 * All of the base URLs for Synapse objects's Annotations.
	 */
	public static final String ENTITY_ANNOTATIONS 	= ENTITY_ID+ANNOTATIONS;

	/**
	 * All of the base URLs for Synapse objects's Annotations.
	 */
	public static final String ENTITY_ANNOTATIONS_V2 	= ENTITY_ID+ANNOTATIONS_V2;

	/**
	 * All of the base URLs for locationable entity s3Tokens
	 */
	public static final String ENTITY_S3TOKEN	= ENTITY_ID+S3TOKEN;
	
	/**
	 * Used to get a user profile attachment token
	 */
	public static final String USER_PROFILE_S3_ATTACHMENT_TOKEN = USER_PROFILE_ID+ATTACHMENT_S3_TOKEN;
	
	/**
	 * All of the base URLs for Synapse objects's paths.
	 */
	public static final String ENTITY_PATH		= ENTITY_ID+PATH;

	public static final String ENTITY_VERSION		= ENTITY_ID+VERSION;

	
	public static final String ENTITY_VERSION_NUMBER		= ENTITY_VERSION+VERSION_NUMBER;
	
	/**
	 * Get the annotations of a specific version of an entity
	 */
	public static final String ENTITY_VERSION_ANNOTATIONS =		ENTITY_VERSION_NUMBER+ANNOTATIONS;

	/**
	 * Get the annotations of a specific version of an AnnotaitonV2
	 */
	public static final String ENTITY_VERSION_ANNOTATIONS_V2 =		ENTITY_VERSION_NUMBER+ANNOTATIONS_V2;

	/**
	 * Get the bundle for a specific version of an entity
	 */
	public static final String ENTITY_VERSION_NUMBER_BUNDLE = ENTITY_VERSION_NUMBER+BUNDLE;

	/**
	 * Get the bundle for a specific version of an entity
	 */
	public static final String ENTITY_VERSION_NUMBER_BUNDLE_V2 = ENTITY_VERSION_NUMBER+BUNDLE_V2;

	/**
	 * Get the generating activity for the current version of an entity
	 */
	public static final String ENTITY_GENERATED_BY = ENTITY_ID+GENERATED_BY;
	
	/**
	 * Get the generating activity for a specific version of an entity
	 */
	public static final String ENTITY_VERSION_GENERATED_BY = ENTITY_VERSION_NUMBER+GENERATED_BY;

	/**
	 * DOI (Digital Object Identifier).
	 */
	public static final String DOI = "/doi";
	public static final String DOI_ASSOCIATION = DOI + "/association";
	public static final String DOI_LOCATE = DOI + "/locate";
	public static final String DOI_ASYNC_START = DOI + ASYNC_START_REQUEST;
	public static final String DOI_ASYNC_GET = DOI + ASYNC_GET_REQUEST;
	
	/**
	 * Form  API URIs
	 * 
	 */
	public static final String FORM = "/form";
	public static final String FORM_DATA = FORM+"/data";
	public static final String FORM_GROUP = FORM+"/group";
	public static final String FORM_GROUP_ACL = FORM_GROUP+"/{id}/acl";
	public static final String FORM_DATA_ID = FORM_DATA+"/{id}";
	public static final String FORM_DATA_SUBMIT = FORM_DATA+"/{id}/submit";
	public static final String FORM_LIST = FORM_DATA+"/list";
	public static final String FORM_LIST_REVIEWER = FORM_DATA+"/list/reviewer";
	
	public static final String FORM_DATA_ACCEPT = FORM_DATA_ID+"/accept";
	public static final String FORM_DATA_REJECT = FORM_DATA_ID+"/reject";

	/**
	 * Clears the Synapse DOI table (by administrators only).
	 */
	public static final String ADMIN_DOI_CLEAR = ADMIN + DOI + "/clear";

	/**
	 * The DOI associated with the entity (implies the current version).
	 */
	public static final String ENTITY_DOI = ENTITY_ID + DOI;

	/**
	 * The DOI associated with the entity version.
	 */
	public static final String ENTITY_VERSION_DOI = ENTITY_VERSION_NUMBER + DOI;

 	/**
	 * Gets the root node.
	 */
	public static final String ENTITY_ROOT = ENTITY + "/root";

	/**
	 * Gets the ancestors for the specified node.
	 */
	public static final String ENTITY_ANCESTORS = ENTITY_ID + "/ancestors";

	/**
	 * Gets the descendants for the specified node.
	 */
	public static final String ENTITY_DESCENDANTS = ENTITY_ID + "/descendants";

	/**
	 * Gets the descendants of a particular generation for the specified node.
	 */
	public static final String GENERATION = "generation";

	/**
	 * Gets the descendants of a particular generation for the specified node.
	 */
	public static final String ENTITY_DESCENDANTS_GENERATION = ENTITY_ID + "/descendants/{" + GENERATION + "}";

	/**
	 * Gets the parent for the specified node.
	 */
	public static final String ENTITY_PARENT = ENTITY_ID + "/parent";

	/**
	 * For trash can APIs.
	 */
	public static final String TRASHCAN = "/trashcan";

	/**
	 * Moves an entity to the trash can.
	 */
	public static final String TRASHCAN_TRASH = TRASHCAN + "/trash" + ID;

	/**
	 * Restores an entity from the trash can back to the original parent before it is deleted to the trash can.
	 */
	public static final String TRASHCAN_RESTORE = TRASHCAN + "/restore" + ID;

	/**
	 * Restores an entity from the trash can back to a parent entity.
	 */
	public static final String TRASHCAN_RESTORE_TO_PARENT = TRASHCAN + "/restore" + ID + PARENT_ID;

	/**
	 * Views the current trash can.
	 */
	public static final String TRASHCAN_VIEW = TRASHCAN + "/view";

	/**
	 * Purges the trash can for the current user.
	 */
	public static final String TRASHCAN_PURGE = TRASHCAN + "/purge";
	
	/**
	 * Purges the trash can for the current user of all trash items with no children trash items.
	 */
	public static final String TRASHCAN_PURGE_LEAVES = TRASHCAN + "/purgeleaves";

	/**
	 * Views the current trash can.
	 */
	public static final String TRASHCAN_PURGE_ENTITY = TRASHCAN_PURGE + ID;

	/**
	 * Views everything in the trash can.
	 */
	public static final String ADMIN_TRASHCAN_VIEW = ADMIN + TRASHCAN_VIEW;

	/**
	 * Purges everything in the trash can.
	 */
	public static final String ADMIN_TRASHCAN_PURGE = ADMIN + TRASHCAN_PURGE;
	
	/**
	 * Purges all trash items in the trash can with no children trash items.
	 */
	public static final String ADMIN_TRASHCAN_PURGE_LEAVES = ADMIN + TRASHCAN_PURGE_LEAVES;

	/**
	 * URL path for query controller
	 * 
	 */
	public static final String QUERY = "/query";

	/**
	 * URL path for log controller
	 * 
	 */
	public static final String LOG = "/log";

	/**
	 * URL prefix for Users in the system
	 * 
	 */
	public static final String USER = "/user";
	
	public static final String USER_BUNDLE = USER+BUNDLE;
	
	public static final String USER_BUNDLE_ID = USER+ID+BUNDLE;

	
	/**
	 * URL prefix for User Group model objects
	 * 
	 */
	public static final String USERGROUP = "/userGroup";
	
	public static final String ACCESS_REQUIREMENT = "/accessRequirement";
	public static final String ACCESS_REQUIREMENT_WITH_ENTITY_ID = ENTITY_ID+ACCESS_REQUIREMENT;
	public static final String ACCESS_REQUIREMENT_ID = "/{requirementId}";
	public static final String ACCESS_REQUIREMENT_WITH_REQUIREMENT_ID = ACCESS_REQUIREMENT+ACCESS_REQUIREMENT_ID;
	public static final String ENTITY_LOCK_ACCESS_REQURIEMENT = ENTITY_ID+"/lockAccessRequirement";	

	public static final String ACCESS_REQUIREMENT_CONVERSION = ACCESS_REQUIREMENT+"/conversion";
	public static final String ACCESS_REQUIREMENT_WITH_REQUIREMENT_ID_SUBJECTS = ACCESS_REQUIREMENT_WITH_REQUIREMENT_ID+"/subjects";

	public static final String ENTITY_ACCESS_REQUIREMENT_UNFULFILLED_WITH_ID = ENTITY_ID+"/accessRequirementUnfulfilled";

	public static final String ACCESS_REQUIREMENT_VERSION = ACCESS_REQUIREMENT_WITH_REQUIREMENT_ID + "/version";

	public static final String ACCESS_APPROVAL = "/accessApproval";
	public static final String ACCESS_APPROVALS = "/accessApprovals";
	public static final String ACCESS_APPROVAL_WITH_ENTITY_ID = ENTITY_ID+ACCESS_APPROVAL;
	public static final String ACCESS_APPROVAL_WITH_APPROVAL_ID = ACCESS_APPROVAL+"/{approvalId}";
	public static final String ACCESS_APPROVAL_GROUP = ACCESS_APPROVAL+"/group";
	public static final String ACCESS_APPROVAL_GROUP_REVOKE = ACCESS_APPROVAL_GROUP+"/revoke";

	public static final String ACCESS_APPROVAL_INFO = ACCESS_APPROVAL+"/information";

	/**
	 * URL prefix for Users in a UserGroup
	 * 
	 */
	public static final String RESOURCES = "/resources";
	
	/**
	 * URL prefix for User mirroring service
	 * 
	 */
	public static final String USER_MIRROR = "/userMirror";

	/**
	 * Storage usage summary for the current user.
	 */
	public static final String STORAGE_SUMMARY = "/storageSummary";

	/**
	 * Itemized storage usage for the current user.
	 */
	public static final String STORAGE_DETAILS = "/storageDetails";

	/**
	 * The user whose storage usage is queried.
	 */
	public static final String STORAGE_USER_ID = "userId";

	/**
	 * Storage usage summary for the specified user.
	 */
	public static final String STORAGE_SUMMARY_USER_ID = STORAGE_SUMMARY + "/{" + STORAGE_USER_ID + "}";

	/**
	 * Itemized storage usage for the specified user.
	 */
	public static final String STORAGE_DETAILS_USER_ID = STORAGE_DETAILS + "/{" + STORAGE_USER_ID + "}";

	/**
	 * Itemized storage usage for the specified NODE.
	 */
	public static final String STORAGE_DETAILS_ENTITY_ID = STORAGE_DETAILS + ENTITY_ID;

	/**
	 * Storage usage summary for administrators.
	 */
	public static final String ADMIN_STORAGE_SUMMARY = ADMIN + STORAGE_SUMMARY;

	/**
	 * Storage usage summaries, aggregated by users, for administrators.
	 */
	public static final String ADMIN_STORAGE_SUMMARY_PER_USER = ADMIN_STORAGE_SUMMARY + "/perUser";

	/**
	 * Storage usage summaries, aggregated by entities, for administrators.
	 */
	public static final String ADMIN_STORAGE_SUMMARY_PER_ENTITY = ADMIN_STORAGE_SUMMARY + "/perEntity";

	/**
	 * Public access for Synapse user and group info
	 */
	public static final String USER_GROUP_HEADERS = "/userGroupHeaders";
	
	/**
	 * Public batch request access for Synapse user and group info
	 */
	public static final String USER_GROUP_HEADERS_BATCH = USER_GROUP_HEADERS + BATCH;
	
	/**
	 * Get a batch of headers given aliases.
	 */
	public static final String USER_GROUP_HEADERS_BY_ALIASES = USER_GROUP_HEADERS + "/aliases";
	
	/**
	 * The name of the query parameter for a prefix filter.
	 */
	public static final String PREFIX_FILTER = "prefix";
	
	/**
	 * The other header key used to request JSONP
	 */
	public static final String REQUEST_CALLBACK_JSONP = "callback";
	
	
	/**
	 * The stack status of synapse 
	 */
	public static final String ADMIN_STACK_STATUS	= ADMIN+"/synapse/status";
	public static final String STACK_STATUS			=  "/status";

	/**
	 * List change messages.
	 */
	public static final String CHANGE_MESSAGES			= ADMIN+"/messages";
	
	/**
	 * Rebroadcast changes messages to a Queue
	 */
	public static final String REBROADCAST_MESSAGES 		= CHANGE_MESSAGES+"/rebroadcast";
	public static final String REFIRE_MESSAGES				= CHANGE_MESSAGES+"/refire";
	public static final String CURRENT_NUMBER				= CHANGE_MESSAGES+"/currentnumber";
	public static final String CREATE_OR_UPDATE				= CHANGE_MESSAGES+"/createOrUpdate";
	
	// Messaging URLs
	public static final String MESSAGE                    = "/message";
	public static final String FORWARD                    = "/forward";
	public static final String CONVERSATION               = "/conversation";
	public static final String MESSAGE_STATUS             = MESSAGE + "/status";
	public static final String MESSAGE_INBOX              = MESSAGE + "/inbox";
	public static final String MESSAGE_OUTBOX             = MESSAGE + "/outbox";
	public static final String MESSAGE_INBOX_FILTER_PARAM = "inboxFilter";
	public static final String MESSAGE_ORDER_BY_PARAM     = "orderBy";
	public static final String MESSAGE_DESCENDING_PARAM   = "descending";
	public static final String MESSAGE_ID_PATH_VAR        = "messageId";
	public static final String MESSAGE_ID                 = MESSAGE + "/{" + MESSAGE_ID_PATH_VAR + "}";
	public static final String MESSAGE_ID_FORWARD         = MESSAGE_ID + FORWARD;
	public static final String MESSAGE_ID_CONVERSATION    = MESSAGE_ID + CONVERSATION;
	public static final String MESSAGE_ID_FILE            = MESSAGE_ID + FILE;
	public static final String ENTITY_ID_MESSAGE          = ENTITY_ID + MESSAGE;
	public static final String CLOUDMAILIN_MESSAGE        = "/cloudMailInMessage";
	public static final String CLOUDMAILIN_AUTHORIZATION  = "/cloudMailInAuthorization";
	
	/**
	 * Mapping of dependent property classes to their URL suffixes
	 */
	@SuppressWarnings("rawtypes")
	private static final Map<Class, String> PROPERTY2URLSUFFIX;

	/**
	 * The parameter for a resource name.
	 */
	public static final String RESOURCE_ID = "resourceId";
	
	
	public static final String MIGRATION = "/migration";
	public static final String MIGRATION_COUNTS = MIGRATION+"/counts";
	public static final String MIGRATION_COUNT = MIGRATION+"/count";
	public static final String MIGRATION_DELTA = MIGRATION+"/delta";
	public static final String MIGRATION_PRIMARY = MIGRATION+"/primarytypes";
	public static final String MIGRATION_PRIMARY_NAMES = MIGRATION_PRIMARY + "/names";
	public static final String MIGRATION_TYPES = MIGRATION+"/types";
	public static final String MIGRATION_TYPE_NAMES = MIGRATION_TYPES + "/names";
	public static final String MIGRATION_RANGE_CHECKSUM = MIGRATION+"/rangechecksum";
	public static final String MIGRATION_TYPE_CHECKSUM = MIGRATION+"/typechecksum";

	/**
	 * Used by AdministrationController service to say whether object dependencies should be calculated
	 * when listing objects to back up.
	 */
	public static final String INCLUDE_DEPENDENCIES_PARAM = "includeDependencies";
	
	// Evaluation URLs
	public static final String STATUS = "status";
	public static final String EVALUATION = "/evaluation";
	public static final String EVALUATION_ID_PATH_VAR_WITHOUT_BRACKETS = "evalId";
	public static final String EVALUATION_ID_PATH_VAR = "{"+EVALUATION_ID_PATH_VAR_WITHOUT_BRACKETS+"}";
	public static final String EVALUATION_WITH_ID = EVALUATION + "/" + EVALUATION_ID_PATH_VAR;
	public static final String EVALUATION_WITH_CONTENT_SOURCE = ENTITY_ID + EVALUATION;
	public static final String EVALUATION_WITH_NAME = EVALUATION + "/name/{name}";
	public static final String EVALUATION_COUNT = EVALUATION + "/count";
	public static final String EVALUATION_AVAILABLE = EVALUATION+"/available";
	
	public static final String PARTICIPANT = EVALUATION_WITH_ID + "/participant";
	public static final String PARTICIPANT_WITH_ID = PARTICIPANT + "/{partId}";
	public static final String PARTICIPANT_COUNT = PARTICIPANT + "/count";
	
	public static final String SUBMISSION = EVALUATION + "/submission";
	public static final String SUBMISSION_WITH_ID = SUBMISSION + "/{subId}";
	public static final String SUBMISSION_STATUS = SUBMISSION_WITH_ID + "/status";
	public static final String EVALUATION_STATUS_BATCH = EVALUATION_WITH_ID + "/statusBatch";
	public static final String SUBMISSION_WITH_EVAL_ID = EVALUATION_WITH_ID + "/submission";
	public static final String SUBMISSION_WITH_EVAL_ID_BUNDLE = SUBMISSION_WITH_EVAL_ID + BUNDLE;
	public static final String SUBMISSION_WITH_EVAL_ID_ADMIN = SUBMISSION_WITH_EVAL_ID + ALL;
	public static final String SUBMISSION_STATUS_WITH_EVAL_ID = SUBMISSION_WITH_EVAL_ID + "/" + STATUS + ALL;
	public static final String SUBMISSION_WITH_EVAL_ID_ADMIN_BUNDLE = SUBMISSION_WITH_EVAL_ID + BUNDLE + ALL;
	public static final String SUBMISSION_FILE = SUBMISSION_WITH_ID + FILE + "/{fileHandleId}";
	public static final String SUBMISSION_COUNT = SUBMISSION_WITH_EVAL_ID + "/count";
	public static final String SUBMISSION_CONTRIBUTOR = SUBMISSION_WITH_ID+"/contributor";
	
	public static final String ACCESS_REQUIREMENT_WITH_EVALUATION_ID = EVALUATION_WITH_ID+ACCESS_REQUIREMENT;
	public static final String EVALUATION_ACCESS_REQUIREMENT_UNFULFILLED_WITH_ID = EVALUATION_WITH_ID+"/accessRequirementUnfulfilled";
	public static final String ACCESS_APPROVAL_WITH_EVALUATION_ID = EVALUATION_WITH_ID+ACCESS_APPROVAL;

	public static final String EVALUATION_ACL = EVALUATION + ACL;
	public static final String EVALUATION_ID_ACL = EVALUATION + "/" + EVALUATION_ID_PATH_VAR + ACL;
	public static final String EVALUATION_ID_PERMISSIONS = EVALUATION + "/" + EVALUATION_ID_PATH_VAR + PERMISSIONS;
	
	public static final String EVALUATION_QUERY = SUBMISSION + QUERY;
	public static final String EVALUATION_SUBMISSION_CANCALLATION = SUBMISSION_WITH_ID + "/cancellation";

	// Wiki URL
	public static final String WIKI = "/wiki";
	public static final String WIKI_KEY = "/wikikey";
	public static final String WIKI_HEADER_TREE = "/wikiheadertree";
	public static final String ATTACHMENT = "/attachment";
	public static final String ATTACHMENT_PREVIEW = "/attachmentpreview";
	public static final String ATTACHMENT_HANDLES = "/attachmenthandles";
	public static final String WIKI_WITH_ID = WIKI + "/{wikiId}";
	// Entity
	public static final String ENTITY_OWNER_ID = ENTITY+"/{ownerId}";
	public static final String ENTITY_WIKI = ENTITY_OWNER_ID + WIKI;
	public static final String ENTITY_WIKI_KEY = ENTITY_OWNER_ID + WIKI_KEY;
	public static final String ENTITY_WIKI_TREE = ENTITY_OWNER_ID + WIKI_HEADER_TREE;
	public static final String ENTITY_WIKI_ID = ENTITY_OWNER_ID + WIKI_WITH_ID;
	public static final String ENTITY_WIKI_ID_ATTCHMENT_HANDLE = ENTITY_OWNER_ID + WIKI_WITH_ID+ATTACHMENT_HANDLES;
	public static final String ENTITY_WIKI_ID_ATTCHMENT_FILE = ENTITY_OWNER_ID + WIKI_WITH_ID+ATTACHMENT;
	public static final String ENTITY_WIKI_ID_ATTCHMENT_FILE_PREVIEW = ENTITY_OWNER_ID + WIKI_WITH_ID+ATTACHMENT_PREVIEW;
	// Evaluation
	public static final String EVALUATION_OWNER_ID = EVALUATION+"/{ownerId}";
	public static final String EVALUATION_WIKI = EVALUATION_OWNER_ID+ WIKI;
	public static final String EVALUATION_WIKI_KEY = EVALUATION_OWNER_ID+ WIKI_KEY;
	public static final String EVALUATION_WIKI_TREE = EVALUATION_OWNER_ID + WIKI_HEADER_TREE;
	public static final String EVALUATION_WIKI_ID =EVALUATION_OWNER_ID + WIKI_WITH_ID;
	public static final String EVALUATION_WIKI_ID_ATTCHMENT_HANDLE =EVALUATION_OWNER_ID + WIKI_WITH_ID+ATTACHMENT_HANDLES;
	public static final String EVALUATION_WIKI_ID_ATTCHMENT_FILE =EVALUATION_OWNER_ID + WIKI_WITH_ID+ATTACHMENT;
	public static final String EVALUATION_WIKI_ID_ATTCHMENT_FILE_PREVIEW =EVALUATION_OWNER_ID + WIKI_WITH_ID+ATTACHMENT_PREVIEW;
	// Access Requirement
	public static final String ACCESS_REQUIREMENT_OWNER_ID = "/access_requirement/{ownerId}";
	public static final String ACCESS_REQUIREMENT_WIKI = ACCESS_REQUIREMENT_OWNER_ID+ WIKI;
	public static final String ACCESS_REQUIREMENT_WIKI_ID = ACCESS_REQUIREMENT_OWNER_ID+ WIKI_WITH_ID;
	public static final String ACCESS_REQUIREMENT_WIKI_KEY = ACCESS_REQUIREMENT_OWNER_ID+ WIKI_KEY;
	

	// V2 Wiki URL
	public static final String WIKI_V2 = "/wiki2";
	public static final String WIKI_HEADER_TREE_V2 = "/wikiheadertree2";
	public static final String WIKI_V2_ORDER_HINT = "/wiki2orderhint";
	public static final String WIKI_HISTORY_V2 = "/wikihistory";
	public static final String ATTACHMENT_V2 = "/attachment";
	public static final String ATTACHMENT_PREVIEW_V2 = "/attachmentpreview";
	public static final String ATTACHMENT_HANDLES_V2 = "/attachmenthandles";
	public static final String WIKI_WITH_ID_V2 = WIKI_V2 + "/{wikiId}";
	public static final String WIKI_VERSION_V2 = "/{wikiVersion}";
	public static final String MARKDOWN_V2 = "/markdown";
	public static final String MARKDOWN_V2_VERSION_DELETE = "/deleteversion";
	// Entity
	public static final String ENTITY_OWNER_ID_V2 = ENTITY+"/{ownerId}";
	public static final String ENTITY_WIKI_V2 = ENTITY_OWNER_ID_V2 + WIKI_V2;
	public static final String ENTITY_WIKI_V2_ORDER_HINT = ENTITY_OWNER_ID_V2 + WIKI_V2_ORDER_HINT;
	public static final String ENTITY_WIKI_TREE_V2 = ENTITY_OWNER_ID_V2 + WIKI_HEADER_TREE_V2;
	public static final String ENTITY_WIKI_ID_V2 = ENTITY_OWNER_ID_V2 + WIKI_WITH_ID_V2;
	public static final String ENTITY_WIKI_ID_ATTCHMENT_HANDLE_V2 = ENTITY_OWNER_ID_V2 + WIKI_WITH_ID_V2+ATTACHMENT_HANDLES_V2;
	public static final String ENTITY_WIKI_ID_ATTCHMENT_FILE_V2 = ENTITY_OWNER_ID_V2 + WIKI_WITH_ID_V2+ATTACHMENT_V2;
	public static final String ENTITY_WIKI_ID_ATTCHMENT_FILE_PREVIEW_V2 = ENTITY_OWNER_ID_V2 + WIKI_WITH_ID_V2+ATTACHMENT_PREVIEW_V2;
	public static final String ENTITY_WIKI_HISTORY_V2 = ENTITY_WIKI_ID_V2 + WIKI_HISTORY_V2;
	public static final String ENTITY_WIKI_ID_AND_VERSION_V2 = ENTITY_OWNER_ID_V2+WIKI_WITH_ID_V2+WIKI_VERSION_V2;
	public static final String ENTITY_WIKI_ID_MARKDOWN_FILE_V2 = ENTITY_OWNER_ID_V2 + WIKI_WITH_ID_V2 + MARKDOWN_V2;
	// Evaluation
	public static final String EVALUATION_OWNER_ID_V2 = EVALUATION+"/{ownerId}";
	public static final String EVALUATION_WIKI_V2 = EVALUATION_OWNER_ID_V2+ WIKI_V2;
	public static final String EVALUATION_WIKI_TREE_V2 = EVALUATION_OWNER_ID_V2 + WIKI_HEADER_TREE_V2;
	public static final String EVALUATION_WIKI_ID_V2 =EVALUATION_OWNER_ID_V2 + WIKI_WITH_ID_V2;
	public static final String EVALUATION_WIKI_ID_ATTCHMENT_HANDLE_V2 =EVALUATION_OWNER_ID_V2 + WIKI_WITH_ID_V2+ATTACHMENT_HANDLES_V2;
	public static final String EVALUATION_WIKI_ID_ATTCHMENT_FILE_V2 =EVALUATION_OWNER_ID_V2 + WIKI_WITH_ID_V2+ATTACHMENT_V2;
	public static final String EVALUATION_WIKI_ID_ATTCHMENT_FILE_PREVIEW_V2 =EVALUATION_OWNER_ID_V2 + WIKI_WITH_ID_V2+ATTACHMENT_PREVIEW_V2;
	public static final String EVALUATION_WIKI_HISTORY_V2 = EVALUATION_WIKI_ID_V2 + WIKI_HISTORY_V2;
	public static final String EVALUATION_WIKI_ID_AND_VERSION_V2 = EVALUATION_OWNER_ID_V2+WIKI_WITH_ID_V2+WIKI_VERSION_V2;
	public static final String EVALUATION_WIKI_ID_MARKDOWN_FILE_V2 = EVALUATION_OWNER_ID_V2 + WIKI_WITH_ID_V2 + MARKDOWN_V2;
	// Access Requirement
	public static final String ACCESS_REQUIREMENT_OWNER_ID_V2 = "/access_requirement/{ownerId}";
	public static final String ACCESS_REQUIREMENT_WIKI_V2 = ACCESS_REQUIREMENT_OWNER_ID_V2 + WIKI_V2;
	public static final String ACCESS_REQUIREMENT_WIKI_TREE_V2 = ACCESS_REQUIREMENT_OWNER_ID_V2 + WIKI_HEADER_TREE_V2;
	public static final String ACCESS_REQUIREMENT_WIKI_ID_V2 = ACCESS_REQUIREMENT_OWNER_ID_V2 + WIKI_WITH_ID_V2;
	public static final String ACCESS_REQUIREMENT_WIKI_ID_ATTCHMENT_HANDLE_V2 = ACCESS_REQUIREMENT_OWNER_ID_V2 + WIKI_WITH_ID_V2+ATTACHMENT_HANDLES_V2;
	public static final String ACCESS_REQUIREMENT_WIKI_ID_ATTCHMENT_FILE_V2 = ACCESS_REQUIREMENT_OWNER_ID_V2 + WIKI_WITH_ID_V2+ATTACHMENT_V2;
	public static final String ACCESS_REQUIREMENT_WIKI_ID_ATTCHMENT_FILE_PREVIEW_V2 = ACCESS_REQUIREMENT_OWNER_ID_V2 + WIKI_WITH_ID_V2+ATTACHMENT_PREVIEW_V2;
	public static final String ACCESS_REQUIREMENT_WIKI_HISTORY_V2 = ACCESS_REQUIREMENT_WIKI_ID_V2 + WIKI_HISTORY_V2;
	public static final String ACCESS_REQUIREMENT_WIKI_ID_AND_VERSION_V2 = ACCESS_REQUIREMENT_OWNER_ID_V2+WIKI_WITH_ID_V2+WIKI_VERSION_V2;
	public static final String ACCESS_REQUIREMENT_WIKI_ID_MARKDOWN_FILE_V2 = ACCESS_REQUIREMENT_OWNER_ID_V2 + WIKI_WITH_ID_V2 + MARKDOWN_V2;

	// Tables
	public static final String COLUMN = "/column";
	public static final String COLUMN_BATCH = COLUMN + "/batch";
	public static final String COLUMN_TABLE_VIEW_DEFAULT = COLUMN + "/tableview/defaults";
	public static final String COLUMN_TABLE_VIEW_DEFAULT_TYPE = COLUMN_TABLE_VIEW_DEFAULT+"/{viewtype}";
	public static final String ROW_ID = "/row/{rowId}";
	public static final String ROW_VERSION = "/version/{versionNumber}";
	public static final String TABLE = "/table";
	public static final String COLUMN_ID = COLUMN+"/{columnId}";
	public static final String ENTITY_COLUMNS = ENTITY_ID+COLUMN;
	public static final String ENTITY_TABLE = ENTITY_ID+TABLE;
	public static final String ENTITY_TABLE_PARTIAL = ENTITY_TABLE + "/partial";
	public static final String ENTITY_TABLE_DELETE_ROWS = ENTITY_ID + TABLE + "/deleteRows";
	public static final String ENTITY_TABLE_GET_ROWS = ENTITY_ID + TABLE + "/getRows";
	public static final String ENTITY_TABLE_FILE_HANDLES = ENTITY_TABLE + FILE_HANDLES;
	public static final String ENTITY_TABLE_FILE = ENTITY_TABLE + COLUMN_ID + ROW_ID + ROW_VERSION + FILE;
	public static final String ENTITY_TABLE_FILE_PREVIEW = ENTITY_TABLE + COLUMN_ID + ROW_ID + ROW_VERSION + FILE_PREVIEW;
	public static final String TABLE_QUERY = TABLE+"/query";
	public static final String TABLE_QUERY_ASYNC_START = TABLE_QUERY + ASYNC_START_REQUEST;
	public static final String TABLE_QUERY_ASYNC_GET = TABLE_QUERY + ASYNC_GET_REQUEST;
	public static final String TABLE_QUERY_NEXT_PAGE = TABLE_QUERY + "/nextPage";
	public static final String TABLE_QUERY_NEXT_PAGE_ASYNC_START = TABLE_QUERY_NEXT_PAGE + ASYNC_START_REQUEST;
	public static final String TABLE_QUERY_NEXT_PAGE_ASYNC_GET = TABLE_QUERY_NEXT_PAGE + ASYNC_GET_REQUEST;
	public static final String TABLE_DOWNLOAD_CSV = TABLE + "/download/csv";
	public static final String TABLE_DOWNLOAD_CSV_ASYNC_START = TABLE_DOWNLOAD_CSV + ASYNC_START_REQUEST;
	public static final String TABLE_DOWNLOAD_CSV_ASYNC_GET = TABLE_DOWNLOAD_CSV + ASYNC_GET_REQUEST;
	public static final String TABLE_UPLOAD_CSV = TABLE + "/upload/csv";
	public static final String TABLE_UPLOAD_CSV_ASYNC_START = TABLE_UPLOAD_CSV + ASYNC_START_REQUEST;
	public static final String TABLE_UPLOAD_CSV_ASYNC_GET = TABLE_UPLOAD_CSV + ASYNC_GET_REQUEST;
	public static final String TABLE_UPLOAD_CSV_PREVIEW = TABLE + "/upload/csv/preview";
	public static final String TABLE_UPLOAD_CSV_PREVIEW_ASYNC_START = TABLE_UPLOAD_CSV_PREVIEW + ASYNC_START_REQUEST;
	public static final String TABLE_UPLOAD_CSV_PREVIEW_ASYNC_GET = TABLE_UPLOAD_CSV_PREVIEW + ASYNC_GET_REQUEST;
	public static final String TABLE_APPEND = TABLE+"/append";
	public static final String TABLE_APPEND_ROW_ASYNC_START = TABLE_APPEND + ASYNC_START_REQUEST;
	public static final String TABLE_APPEND_ROW_ASYNC_GET = TABLE_APPEND + ASYNC_GET_REQUEST;
	public static final String ENTITY_TABLE_APPEND = ENTITY_TABLE+"/append";
	public static final String ENTITY_TABLE_APPEND_ROW_ASYNC_START = ENTITY_TABLE_APPEND + ASYNC_START_REQUEST;
	public static final String ENTITY_TABLE_APPEND_ROW_ASYNC_GET = ENTITY_TABLE_APPEND + ASYNC_GET_REQUEST;
	public static final String ENTITY_TABLE_TRANSACTION = ENTITY_TABLE+"/transaction";
	public static final String ENTITY_TABLE_TRANSACTION_ASYNC_START = ENTITY_TABLE_TRANSACTION + ASYNC_START_REQUEST;
	public static final String ENTITY_TABLE_TRANSACTION_ASYNC_GET = ENTITY_TABLE_TRANSACTION + ASYNC_GET_REQUEST;
	public static final String ENTITY_TABLE_QUERY = ENTITY_TABLE+"/query";
	public static final String ENTITY_TABLE_QUERY_ASYNC_START = ENTITY_TABLE_QUERY + ASYNC_START_REQUEST;
	public static final String ENTITY_TABLE_QUERY_ASYNC_GET = ENTITY_TABLE_QUERY + ASYNC_GET_REQUEST;
	public static final String ENTITY_TABLE_QUERY_NEXT_PAGE = ENTITY_TABLE_QUERY + "/nextPage";
	public static final String ENTITY_TABLE_QUERY_NEXT_PAGE_ASYNC_START = ENTITY_TABLE_QUERY_NEXT_PAGE + ASYNC_START_REQUEST;
	public static final String ENTITY_TABLE_QUERY_NEXT_PAGE_ASYNC_GET = ENTITY_TABLE_QUERY_NEXT_PAGE + ASYNC_GET_REQUEST;
	public static final String ENTITY_TABLE_DOWNLOAD_CSV = ENTITY_TABLE + "/download/csv";
	public static final String ENTITY_TABLE_DOWNLOAD_CSV_ASYNC_START = ENTITY_TABLE_DOWNLOAD_CSV + ASYNC_START_REQUEST;
	public static final String ENTITY_TABLE_DOWNLOAD_CSV_ASYNC_GET = ENTITY_TABLE_DOWNLOAD_CSV + ASYNC_GET_REQUEST;
	public static final String ENTITY_TABLE_UPLOAD_CSV = ENTITY_TABLE + "/upload/csv";
	public static final String ENTITY_TABLE_UPLOAD_CSV_ASYNC_START = ENTITY_TABLE_UPLOAD_CSV + ASYNC_START_REQUEST;
	public static final String ENTITY_TABLE_UPLOAD_CSV_ASYNC_GET = ENTITY_TABLE_UPLOAD_CSV + ASYNC_GET_REQUEST;
	public static final String TABLE_COLUMNS_OF_SCOPE = COLUMN+"/view/scope";
	public static final String TABLE_SQL_TRANSFORM = TABLE+"/sql/transform";
	public static final String TABLE_SNAPSHOT = ENTITY_TABLE+"/snapshot";

	public static final String ADMIN_TABLE_REBUILD = ADMIN + ENTITY_TABLE + "/rebuild";
	public static final String ADMIN_TABLE_ADD_INDEXES = ADMIN + ENTITY_TABLE + "/addindexes";
	public static final String ADMIN_TABLE_REMOVE_INDEXES = ADMIN + ENTITY_TABLE + "/removeindexes";

	// Team
	public static final String TEAM = "/team";
	public static final String TEAM_LIST = "/teamList";
	public static final String TEAM_ID = TEAM+ID;
	public static final String USER_TEAM = USER+ID+TEAM;
	public static final String USER_TEAM_IDS = USER+ID+TEAM+"/id";
	public static final String NAME_FRAGMENT_FILTER = "fragment";
	public static final String MEMBER_TYPE_FILTER = "memberType";
	public static final String TEAM_ID_ICON = TEAM_ID+"/icon";
	private static final String MEMBER = "/member";
	public static final String PRINCIPAL_ID_PATH_VARIABLE = "principalId";
	public static final String PRINCIPAL_ID = "/{"+PRINCIPAL_ID_PATH_VARIABLE+"}";
	public static final String TEAM_ID_MEMBER = TEAM_ID+MEMBER;
	public static final String TEAM_MEMBER_LIST = TEAM_ID_MEMBER+"List";
	public static final String USER_TEAM_MEMBER_LIST = USER+ID+MEMBER+"List";
	public static final String TEAM_MEMBER = TEAM+"Member";
	public static final String TEAM_ID_MEMBER_ID = TEAM_ID_MEMBER+PRINCIPAL_ID;
	public static final String TEAM_ID_MEMBER_ID_PERMISSION = TEAM_ID_MEMBER+PRINCIPAL_ID+"/permission";
	public static final String TEAM_PERMISSION_REQUEST_PARAMETER = "isAdmin";
	public static final String TEAM_ID_MEMBER_ID_MEMBERSHIP_STATUS = TEAM_ID_MEMBER+PRINCIPAL_ID+"/membershipStatus";
	// 	Team URIs for JSONP
	public static final String TEAMS = "/teams";
	public static final String TEAM_MEMBERS_ID = "/teamMembers"+ID;
	public static final String TEAM_MEMBERS_COUNT_ID = "/teamMembers/count"+ID;
	public static final String TEAM_ACL = TEAM+"/acl";
	public static final String TEAM_ID_ACL = TEAM_ID+"/acl";
	
	public static final String ACCESS_REQUIREMENT_WITH_TEAM_ID = TEAM_ID+ACCESS_REQUIREMENT;
	public static final String TEAM_ACCESS_REQUIREMENT_UNFULFILLED_WITH_ID = TEAM_ID+"/accessRequirementUnfulfilled";
	public static final String ACCESS_APPROVAL_WITH_TEAM_ID = TEAM_ID+ACCESS_APPROVAL;

	// membership invitation
	public static final String MEMBERSHIP_INVITATION = "/membershipInvitation";
	public static final String MEMBERSHIP_INVITATION_ID = MEMBERSHIP_INVITATION+ID;
	public static final String OPEN_MEMBERSHIP_INVITATION_BY_USER = USER+ID+"/openInvitation";
	public static final String OPEN_MEMBERSHIP_INVITATION_BY_TEAM = TEAM+ID+"/openInvitation";
	public static final String TEAM_ID_REQUEST_PARAMETER = "teamId";
	public static final String INVITEE_ID_REQUEST_PARAMETER = "inviteeId";
	public static final String OPEN_MEMBERSHIP_INVITATION_COUNT = MEMBERSHIP_INVITATION + "/openInvitationCount";
	public static final String MEMBERSHIP_INVITATION_VERIFY_INVITEE = MEMBERSHIP_INVITATION_ID + "/inviteeVerificationSignedToken";
	public static final String MEMBERSHIP_INVITATION_UPDATE_INVITEE_ID = MEMBERSHIP_INVITATION_ID + "/inviteeId";
	// membership request
	public static final String MEMBERSHIP_REQUEST = "/membershipRequest";
	public static final String MEMBERSHIP_REQUEST_ID = MEMBERSHIP_REQUEST+ID;
	public static final String OPEN_MEMBERSHIP_REQUEST_FOR_TEAM = TEAM_ID+"/openRequest";
	public static final String OPEN_MEMBERSHIP_REQUEST_FOR_USER = USER+ID+"/openRequest";
	public static final String REQUESTOR_ID_REQUEST_PARAMETER = "requestorId";
	public static final String OPEN_MEMBERSHIP_REQUEST_COUNT = MEMBERSHIP_REQUEST + "/openRequestCount";
	
	public static final String TEAM_SUBMISSION_ELIGIBILITY = EVALUATION_WITH_ID +TEAM_ID+
			"/submissionEligibility";
	
	/**
	 * Challenge URIs
	 */
	public static final String CHALLENGE = "/challenge";
	public static final String ENTITY_ID_CHALLENGE = ENTITY_ID+CHALLENGE;
	public static final String CHALLENGE_ID_PATH_VARIABLE = "challengeId";
	public static final String CHALLENGE_ID = "/{"+CHALLENGE_ID_PATH_VARIABLE+"}";
	public static final String CHALLENGE_CHALLENGE_ID = CHALLENGE+CHALLENGE_ID;
	public static final String CHALLENGE_CHAL_ID_PARTICIPANT = CHALLENGE+CHALLENGE_ID+"/participant";
	public static final String CHALLENGE_CHAL_ID_SUBMISSION_TEAMS = CHALLENGE+CHALLENGE_ID+"/submissionTeams";
	public static final String CHALLENGE_TEAM = "/challengeTeam";
	public static final String CHALLENGE_CHAL_ID_CHAL_TEAM = CHALLENGE+CHALLENGE_ID+CHALLENGE_TEAM;
	public static final String CHALLENGE_TEAM_ID_PATH_VARIABLE = "challengeTeamId";
	public static final String CHALLENGE_TEAM_ID = "/{"+CHALLENGE_TEAM_ID_PATH_VARIABLE+"}";
	public static final String CHALLENGE_CHAL_ID_CHAL_TEAM_CHAL_TEAM_ID = 
			CHALLENGE+CHALLENGE_ID+CHALLENGE_TEAM+CHALLENGE_TEAM_ID;
	public static final String CHALLENGE_TEAM_CHAL_TEAM_ID = 
			CHALLENGE_TEAM+CHALLENGE_TEAM_ID;
	public static final String CHALLENGE_CHAL_ID_REGISTRATABLE_TEAM = CHALLENGE+CHALLENGE_ID+"/registratableTeam";

	/*
	 * Project URLs
	 */
	@Deprecated
	public static final String MY_PROJECTS = PrefixConst.PROJECT;
	@Deprecated
	public static final String PROJECTS_FOR_USER = PrefixConst.PROJECT + USER + "/{principalId}";
	@Deprecated
	public static final String PROJECTS_FOR_TEAM = PrefixConst.PROJECT + TEAM + "/{teamId}";

	public static final String PROJECTS = "/projects/{type}";
	public static final String PROJECTS_USER = PROJECTS + USER + "/{principalId}";
	public static final String PROJECTS_TEAM = PROJECTS + TEAM + "/{teamId}";
	public static final String PROJECTS_SORT_PARAM = "sort";
	public static final String PROJECTS_SORT_DIRECTION_PARAM = "sortDirection";

	// certified user services
	public static final String CERTIFIED_USER_TEST = "/certifiedUserTest";
	public static final String CERTIFIED_USER_TEST_RESPONSE = "/certifiedUserTestResponse";
	public static final String CERTIFIED_USER_TEST_RESPONSE_WITH_ID = "/certifiedUserTestResponse"+ID;
	public static final String CERTIFIED_USER_PASSING_RECORD_WITH_ID = USER+ID+"/certifiedUserPassingRecord";
	public static final String CERTIFIED_USER_PASSING_RECORDS_WITH_ID = USER+ID+"/certifiedUserPassingRecords";
	public static final String CERTIFIED_USER_STATUS = USER+ID+"/certificationStatus";
	
	
	// verified user services
	public static final String VERIFICATION_SUBMISSION = "/verificationSubmission";
	public static final String VERIFICATION_SUBMISSION_ID = VERIFICATION_SUBMISSION+ID;
	public static final String VERIFICATION_SUBMISSION_ID_STATE = "/verificationSubmission"+ID+"/state";

	// Discussion Services
	public static final String FORUM = "/forum";
	public static final String PROJECT_ID = "/{projectId}";
	public static final String PROJECT = "/project";
	public static final String PROJECT_PROJECT_ID_FORUM = PROJECT+PROJECT_ID+FORUM;
	public static final String FORUM_ID = "/{forumId}";
	public static final String FORUM_FORUM_ID = FORUM+FORUM_ID;
	public static final String THREADS = "/threads";
	public static final String FORUM_FORUM_ID_THREADS = FORUM+FORUM_ID+THREADS;
	public static final String THREAD_COUNT = "/threadcount";
	public static final String FORUM_FORUM_ID_THREAD_COUNT = FORUM+FORUM_ID+THREAD_COUNT;
	public static final String THREAD = "/thread";
	public static final String THREAD_ID = "/{threadId}";
	public static final String THREAD_THREAD_ID = THREAD+THREAD_ID;
	public static final String THREAD_THREAD_ID_RESTORE = THREAD+THREAD_ID+"/restore";
	public static final String PIN = "/pin";
	public static final String UNPIN = "/unpin";
	public static final String THREAD_THREAD_ID_PIN = THREAD_THREAD_ID+PIN;
	public static final String THREAD_THREAD_ID_UNPIN = THREAD_THREAD_ID+UNPIN;
	public static final String TITLE = "/title";
	public static final String THREAD_THREAD_ID_TITLE = THREAD_THREAD_ID+TITLE;
	public static final String DISCUSSION_MESSAGE = "/message";
	public static final String THREAD_THREAD_ID_MESSAGE = THREAD_THREAD_ID+DISCUSSION_MESSAGE;
	public static final String REPLY = "/reply";
	public static final String REPLY_ID = "/{replyId}";
	public static final String REPLY_REPLY_ID = REPLY+REPLY_ID;
	public static final String REPLY_REPLY_ID_MESSAGE = REPLY_REPLY_ID+DISCUSSION_MESSAGE;
	public static final String REPLIES = "/replies";
	public static final String THREAD_THREAD_ID_REPLIES = THREAD_THREAD_ID+REPLIES;
	public static final String REPLY_COUNT = "/replycount";
	public static final String THREAD_THREAD_ID_REPLY_COUNT = THREAD_THREAD_ID+REPLY_COUNT;
	public static final String URL = "/messageUrl";
	public static final String THREAD_URL = THREAD+URL;
	public static final String REPLY_URL = REPLY+URL;
	public static final String ENTITY_ID_THREADS = ENTITY_ID+THREADS;
	public static final String THREAD_COUNTS = "/threadcounts";
	public static final String ENTITY_THREAD_COUNTS = ENTITY+THREAD_COUNTS;
	public static final String MODERATORS = "/moderators";
	public static final String FORUM_FORUM_ID_MODERATORS = FORUM+FORUM_ID+MODERATORS;

	// Subscription Services
	public static final String SUBSCRIPTION = "/subscription";
	public static final String LIST = "/list";
	public static final String SUBSCRIPTION_LIST = SUBSCRIPTION + LIST;
	public static final String SUBSCRIPTION_ALL = SUBSCRIPTION + ALL;
	public static final String SUBSCRIPTION_ID = SUBSCRIPTION + ID;
	public static final String OBJECT = "/object";
	public static final String OBJECT_ID = "/{objectId}";
	public static final String OBJECT_TYPE = "/{objectType}";
	public static final String ETAG = "/etag";
	public static final String OBJECT_ID_TYPE_ETAG = OBJECT+OBJECT_ID+OBJECT_TYPE+ETAG;
	public static final String SUBSCRIPTION_SUBSCRIBERS = SUBSCRIPTION + "/subscribers";
	public static final String SUBSCRIPTION_SUBSCRIBER_COUNT = SUBSCRIPTION_SUBSCRIBERS + "/count";
	
	// Docker authorization services
	public static final String DOCKER_AUTHORIZATION = "/bearerToken";
	public static final String ENITY_ID_DOCKER_COMMIT = ENTITY_ID+"/dockerCommit";
	public static final String ENTITY_ID_DOCKER_TAG = ENTITY_ID+"/dockerTag";
	public static final String DOCKER_REGISTRY_EVENTS = "/events";

	// Data Access Services
	public static final String RESEARCH_PROJECT = "/researchProject";
	public static final String ACCESS_REQUIREMENT_ID_RESEARCH_PROJECT =
			ACCESS_REQUIREMENT_WITH_REQUIREMENT_ID + "/researchProjectForUpdate";

	public static final String DATA_ACCESS_REQUEST ="/dataAccessRequest";
	public static final String ACCESS_REQUIREMENT_ID_DATA_ACCESS_REQUEST_FOR_UPDATE = 
			ACCESS_REQUIREMENT_WITH_REQUIREMENT_ID +"/dataAccessRequestForUpdate";

	public static final String DATA_ACCESS_REQUEST_ID_SUBMISSION = DATA_ACCESS_REQUEST+"/{requestId}/submission";
	public static final String DATA_ACCESS_SUBMISSION = "/dataAccessSubmission";
	public static final String DATA_ACCESS_SUBMISSION_ID = DATA_ACCESS_SUBMISSION + "/{submissionId}";
	public static final String DATA_ACCESS_SUBMISSION_ID_CANCEL = DATA_ACCESS_SUBMISSION_ID +"/cancellation";
	public static final String ACCESS_REQUIREMENT_ID_LIST_SUBMISSION =
			ACCESS_REQUIREMENT_WITH_REQUIREMENT_ID + "/submissions";
	public static final String ACCESS_REQUIREMENT_ID_STATUS =
			ACCESS_REQUIREMENT_WITH_REQUIREMENT_ID + "/status";
	public static final String RESTRICTION_INFORMATION = "/restrictionInformation";
	public static final String DATA_ACCESS_SUBMISSION_OPEN_SUBMISSIONS = DATA_ACCESS_SUBMISSION+"/openSubmissions";
	public static final String ACCESS_APPROVAL_BATCH = ACCESS_APPROVAL+"/batch";
	
	// Statistics Services
	public static final String STATISTICS = "/statistics";

	/**
	 * APIs for DynamoDB related operations.
	 */
	public static final String DYNAMO = "/dynamo";

	/**
	 * API for clearing the specified dynamo table.
	 */
	public static final String ADMIN_DYNAMO_CLEAR_TABLE = ADMIN + DYNAMO + "/clear" + "/{tableName}";
	
	/**
	 * Temporary API to migrate wiki pages from V1 to V2
	 */
	public static final String ADMIN_MIGRATE_WIKI = ADMIN + "/migrateWiki";
	
	// Authentication
	public static final String AUTH_SESSION = "/session";
	public static final String AUTH_USER = "/user";
	public static final String AUTH_USER_PASSWORD = AUTH_USER + "/password";
	public static final String AUTH_USER_CHANGE_PASSWORD = AUTH_USER + "/changePassword";

	public static final String AUTH_USER_PASSWORD_EMAIL = AUTH_USER_PASSWORD + "/email";
	public static final String AUTH_USER_PASSWORD_RESET = AUTH_USER_PASSWORD + "/reset";

	public static final String AUTH_TERMS_OF_USE = "/termsOfUse";
	public static final String AUTH_SECRET_KEY = "/secretKey";
	
	public static final String AUTH_OAUTH_2 = "/oauth2";
	public static final String AUTH_OAUTH_2_AUTH_URL = AUTH_OAUTH_2+"/authurl";
	public static final String AUTH_OAUTH_2_SESSION = AUTH_OAUTH_2+"/session";
	public static final String AUTH_OAUTH_2_ALIAS = AUTH_OAUTH_2+"/alias";
	public static final String AUTH_OAUTH_2_ACCOUNT = AUTH_OAUTH_2+"/account";
	public static final String WELL_KNOWN = "/.well-known";
	public static final String WELL_KNOWN_OPENID_CONFIGURATION = WELL_KNOWN+"/openid-configuration";

	/**
	 * OpenID Connect URL constants
	 */
	public static final String OAUTH_2_CLIENT = AUTH_OAUTH_2+"/client";
	public static final String OAUTH_2_CLIENT_ID = OAUTH_2_CLIENT+ID;
	public static final String OAUTH_2_CLIENT_SECRET = OAUTH_2_CLIENT+"/secret"+ID;
	public static final String OAUTH_2_CONSENT = AUTH_OAUTH_2+"/consent";
	public static final String OAUTH_2_TOKEN = AUTH_OAUTH_2+"/token";
	public static final String OAUTH_2_USER_INFO = AUTH_OAUTH_2+"/userinfo";
	public static final String OAUTH_2_JWKS = AUTH_OAUTH_2+"/jwks";
	public static final String OAUTH_2_AUTH_REQUEST_DESCRIPTION = AUTH_OAUTH_2+"/description";
	

	public static final String AUTH_LOGIN = "/login";
	
	/**
	 * API for creating integration test users
	 */
	public static final String ADMIN_USER = ADMIN + AUTH_USER;
	
	public static final String ADMIN_USER_ID = ADMIN_USER + ID;
	/**
	 * Clear all locks.
	 */
	public static final String ADMIN_CLEAR_LOCKS = ADMIN+"/locks";

	/**
	 * API for updating a file
	 * @see PLFM-4108
	 */
	public static final String ADMIN_UPDATE_FILE = ADMIN + "/updateFile";
	
	/**
	 * Request parameter to get the next page of a paginated query response.
	 */
	public static final String NEXT_PAGE_TOKEN_PARAM = "nextPageToken";


	static {
		@SuppressWarnings("rawtypes")
		Map<Class, String> property2urlsuffix = new HashMap<Class, String>();
		property2urlsuffix.put(Annotations.class, ANNOTATIONS);
		PROPERTY2URLSUFFIX = Collections.unmodifiableMap(property2urlsuffix);
	}

	/**
	 * Helper function to translate ids found in URLs to ids used by the system
	 * <p>
	 * 
	 * Specifically we currently use the serialized system id url-encoded for
	 * use in URLs
	 * 
	 * @param id
	 * @return URL-decoded entity id
	 */
	public static String getEntityIdFromUriId(String id) {
		String entityId = null;
		try {
			entityId = URLDecoder.decode(id, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.log(Level.SEVERE,
					"Something is really messed up if we don't support UTF-8",
					e);
		}
		return entityId;
	}
	
	/**
	 * Get the URL prefix from a request.
	 * @param request
	 * @return Servlet context and path url prefix
	 */
	public static String getUrlPrefixFromRequest(HttpServletRequest request){
		if(request == null) throw new IllegalArgumentException("Request cannot be null");
		if(request.getServletPath() == null) throw new IllegalArgumentException("Servlet path cannot be null");
		String urlPrefix = (null != request.getContextPath()) 
		? request.getContextPath() + request.getServletPath() 
				: request.getServletPath();
		return urlPrefix;
	}


	/**
	 * Create an ACL redirect URL.
	 * @param request - The initial request.
	 * @param type - The type of the redirect entity.
	 * @param id - The ID of the redirect entity
	 * @return
	 */
	public static String createACLRedirectURL(HttpServletRequest request, String id){
		if(request == null) throw new IllegalArgumentException("Request cannot be null");
		if(id == null) throw new IllegalArgumentException("ID cannot be null");
		StringBuilder redirectURL = new StringBuilder();
		redirectURL.append(UrlHelpers.getUrlPrefixFromRequest(request));
		redirectURL.append(UrlHelpers.ENTITY);
		redirectURL.append("/");
		redirectURL.append(id);
		redirectURL.append(UrlHelpers.ACL);
		return redirectURL.toString();
	}
	
	public static String getAttachmentTypeURL(ServiceConstants.AttachmentType type)
	{
		if (type == AttachmentType.ENTITY)
			return UrlHelpers.ENTITY;
		else if (type == AttachmentType.USER_PROFILE)
			return UrlHelpers.USER_PROFILE;
		else throw new IllegalArgumentException("Unrecognized attachment type: " + type);
	}
}
