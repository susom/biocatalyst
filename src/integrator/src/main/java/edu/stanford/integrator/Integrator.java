package edu.stanford.integrator;

import static edu.stanford.integrator.ServiceStatus.State.INACTIVE;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The integrator class is assigned to a specific integration configuration, which is comprised of several data-sources
 * and is responsible for importing, integrating and indexing the data, and making it available with a REST API to be
 * searched by the end user
 */
public class Integrator {
	private static final Logger LOGGER = Logger.getLogger(Integrator.class.getName());
	
	private String integrationID;
	private ServiceStatus status = new ServiceStatus(INACTIVE, "");
	private String importedSpecimenTable;
	private String primarySourceColumnPrefix;
	private Map<String,String> primaryDataSourceIDColumnNames;

	public Integrator(String integrationID) {
		if (integrationID == null || integrationID.equals("")) {
			throw new IllegalArgumentException("Configuration ID is missing");
		}
		
		this.integrationID = integrationID;
		this.status = new ServiceStatus(INACTIVE, "");
		primaryDataSourceIDColumnNames = new HashMap<>();
	}
	
	public String getIntegrationID() {
		return integrationID;
	}
	
	public String getImportedSpecimenTable() {
		return importedSpecimenTable;
	}

	public void setImportedSpecimenTable(String importedSpecimenTable) {
		this.importedSpecimenTable = importedSpecimenTable;
	}

	public String getPrimarySourceColumnPrefix() {
		return primarySourceColumnPrefix;
	}

	public void setPrimarySourceColumnPrefix(String primarySourceColumnPrefix) {
		this.primarySourceColumnPrefix = primarySourceColumnPrefix;
	}

	public Map<String, String> getPrimaryDataSourceIDColumnNames() {
		return primaryDataSourceIDColumnNames;
	}

	public void setPrimaryDataSourceIDColumnNames(Map<String, String> primaryDataSourceIDColumnNames) {
		this.primaryDataSourceIDColumnNames = primaryDataSourceIDColumnNames;
	}

	public synchronized void  setStatus(ServiceStatus integrationStatus) {
		this.status = integrationStatus;
		LOGGER.log(Level.INFO, "Setting "+ integrationID +" integration status to: " + integrationStatus.toString());
	}

	public ServiceStatus getStatus() {
		return status;
	}
	
}
