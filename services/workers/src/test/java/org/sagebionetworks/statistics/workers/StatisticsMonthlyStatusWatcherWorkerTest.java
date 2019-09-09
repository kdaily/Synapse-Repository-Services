package org.sagebionetworks.statistics.workers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sagebionetworks.repo.manager.statistics.monthly.StatisticsMonthlyManager;
import org.sagebionetworks.repo.model.statistics.StatisticsObjectType;

@ExtendWith(MockitoExtension.class)
public class StatisticsMonthlyStatusWatcherWorkerTest {

	@Mock
	private StatisticsMonthlyManager mockStatisticsManager;

	private StatisticsMonthlyStatusWatcherWorker worker;

	@BeforeEach
	public void before() {
		worker = new StatisticsMonthlyStatusWatcherWorker(mockStatisticsManager);
	}

	@Test
	public void testRunWithNoProcessing() throws Exception {
		List<YearMonth> unprocessedMonths = Collections.emptyList();
		when(mockStatisticsManager.getUnprocessedMonths(any(StatisticsObjectType.class))).thenReturn(unprocessedMonths);

		// Call under test
		worker.run(null);

		for (StatisticsObjectType type : StatisticsObjectType.values()) {
			verify(mockStatisticsManager).getUnprocessedMonths(type);
		}

		verify(mockStatisticsManager, never()).startProcessingMonth(any(), any(), any(Long.class));
	}

	@Test
	public void testRunWithWithProcessing() throws Exception {
		YearMonth month = YearMonth.of(2019, 8);

		List<YearMonth> unprocessedMonths = Collections.singletonList(month);

		when(mockStatisticsManager.startProcessingMonth(any(StatisticsObjectType.class), any(YearMonth.class), any(Long.class)))
				.thenReturn(true);
		when(mockStatisticsManager.getUnprocessedMonths(any(StatisticsObjectType.class))).thenReturn(unprocessedMonths);

		// Call under test
		worker.run(null);

		for (StatisticsObjectType type : StatisticsObjectType.values()) {
			verify(mockStatisticsManager).getUnprocessedMonths(type);
			verify(mockStatisticsManager).startProcessingMonth(eq(type), eq(month), any(Long.class));
		}
	}

}
