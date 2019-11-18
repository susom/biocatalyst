package edu.stanford.integrator.data;

public interface IDataTransfer {
  DataTransferPacket request() throws Exception;

  DataTransferPacket checkStatus(DataTransferPacket requestPacket) throws Exception;

  DataTransferPacket getData(DataTransferPacket statusPacket) throws Exception;
}
