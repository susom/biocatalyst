package edu.stanford.integrator.services;

import static java.lang.Integer.max;


import edu.stanford.integrator.config.DataSourceConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;

@Service
public class TokenGrowthServiceImpl implements TokenGrowthService {
  private static final Logger LOGGER = Logger.getLogger(TokenGrowthService.class.getName());

  public boolean detectTokenGrowth(DataSourceConfig sourceConfig, String current_id) throws Exception {
    String processed_id = sourceConfig.getProcessedId("ppid"); // only PPID supported for now
    if (processed_id == null || processed_id.isEmpty()) {
      LOGGER.log(Level.SEVERE, "processed_id is empty, did a source not get configured with the preprocessor modal? Or is the UI not correctly saving to the backend ES model?");
      throw new Exception("ERROR: failed to pull processed_id needed for token growth algorithm");
    }
    // Strip delimiters
    processed_id = processed_id.replace("-", "");
    current_id = current_id.replace("-", "");

    // current_id comes in wrapped in double quotes, so strip those as well
    current_id = current_id.replace("\"", "");

    return processed_id.length() != current_id.length();
  }

  public List<Integer> correctedDelimiterPositions(DataSourceConfig sourceConfig, int growthIndex, List<Integer> original_delimiter_positions, String current_id) {
    List<Integer> correctedDelimiterPositions = new ArrayList<>();
    int numberOfTokens = original_delimiter_positions.size() + 1; // e.g. a delim list like [4,6] means there are 3 tokens
    if (numberOfTokens == growthIndex) {
      // if token growth is in the last index position, no correction needed
      LOGGER.log(Level.INFO, "last index position requires no token growth");
      return original_delimiter_positions;
    }

    String processed_id = sourceConfig.getProcessedId("ppid");
    // Strip delimiters
    processed_id = processed_id.replace("-", "");
    current_id = current_id.replace("-", "");
    // current_id comes in wrapped in double quotes, so strip those as well
    current_id = current_id.replace("\"", "");

    if (processed_id.length() == current_id.length()) {
      // length is the same, no need to correct delimiter positions
      LOGGER.log(Level.INFO, "id lengths are the same no change needed");
      return original_delimiter_positions;
    }

    // Compare the size of the current_id to that of the processed id - this is how many characters we need to modify
    // NOTE: Assumes that only one token is changing.

    // Determine which ID is longer with the max() method
    int bumpSize1 = current_id.length() - processed_id.length();
    int bumpSize2 = processed_id.length() - current_id.length();
    final int bumpSize = max(bumpSize1, bumpSize2);

    // Create a list of indexes to bump
    // every index after the growthIndex must be bumped
    List<Integer> indexesToBump = new ArrayList<>();
    for (int i = 0; i < original_delimiter_positions.size(); i++) {
      if (i >= growthIndex) {
        indexesToBump.add(i);
      }
    }

    // Algorithm to correct delimiter positions
    // all index positions at the growth index or later must be bumped by the size of the difference of the token in processed_id compared to the current token length
    for (int i = 0; i < original_delimiter_positions.size(); i++) {
      if (indexesToBump.contains(i)) {
        correctedDelimiterPositions.add(original_delimiter_positions.get(i) + bumpSize);
      } else {
        correctedDelimiterPositions.add(original_delimiter_positions.get(i));
      }
    }

    return correctedDelimiterPositions;
  }
}
