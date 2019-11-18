package edu.stanford.integrator.data;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.springframework.stereotype.Service;

@Service
public class DataTransfer {
  public void execute(final IDataTransfer dataTransfer) throws JSONException {
    // Submit a data Request
    System.out.println("DataTransfer Request" + dataTransfer.toString());
    try {
      DataTransferPacket requestPacket = this.request(dataTransfer);
      if (requestPacket.status == DataTransferStatus.failed) {
        throw new Exception("Epic Request Failed:" + requestPacket.json.toString());
      }

      DataTransferPacket statusPacket = this.checkStatus(dataTransfer, requestPacket);
      if (requestPacket.status == DataTransferStatus.failed) {
        throw new Exception("Epic Status Request Failed:" + statusPacket.json.toString());
      }

      DataTransferPacket dataPacket = this.getData(dataTransfer, statusPacket);
      if (requestPacket.status == DataTransferStatus.failed) {
        throw new Exception("Epic Data Retrieval Failed:" + dataPacket.json.toString());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private DataTransferPacket request(final IDataTransfer dataTransfer) throws Exception {
    return dataTransfer.request();
  }

  private DataTransferPacket checkStatus(final IDataTransfer dataTransfer, final DataTransferPacket requestTransferPacket) throws Exception {
    if (!CheckDataTransferPacketStatusIsReady(requestTransferPacket)) {
      return new DataTransferPacket(DataTransferStatus.failed);
    }
    class DTP {
      // this class acts as a handler for use in the timerTask
      DataTransferPacket packet = new DataTransferPacket(DataTransferStatus.ready);
    }
    final DTP statusTransferPacket = new DTP();

    // Check the Status of the Request
    final Timer timer = new Timer();
    final CountDownLatch startSignal = new CountDownLatch(1);
    class CheckStatus extends TimerTask {
      @Override
      public void run() {
        System.out.println("DataTransfer Status");
        try {
          statusTransferPacket.packet = dataTransfer.checkStatus(requestTransferPacket);
          switch (statusTransferPacket.packet.status) {
            case yettostart:
              System.out.println("DataTransfer yet to start...");
              break;
            case ready:
              System.out.println("DataTransfer ready");
              timer.cancel();
              startSignal.countDown();
              break;
            case building:
              System.out.println("DataTransfer building");
              break;
            case failed:
              System.out.println("DataTransfer failed");
              timer.cancel();
              startSignal.countDown();
              break;
            default:
              break;
          }
        } catch (JSONException e) {
          statusTransferPacket.packet.status = DataTransferStatus.failed;
          timer.cancel();
          startSignal.countDown();
          e.printStackTrace();
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    class Timeout extends TimerTask {
      @Override
      public void run() {
        timer.cancel();
        startSignal.countDown();
      }
    }

    int checkStatusInterval = 2;
    int timeoutInterval = 2;
    int timeoutIntervalRepeat = 5;

    timer.schedule(new CheckStatus(), 0, TimeUnit.SECONDS.toMillis(checkStatusInterval)); // Should Be controllable the reoccourance
    timer.scheduleAtFixedRate(new Timeout(), TimeUnit.HOURS.toMillis(timeoutInterval), TimeUnit.SECONDS.toMillis(timeoutIntervalRepeat));
    startSignal.await(); // Wait for Check Status to finish.

    System.out.println("DataTransfer Retrieve Data");
    return statusTransferPacket.packet;
  }

  private DataTransferPacket getData(final IDataTransfer dataTransfer, final DataTransferPacket statusTransferPacket) throws Exception {
    if (!CheckDataTransferPacketStatusIsReady(statusTransferPacket)) {
      return new DataTransferPacket(DataTransferStatus.failed);
    }
    dataTransfer.getData(statusTransferPacket);
    return null;
  }

  private boolean CheckDataTransferPacketStatusIsReady(DataTransferPacket transferPacket) {
    if (transferPacket == null) {
      return false;
    }
    return transferPacket.status == DataTransferStatus.ready;
  }
}
