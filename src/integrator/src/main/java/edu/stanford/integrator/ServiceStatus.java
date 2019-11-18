package edu.stanford.integrator;

import java.util.Calendar;
import java.util.logging.Logger;

public final class ServiceStatus {
  private static Logger LOGGER = Logger.getLogger(ServiceStatus.class.getName());

  public enum State {
    ACTIVE, INACTIVE, UNKNOWN
  }

  private State state = State.UNKNOWN;
  private String info = "";
  private String startTimestamp = "";

  @Override
  public String toString() {
    String result = new String("State: " + getState());
    if (getInfo().equals("") == false) {
      result = result + ", " + "Info: " + getInfo();
    }
    if (getStartTimestamp().equals("") == false) {
      result = result + ", " + "startTimestamp: " + getStartTimestamp();
    }
    return result;
  }

  public ServiceStatus(ServiceStatus.State newState) {
    setState(newState);
  }

  public ServiceStatus(State state, Exception ex) {
    this.setState(state, ex);
  }

  public ServiceStatus(State state, String info) {
    this.setState(state, info);
  }

  public void setState(State newState) {
    if (newState != State.ACTIVE) {
      this.setStartTimestamp("");
    }

    if (newState == State.ACTIVE
        && this.getState() != State.ACTIVE
        && (getStartTimestamp() == null || getStartTimestamp().equals(""))) {

      String timeStampString = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()).toString();
      this.setStartTimestamp(timeStampString);
    }

    this.state = newState;
  }

  public void setState(State newState, String info) {
    this.setState(newState);
    this.setInfo(info);
  }

  public void setState(State newState, Exception ex) {
    this.setState(newState);
    this.setInfo(ex);
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public void setInfo(Exception ex) {
    this.info = "Errors. " + ex.getLocalizedMessage();
  }

  public void setStartTimestamp(String startTimestamp) {
    this.startTimestamp = startTimestamp;
  }

  public State getState() {
    return this.state;
  }

  public String getInfo() {
    return this.info;
  }

  public String getStartTimestamp() {
    return this.startTimestamp;
  }
}
