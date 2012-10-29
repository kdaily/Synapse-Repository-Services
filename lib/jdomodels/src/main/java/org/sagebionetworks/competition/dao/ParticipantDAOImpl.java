package org.sagebionetworks.competition.dao;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.competition.dbo.CompetitionDBO;
import org.sagebionetworks.competition.dbo.DBOConstants;
import org.sagebionetworks.competition.dbo.ParticipantDBO;
import org.sagebionetworks.competition.model.Competition;
import org.sagebionetworks.competition.model.Participant;
import org.sagebionetworks.competition.query.jdo.SQLConstants;
import org.sagebionetworks.competition.util.Utility;
import org.sagebionetworks.ids.ETagGenerator;
import org.sagebionetworks.ids.IdGenerator;
import org.sagebionetworks.repo.model.ConflictingUpdateException;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.InvalidModelException;
import org.sagebionetworks.repo.model.dbo.DBOBasicDao;
import org.sagebionetworks.repo.web.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class ParticipantDAOImpl{
	
	@Autowired
	private DBOBasicDao basicDao;
	
	@Autowired
	private SimpleJdbcTemplate simpleJdbcTemplate;
	
	private static final String SELECT_ALL_SQL_PAGINATED = 
			"SELECT * FROM "+ SQLConstants.TABLE_PARTICIPANT +
			" LIMIT :"+ SQLConstants.LIMIT_PARAM_NAME +
			" OFFSET :" + SQLConstants.OFFSET_PARAM_NAME;
	
	private static final String SELECT_BY_COMPETITION_SQL = 
			"SELECT * FROM "+ SQLConstants.TABLE_PARTICIPANT +
			" WHERE "+ SQLConstants.COL_PARTICIPANT_COMP_ID + "=:"+ 
			DBOConstants.PARAM_PARTICIPANT_COMP_ID;
	
	private static final RowMapper<CompetitionDBO> rowMapper = ((new CompetitionDBO()).getTableMapping());

	@Override
	public String create(Participant dto) throws DatastoreException {		
		// Convert to DBO
		ParticipantDBO dbo = new ParticipantDBO();
		copyDtoToDbo(dto, dbo);
		
		// Ensure name is not taken
		if (find(dto.getName()) != null)
			throw new IllegalArgumentException("Sorry, a Competition already exists with the name " + dto.getName());
		
		// Set creation date
		dbo.setCreatedOn(new Date());
		
		// Ensure DBO has required information
		verifyCompetitionDBO(dbo);
		
		// Create DBO
		try {
			dbo = basicDao.createNew(dbo);
			return dbo.getId().toString();
		} catch (Exception e) {
			throw new DatastoreException("id="+dbo.getId()+" name="+dto.getName(), e);
		}
	}

	@Override
	public Competition get(String id) throws DatastoreException, NotFoundException {
		MapSqlParameterSource param = new MapSqlParameterSource();
		param.addValue(ID_PARAM_NAME, id);
		CompetitionDBO dbo = basicDao.getObjectById(CompetitionDBO.class, param);
		Competition dto = new Competition();
		copyDboToDto(dbo, dto);
		return dto;
	}

	@Override
	public List<Competition> getInRange(long startIncl, long endExcl) throws DatastoreException, NotFoundException {
		MapSqlParameterSource param = new MapSqlParameterSource();
		param.addValue(SQLConstants.OFFSET_PARAM_NAME, startIncl);
		long limit = endExcl - startIncl;
		if (limit <= 0)
			throw new IllegalArgumentException("'to' param must be greater than 'from' param.");
		param.addValue(SQLConstants.LIMIT_PARAM_NAME, limit);	
		List<CompetitionDBO> dbos = simpleJdbcTemplate.query(SELECT_ALL_SQL_PAGINATED, rowMapper, param);
		List<Competition> dtos = new ArrayList<Competition>();
		for (CompetitionDBO dbo : dbos) {
			Competition dto = new Competition();
			copyDboToDto(dbo, dto);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public long getCount() throws DatastoreException, NotFoundException {
		return basicDao.getCount(CompetitionDBO.class);
	}

	@Override
	public Competition find(String name) throws DatastoreException {
		MapSqlParameterSource param = new MapSqlParameterSource();
		param.addValue(NAME_PARAM_NAME, name);
		List<CompetitionDBO> comps = simpleJdbcTemplate.query(SELECT_BY_NAME_SQL, rowMapper, param);
		if (comps.size() > 1) 
			throw new DatastoreException("Expected 0-1 Competitions but found " + comps.size());
		if (comps.size() == 0) 
			return null;
		Competition dto = new Competition();
		copyDboToDto(comps.iterator().next(), dto);
		return dto;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void update(Competition dto)
			throws DatastoreException, InvalidModelException,
			NotFoundException, ConflictingUpdateException {		
		CompetitionDBO dbo = new CompetitionDBO();
		copyDtoToDbo(dto, dbo);
		verifyCompetitionDBO(dbo);
		basicDao.update(dbo);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void delete(String id) throws DatastoreException, NotFoundException {
		MapSqlParameterSource param = new MapSqlParameterSource();
		param.addValue(ID_PARAM_NAME, id);
		basicDao.deleteObjectById(CompetitionDBO.class, param);		
	}
	
	@Transactional(readOnly = true)
	public boolean doesIdExist(Long id) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(ID_PARAM_NAME, id);
		try {
			long count = simpleJdbcTemplate.queryForLong(COUNT_COMPETITIONS_BY_ID_SQL, parameters);
			return count > 0;
		} catch(Exception e) {
			// Can occur when the schema does not exist.
			return false;
		}
	}

	/**
	 * Copy a CompetitionDBO database object to a Competition data transfer object
	 * 
	 * @param dto
	 * @param dbo
	 */
	private static void copyDtoToDbo(Competition dto, CompetitionDBO dbo) {		
		dbo.setId(dto.getId() == null ? null : Long.parseLong(dto.getId()));
		dbo.seteTag(dto.getEtag());
		dbo.setName(dto.getName());
		dbo.setDescription(dto.getDescription() == null ? null : dto.getDescription().getBytes());
		dbo.setOwnerId(dto.getOwnerId() == null ? null : Long.parseLong(dto.getOwnerId()));
		dbo.setCreatedOn(dto.getCreatedOn());
		dbo.setContentSource(dto.getContentSource());
		dbo.setStatusEnum(dto.getStatus());
	}
	
	/**
	 * Copy a Competition data transfer object to a CompetitionDBO database object
	 * 
	 * @param dbo
	 * @param dto
	 * @throws DatastoreException
	 */
	private static void copyDboToDto(CompetitionDBO dbo, Competition dto) throws DatastoreException {		
		dto.setId(dbo.getId() == null ? null : dbo.getId().toString());
		dto.setEtag(dbo.geteTag());
		dto.setName(dbo.getName());
		if (dbo.getDescription() != null) {
			try {
				dto.setDescription(new String(dbo.getDescription(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new DatastoreException(e);
			}
		} else {
			dto.setDescription(null);
		}
		dto.setOwnerId(dbo.getOwnerId().toString());
		dto.setCreatedOn(dbo.getCreatedOn());
		dto.setContentSource(dbo.getContentSource());
		dto.setStatus(dbo.getStatusEnum());
	}

	/**
	 * Ensure that a CompetitionDBO object has all required components
	 * 
	 * @param dbo
	 */
	private void verifyCompetitionDBO(CompetitionDBO dbo) {
		Utility.ensureNotNull(dbo.getId(), "Competition ID");
		Utility.ensureNotNull(dbo.geteTag(), "Competition eTag");
		Utility.ensureNotNull(dbo.getName(), "Competition name");
		Utility.ensureNotNull(dbo.getOwnerId(), "Competition owner ID");
		Utility.ensureNotNull(dbo.getCreatedOn(), "Competition creation date");
		Utility.ensureNotNull(dbo.getContentSource(), "Competition content source");
		Utility.ensureNotNull(dbo.getStatusEnum(), "Competition status");
	}
	
}
