import { ConnectionDetails, IConnectionDetails } from './connection-details';
import { IIntegrationDetails, IntegrationDetails } from './integration-details';

export enum SourceType {
  CSV = 'CSV',
  OPENSPECIMEN = 'OPENSPECIMEN',
  REDCAP = 'REDCAP',
  EPIC = 'EPIC',
  OTHER = 'OTHER'
}

export class IDataSource {
  source_id: string;
  name: string;
  type: string;
  connection_details: IConnectionDetails;
  integration_details: IIntegrationDetails;
  deletable: boolean;
  valid: boolean;
  primary: boolean;
}

export class DataSource implements IDataSource {
  public source_id: string;
  public name: string;
  public type: string;
  public connection_details: ConnectionDetails;
  public integration_details: IntegrationDetails;
  public valid: boolean;
  public deletable: boolean;
  public primary: boolean;

  constructor(source?: IDataSource) {
    this.source_id = source ? source.source_id : '';
    this.name = source ? source.name : '';
    this.type = source ? source.type : SourceType.CSV;
    this.connection_details = source ? new ConnectionDetails(source.connection_details) : new ConnectionDetails();
    this.integration_details = source ? new IntegrationDetails(source.integration_details) : new IntegrationDetails();
    this.valid = true;
    this.deletable = source ? source.deletable : true;
    this.primary = source ? source.primary : false;
  }

  toJSON(): any {
    return {
      source_id: this.source_id,
      name: this.name,
      type: this.type,
      connection_details: this.connection_details,
      integration_details: this.integration_details.toJSON(),
      deletable: this.deletable,
      valid: this.valid,
      primary: this.primary
    };
  }
}
