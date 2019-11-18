package edu.stanford.integrator.services;

import edu.stanford.integrator.config.DataSourceConfig;
import java.util.List;

public interface TokenGrowthService {
  boolean detectTokenGrowth(DataSourceConfig sourceConfig, String current_id) throws Exception;

  List<Integer> correctedDelimiterPositions(DataSourceConfig sourceConfig, int growthIndex, List<Integer> original_delimiter_positions, String current_id) throws Exception;
}
