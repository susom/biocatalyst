package edu.stanford.integrator.data;

import org.json.JSONObject;

public class DataTransferPacket {
  public JSONObject json;
  public DataTransferStatus status;

  public DataTransferPacket(DataTransferStatus Status) {
    this.status = Status;
    this.json = null;
  }

  public DataTransferPacket(DataTransferStatus Status, JSONObject Json) {
    this.status = Status;
    this.json = Json;
  }
}
