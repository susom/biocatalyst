import { Credentials, ICredentials } from './credentials';

export class IConnectionDetails {
  URL: string;
  filename: any;
  headers: any;
  data: any;
  credentials: ICredentials;
}

export class ConnectionDetails implements IConnectionDetails {
  public URL: string;
  public filename: any;
  public headers: any;
  public data: any;
  public credentials: ICredentials;

  constructor(source?: IConnectionDetails) {
    this.URL = source ? source.URL : '';
    this.filename = source ? source.filename : '';
    this.headers = source ? source.headers : [];
    this.data = source ? source.data : null;
    this.credentials = source ? new Credentials(source.credentials) : new Credentials();
  }

  toJSON(): any {
    return {
      URL: this.URL,
      filename: this.filename,
      headers: this.headers,
      data: this.data,
      credentials: this.credentials
    };
  }
}
