package edu.stanford.integrator.transform.visitdate;

import edu.stanford.integrator.config.DataSourceConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;

@Service
public class VisitDateTransformServiceImpl implements VisitDateTransformService {
  private static final Logger LOGGER = Logger.getLogger(VisitDateTransformServiceImpl.class.getName());

  private DataSourceConfig dataSourceConfig;

  public DataSourceConfig getDataSourceConfig(DataSourceConfig dataSourceConfig) {
    return this.dataSourceConfig;
  }

  public void setDataSourceConfig(DataSourceConfig dataSourceConfig) {
    this.dataSourceConfig = dataSourceConfig;
  }

  public String transform(String inputValue) throws Exception {
    // If the inputValue is null (partial data, missing fields)
    if (inputValue == null) {
      return inputValue;
    }

    // System time is this format:
    DateTimeFormatter systemFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    String transformedVisitDate = "";

    DataSourceConfig dataSourceConfig = getDataSourceConfig(this.dataSourceConfig);
    final String visitDateFormat = dataSourceConfig.getVisitDateFormat();

    final String[] dateFormats = {
        "dd-MM-yyyy",
        "MM-dd-yyyy",
        "mm-dd-yyyy",
        "yyyy-mm-dd",
        "yyyy-MM-dd",
        "MMM dd, yyyy"
    };

    final String[] dateTimeFormats = {
        "dd-MM-yyyy HH:mm",
        "MM-dd-yyyy HH:mm",
        "yyyy-MM-dd HH:mm",
        "dd-MM-yyyy HH:mm:ss",
        "MM-dd-yyyy HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss"
    };

    try {

      // If it's a Date only, not DateTime, convert to a LocalDateTime first
      for (String date : dateFormats) {
        if (date.equals(visitDateFormat)) {
          DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(date);
          LocalDateTime dt = LocalDate.parse(inputValue, inputFormatter).atStartOfDay();
          transformedVisitDate = dt.format(systemFormatter);
          return transformedVisitDate;
        }
      }

      for (String dateTime : dateTimeFormats) {
        if (dateTime.equals(visitDateFormat)) {
          DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(dateTime);
          LocalDateTime dt = LocalDateTime.parse(inputValue, inputFormatter);
          transformedVisitDate = dt.format(systemFormatter);
          return transformedVisitDate;
        }
      }
    } catch (DateTimeParseException ex) {
      LOGGER.log(Level.INFO, "DateTimeParseException: " + ex);
      LOGGER.log(Level.INFO, "Source Visit Date Format: " + visitDateFormat + " is not supported.");
      LOGGER.log(Level.INFO, "Supported Visit Dates: " + Arrays.toString(dateFormats));
      LOGGER.log(Level.INFO, "Supported Visit DateTimes: " + Arrays.toString(dateTimeFormats));
      throw new Exception("Source Visit Date Format: " + visitDateFormat + " is not supported.");
    }

    return "VISIT_DATE_IS_NOT_A_SUPPORTED_FORMAT";
  }
}
