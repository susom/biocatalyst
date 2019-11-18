import { IIntegrationConnection, IntegrationConnection } from './integration-connections';
import { ITokenizationInstructions, TokenizationInstructions } from './tokenization-instructions';

export interface IIntegrationDetails {
  subjectCode: ITokenizationInstructions[];
  visitId: ITokenizationInstructions[];
  connections: IIntegrationConnection[];
}

export class IntegrationDetails implements IIntegrationDetails {
  public subjectCode: ITokenizationInstructions[];
  public visitId: ITokenizationInstructions[];
  public connections: IIntegrationConnection[];

  constructor(source?: IIntegrationDetails) {
    this.subjectCode = source ? Array.from(source.subjectCode, (x) => new TokenizationInstructions(x)) : [];
    this.visitId = source ? Array.from(source.visitId, (x) => new TokenizationInstructions(x)) : [];
    this.connections = source ? Array.from(source.connections, (x) => new IntegrationConnection(x)) : [];
  }

  public toJSON(): any {
    return {
      subjectCode: this.subjectCode,
      visitId: this.visitId,
      connections: this.connections
    };
  }
}
