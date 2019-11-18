import * as moment from 'moment';
import { Message } from 'primeng/components/common/api';

export interface IImportStatus {
  info: string;
  startTimestamp: string;
  lastIntegration: string;
  state: string;
  severity: string;
}

export class ImportStatus implements IImportStatus {
  public info: string;
  public startTimestamp: string;
  public lastIntegration: string;
  public state: string;
  public severity: string;

  constructor(source?: IImportStatus) {
    this.info = source ? source.info : '';
    this.startTimestamp = source ? source.startTimestamp : '';
    this.lastIntegration = source ? source.lastIntegration : '';
    this.state = source ? source.state : '';
    this.severity = source ? source.severity : 'success';
  }

  public static fromObjectToMessages(importStatus: IImportStatus, severity: string = null): Message[] {
    const status = new ImportStatus();
    const result: Message[] = [];

    if (!severity) {
      severity = 'success';
      if (importStatus.info) {
        const info = importStatus.info.substr(0, 7);
        if (info || info === 'Errors.') {
          severity = 'error';
        }
      }
    }

    for (const property in status) {
      if (property !== 'severity' && property !== 'state') {
        const newMessage: Message = {};
        newMessage.severity = severity;
        newMessage.summary = '';
        newMessage.detail = importStatus[property];

        if (newMessage.detail) {
          if (newMessage.detail.length > 0) {
            if (property === 'startTimestamp' || property === 'lastIntegration') {
              const dateFormat = 'MMMM Do YYYY, h:mm:ss A';
              const originalTime = moment.utc(newMessage.detail);
              newMessage.detail = `Integration started at ${originalTime.local().format(dateFormat)}`;
            }
            result.push(newMessage);
          }
        }
      }
    }
    return result;
  }
}
