export interface ICredentials {
  user: string;
  password: string;
}

export class Credentials implements ICredentials {
  public user: string;
  public password: string;

  constructor(source?: ICredentials) {
    this.user = source ? source.user : '';
    this.password = source ? source.password : '';
  }

  toJSON(): any {
    return {
      user: this.user,
      password: this.password
    };
  }
}

