package org.sagebionetworks.repo.manager.statistics.project;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.repo.manager.AuthorizationManager;
import org.sagebionetworks.repo.manager.statistics.StatisticsProvider;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.Node;
import org.sagebionetworks.repo.model.NodeDAO;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UnauthorizedException;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.model.dbo.statistics.StatisticsMonthlyProjectFilesDAO;
import org.sagebionetworks.repo.model.jdo.KeyFactory;
import org.sagebionetworks.repo.model.statistics.FileEvent;
import org.sagebionetworks.repo.model.statistics.FilesCountStatistics;
import org.sagebionetworks.repo.model.statistics.MonthlyFilesStatistics;
import org.sagebionetworks.repo.model.statistics.ProjectFilesStatisticsRequest;
import org.sagebionetworks.repo.model.statistics.ProjectFilesStatisticsResponse;
import org.sagebionetworks.repo.model.statistics.monthly.StatisticsMonthlyUtils;
import org.sagebionetworks.repo.model.statistics.project.StatisticsMonthlyProjectFiles;
import org.sagebionetworks.repo.web.NotFoundException;
import org.sagebionetworks.util.Pair;
import org.sagebionetworks.util.ValidateArgument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectFilesStatisticsProvider implements StatisticsProvider<ProjectFilesStatisticsRequest, ProjectFilesStatisticsResponse> {

	private AuthorizationManager authManager;
	private NodeDAO nodeDao;
	private StatisticsMonthlyProjectFilesDAO fileStatsDao;
	private int maxMonths;

	@Autowired
	public ProjectFilesStatisticsProvider(StackConfiguration stackConfig, AuthorizationManager authManager, NodeDAO nodeDao,
			StatisticsMonthlyProjectFilesDAO fileStatsDao) {
		this.authManager = authManager;
		this.nodeDao = nodeDao;
		this.fileStatsDao = fileStatsDao;
		this.maxMonths = stackConfig.getMaximumMonthsForMonthlyStatistics();
	}

	@Override
	public void verifyViewStatisticsAccess(UserInfo user, String objectId) throws UnauthorizedException {
		ValidateArgument.required(user, "The user");
		ValidateArgument.required(objectId, "The object id");

		Node project = nodeDao.getNode(objectId);

		if (!EntityType.project.equals(project.getNodeType())) {
			throw new NotFoundException("The id " + objectId + " does not refer to project");
		}

		String projectCreator = project.getCreatedByPrincipalId().toString();

		// Verify access to the project
		if (!authManager.isUserCreatorOrAdmin(user, projectCreator)) {
			authManager.canAccess(user, objectId, ObjectType.ENTITY, ACCESS_TYPE.VIEW_STATISTICS).checkAuthorizationOrElseThrow();
		}

	}

	@Override
	public Class<ProjectFilesStatisticsRequest> getSupportedType() {
		return ProjectFilesStatisticsRequest.class;
	}

	@Override
	public ProjectFilesStatisticsResponse getObjectStatistics(ProjectFilesStatisticsRequest request) {
		ValidateArgument.required(request, "The request");
		ValidateArgument.required(request.getObjectId(), "The project id");

		Long projectId = KeyFactory.stringToKey(request.getObjectId());

		ProjectFilesStatisticsResponse statistics = new ProjectFilesStatisticsResponse();

		statistics.setObjectId(request.getObjectId());

		List<YearMonth> months = StatisticsMonthlyUtils.generatePastMonths(maxMonths);

		if (request.getFileDownloads()) {
			statistics.setFileDownloads(getProjectFilesStatistics(projectId, FileEvent.FILE_DOWNLOAD, months));
		}
		if (request.getFileUploads()) {
			statistics.setFileUploads(getProjectFilesStatistics(projectId, FileEvent.FILE_UPLOAD, months));
		}

		return statistics;
	}

	MonthlyFilesStatistics getProjectFilesStatistics(Long projectId, FileEvent eventType, List<YearMonth> months) {
		MonthlyFilesStatistics statistics = new MonthlyFilesStatistics();

		List<FilesCountStatistics> filesCountStats = new ArrayList<>(months.size());

		YearMonth fromMonth = months.get(0);
		YearMonth toMonth = months.get(months.size() - 1);

		Map<YearMonth, StatisticsMonthlyProjectFiles> statsMap = getMonthlyProjectFilesMap(projectId, eventType, fromMonth, toMonth);

		for (YearMonth month : months) {
			FilesCountStatistics monthStats = getFilesCountStatistics(month, statsMap.get(month));
			filesCountStats.add(monthStats);
		}

		Date lastUpdatedOn = getLastUpdatedOnMax(statsMap.values());

		statistics.setMonths(filesCountStats);
		statistics.setLastUpdatedOn(lastUpdatedOn);

		return statistics;
	}

	Date getLastUpdatedOnMax(Collection<StatisticsMonthlyProjectFiles> statistics) {
		Optional<Long> maxTimestamp = statistics.stream().map(StatisticsMonthlyProjectFiles::getLastUpdatedOn).max(Long::compare);
		return maxTimestamp.isPresent() ? new Date(maxTimestamp.get()) : null;
	}

	FilesCountStatistics getFilesCountStatistics(YearMonth month, StatisticsMonthlyProjectFiles statistics) {
		Pair<Long, Long> monthRange = StatisticsMonthlyUtils.getTimestampRange(month);

		FilesCountStatistics monthStats = new FilesCountStatistics();

		monthStats.setRangeStart(new Date(monthRange.getFirst()));
		monthStats.setRangeEnd(new Date(monthRange.getSecond()));
		monthStats.setFilesCount(0L);
		monthStats.setUsersCount(0L);

		if (statistics != null) {
			Integer filesCount = statistics.getFilesCount();
			if (filesCount != null) {
				monthStats.setFilesCount(filesCount.longValue());
			}
			Integer usersCount = statistics.getUsersCount();
			if (usersCount != null) {
				monthStats.setUsersCount(usersCount.longValue());
			}
		}

		return monthStats;
	}

	Map<YearMonth, StatisticsMonthlyProjectFiles> getMonthlyProjectFilesMap(Long projectId, FileEvent eventType, YearMonth from,
			YearMonth to) {
		return fileStatsDao.getProjectFilesStatisticsInRange(projectId, eventType, from, to).stream()
				.collect(Collectors.toMap(StatisticsMonthlyProjectFiles::getMonth, Function.identity()));
	}

}
